package org.dataland.e2etests.tests

import org.awaitility.Awaitility.await
import org.dataland.communitymanager.openApiClient.model.CompanyRole
import org.dataland.datalandbackend.openApiClient.model.CompanyAssociatedDataEutaxonomyNonFinancialsData
import org.dataland.datalandbackend.openApiClient.model.CompanyAssociatedDataSfdrData
import org.dataland.datalandbackend.openApiClient.model.DataMetaInformation
import org.dataland.datalandqaservice.openApiClient.api.QaControllerApi
import org.dataland.datalandqaservice.openApiClient.model.QaStatus
import org.dataland.datalandqaservice.openApiClient.model.ReviewInformationResponse
import org.dataland.datalandqaservice.openApiClient.model.ReviewQueueResponse
import org.dataland.e2etests.auth.GlobalAuth.withTechnicalUser
import org.dataland.e2etests.auth.TechnicalUser
import org.dataland.e2etests.utils.ApiAccessor
import org.dataland.e2etests.utils.DocumentManagerAccessor
import org.dataland.e2etests.utils.testDataProvivders.GeneralTestDataProvider
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import java.time.Instant
import java.util.*
import java.util.concurrent.TimeUnit
import org.dataland.datalandbackend.openApiClient.infrastructure.ClientException as BackendClientException
import org.dataland.datalandbackend.openApiClient.model.QaStatus as BackendQaStatus
import org.dataland.datalandqaservice.openApiClient.infrastructure.ClientException as QaServiceClientException
import org.dataland.datalandqaservice.openApiClient.model.QaStatus as QaServiceQaStatus

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class QaServiceTest {
    private val apiAccessor = ApiAccessor()
    private val documentManagerAccessor = DocumentManagerAccessor()
    private val dataController = apiAccessor.dataControllerApiForEuTaxonomyNonFinancials
    private val qaServiceController = apiAccessor.qaServiceControllerApi

    private lateinit var dummyEuTaxoDataAlpha: CompanyAssociatedDataEutaxonomyNonFinancialsData
    private lateinit var dummySfdrDataBeta: CompanyAssociatedDataSfdrData
    private lateinit var companyIdAlpha: String
    private lateinit var companyIdBeta: String

    private val expectedClientError403Text = "Client error : 403 "

    @BeforeAll
    fun postCompaniesAndBuildTestDatasets() {
        documentManagerAccessor.uploadAllTestDocumentsAndAssurePersistence()

        companyIdAlpha = apiAccessor.uploadOneCompanyWithRandomIdentifier().actualStoredCompany.companyId
        val testDataEuTaxo =
            apiAccessor.testDataProviderForEuTaxonomyDataForNonFinancials.getTData(1).first()
        dummyEuTaxoDataAlpha = CompanyAssociatedDataEutaxonomyNonFinancialsData(companyIdAlpha, "2024", testDataEuTaxo)

        val timestamp = Instant.now().toEpochMilli().toString()
        val companyInfoBeta = GeneralTestDataProvider().generateCompanyInformation("Beta-Company-$timestamp", null)
        val testDataSfdr = apiAccessor.testDataProviderForSfdrData.getTData(1).first()
        companyIdBeta = apiAccessor.companyDataControllerApi.postCompany(companyInfoBeta).companyId
        dummySfdrDataBeta = CompanyAssociatedDataSfdrData(companyIdBeta, "2024", testDataSfdr)
    }

    @BeforeEach
    @AfterEach
    fun clearTheReviewQueue() {
        withTechnicalUser(TechnicalUser.Reviewer) {
            getInfoOnUnreviewedDatasets().forEach { assignQaStatus(it.dataId, QaServiceQaStatus.Rejected) }
            await().atMost(2, TimeUnit.SECONDS)
                .until {
                    getInfoOnUnreviewedDatasets().isEmpty()
                }
        }
    }

    @Test
    fun `post dummy data and accept it and check the qa status changes and check different users access permissions`() {
        val dataId = uploadEuTaxoDataAndValidatePendingState()
        acceptDatasetAsReviewer(dataId, QaServiceQaStatus.Accepted)
        waitForExpectedQaStatus(dataId, BackendQaStatus.Accepted)
        canUserSeeUploaderData(dataId, TechnicalUser.Reader, true, false)
        canUserSeeUploaderData(dataId, TechnicalUser.Uploader, true, true)
        canUserSeeUploaderData(dataId, TechnicalUser.Reviewer, true, false)
        canUserSeeUploaderData(dataId, TechnicalUser.Admin, true, true)
    }

    @Test
    fun `post dummy data and reject it and check the qa status changes and check different users access permissions`() {
        val dataId = uploadEuTaxoDataAndValidatePendingState()
        acceptDatasetAsReviewer(dataId, QaServiceQaStatus.Rejected)
        withTechnicalUser(TechnicalUser.Uploader) {
            waitForExpectedQaStatus(dataId, BackendQaStatus.Rejected)
            canUserSeeUploaderData(dataId, TechnicalUser.Reader, false)
            canUserSeeUploaderData(dataId, TechnicalUser.Uploader, true, true)
            canUserSeeUploaderData(dataId, TechnicalUser.Reviewer, true, false)
            canUserSeeUploaderData(dataId, TechnicalUser.Admin, true, true)
        }
    }

    @Test
    fun `post dummy data and check different users access permissions`() {
        val dataId = uploadEuTaxoDataAndValidatePendingState()
        canUserSeeUploaderData(dataId, TechnicalUser.Reader, false)
        canUserSeeUploaderData(dataId, TechnicalUser.Uploader, true, true)
        canUserSeeUploaderData(dataId, TechnicalUser.Reviewer, true, false)
        canUserSeeUploaderData(dataId, TechnicalUser.Admin, true, true)
    }

    private fun uploadEuTaxoDataAndValidatePendingState(user: TechnicalUser = TechnicalUser.Uploader): String {
        withTechnicalUser(user) {
            val dataId =
                dataController.postCompanyAssociatedEutaxonomyNonFinancialsData(dummyEuTaxoDataAlpha, false).dataId
            assertEquals(BackendQaStatus.Pending, getDataMetaInfo(dataId).qaStatus)
            return dataId
        }
    }

    private fun acceptDatasetAsReviewer(dataId: String, qaStatus: QaServiceQaStatus) {
        withTechnicalUser(TechnicalUser.Reviewer) {
            await().atMost(2, TimeUnit.SECONDS)
                .until { getInfoOnUnreviewedDatasets().map { it.dataId }.contains(dataId) }
            assignQaStatus(dataId, qaStatus)
            assertFalse(getInfoOnUnreviewedDatasets().map { it.dataId }.contains(dataId))
        }
    }

    private fun canUserSeeUploaderData(
        dataId: String,
        viewingUser: TechnicalUser,
        shouldDataBeVisible: Boolean,
        shouldUploaderBeVisible: Boolean? = null,
    ) {
        withTechnicalUser(viewingUser) {
            if (shouldDataBeVisible) {
                requireNotNull(shouldUploaderBeVisible)
                assertEquals(
                    if (shouldUploaderBeVisible) TechnicalUser.Uploader.technicalUserId else null,
                    getDataMetaInfo(dataId).uploaderUserId,
                )
                dataController.getCompanyAssociatedEutaxonomyNonFinancialsData(dataId)
            } else {
                val metaInfoException = assertThrows<BackendClientException> { getDataMetaInfo(dataId) }
                assertEquals(expectedClientError403Text, metaInfoException.message)
                val dataException = assertThrows<BackendClientException> {
                    dataController.getCompanyAssociatedEutaxonomyNonFinancialsData(dataId)
                }
                assertEquals(expectedClientError403Text, dataException.message)
            }
        }
    }

    private fun waitForExpectedQaStatus(dataId: String, expectedQaStatus: BackendQaStatus) {
        await().atMost(2, TimeUnit.SECONDS).until { getDataMetaInfo(dataId).qaStatus == expectedQaStatus }
    }

    @Test
    fun `check that the review queue is correctly ordered`() {
        var expectedDataIdsInReviewQueue = emptyList<String>()

        withTechnicalUser(TechnicalUser.Uploader) {
            expectedDataIdsInReviewQueue = (1..5).map {
                val nextDataId =
                    dataController.postCompanyAssociatedEutaxonomyNonFinancialsData(dummyEuTaxoDataAlpha, false).dataId
                withTechnicalUser(TechnicalUser.Admin) {
                    await().atMost(2, TimeUnit.SECONDS).until {
                        getInfoOnUnreviewedDatasets().last().dataId == nextDataId
                    }
                }
                nextDataId
            }
        }

        withTechnicalUser(TechnicalUser.Reviewer) {
            val actualDataIdsInReviewQueue = getInfoOnUnreviewedDatasets().map { it.dataId }
            assertEquals(expectedDataIdsInReviewQueue, actualDataIdsInReviewQueue)
        }
    }

    @Test
    fun `check that an already reviewed dataset can not be assigned a different qa status`() {
        val dataId = uploadEuTaxoDataAndValidatePendingState()
        acceptDatasetAsReviewer(dataId, QaServiceQaStatus.Accepted)
        waitForExpectedQaStatus(dataId, BackendQaStatus.Accepted)
        val exception = assertThrows<QaServiceClientException> { assignQaStatus(dataId, QaServiceQaStatus.Rejected) }
        assertEquals("Client error : 400 ", exception.message)
        assertNotEquals(BackendQaStatus.Rejected, getDataMetaInfo(dataId).qaStatus)
    }

    @Test
    fun `check that dataset with review history can only be retrieved by admin reviewer and uploader of the data`() {
        val dataId = uploadEuTaxoDataAndValidatePendingState()
        acceptDatasetAsReviewer(dataId, QaServiceQaStatus.Accepted)
        waitForExpectedQaStatus(dataId, BackendQaStatus.Accepted)
        val usersWithAccessToReviewHistory = listOf(
            TechnicalUser.Admin, TechnicalUser.Reviewer,
            TechnicalUser.Uploader,
        )
        usersWithAccessToReviewHistory.forEach {
            withTechnicalUser(it) {
                assertDoesNotThrow {
                    val reviewInfo = getReviewInfoById(UUID.fromString(dataId))
                    if (it == TechnicalUser.Admin) {
                        assertEquals(TechnicalUser.Reviewer.technicalUserId, reviewInfo.reviewerKeycloakId)
                    } else {
                        assertEquals(null, reviewInfo.reviewerKeycloakId)
                    }
                }
            }
        }
        val usersWithoutAccessToReviewHistory = listOf(TechnicalUser.Reader, TechnicalUser.PremiumUser)
        usersWithoutAccessToReviewHistory.forEach {
            withTechnicalUser(it) {
                val exception = assertThrows<QaServiceClientException> { getReviewInfoById(UUID.fromString(dataId)) }
                assertEquals(expectedClientError403Text, exception.message)
            }
        }
    }

    @Test
    fun `check that a reader can access the review history of the dataset they uploaded but an uploader cant`() {
        val reader = TechnicalUser.Reader
        withTechnicalUser(TechnicalUser.Admin) {
            apiAccessor.companyRolesControllerApi.assignCompanyRole(
                CompanyRole.CompanyOwner, UUID.fromString(companyIdAlpha), UUID.fromString(reader.technicalUserId),
            )
        }

        val dataId = uploadEuTaxoDataAndValidatePendingState(reader)
        acceptDatasetAsReviewer(dataId, QaServiceQaStatus.Accepted)
        waitForExpectedQaStatus(dataId, BackendQaStatus.Accepted)

        withTechnicalUser(reader) {
            val reviewInformationResponse = getReviewInfoById(UUID.fromString(dataId))
            assertEquals(null, reviewInformationResponse.reviewerKeycloakId)
            assertEquals(dataId, reviewInformationResponse.dataId)
        }

        withTechnicalUser(TechnicalUser.Uploader) {
            val exception = assertThrows<QaServiceClientException> { getReviewInfoById(UUID.fromString(dataId)) }
            assertEquals(expectedClientError403Text, exception.message)
        }

        withTechnicalUser(TechnicalUser.Admin) {
            apiAccessor.companyRolesControllerApi.removeCompanyRole(
                CompanyRole.CompanyOwner, UUID.fromString(companyIdAlpha), UUID.fromString(reader.technicalUserId),
            )
        }
    }

    @Test
    fun `check that content of the review queue can be retrieved after a pending dataset was deleted`() {
        val dataIdAlpha = uploadEuTaxoDataAndValidatePendingState()
        val dataIdBeta = uploadEuTaxoDataAndValidatePendingState()

        withTechnicalUser(TechnicalUser.Admin) {
            await().atMost(2, TimeUnit.SECONDS)
                .until {
                    val dataIdsInQueue = getInfoOnUnreviewedDatasets().map { it.dataId }
                    dataIdsInQueue.contains(dataIdAlpha) && dataIdsInQueue.contains(dataIdBeta)
                }

            apiAccessor.dataDeletionControllerApi.deleteCompanyAssociatedData(dataIdAlpha)
            await().atMost(2, TimeUnit.SECONDS)
                .until {
                    val dataIdsInQueue = getInfoOnUnreviewedDatasets().map { it.dataId }
                    !dataIdsInQueue.contains(dataIdAlpha) && dataIdsInQueue.contains(dataIdBeta)
                }
        }
    }

    private fun getInfoOnUnreviewedDatasets(
        companyNameFilter: String? = null,
        reportingPeriodFilter: String? = null,
        dataTypeFilter: QaControllerApi.DataTypesGetInfoOnUnreviewedDatasets? = null,
    ): List<ReviewQueueResponse> {
        return qaServiceController.getInfoOnUnreviewedDatasets(
            reportingPeriods = reportingPeriodFilter?.let { setOf(it) } ?: emptySet(),
            dataTypes = dataTypeFilter?.let { listOf(it) } ?: emptyList(),
            companyName = companyNameFilter,
        )
    }

    private fun getNumberOfUnreviewedDatasets(
        companyNameFilter: String? = null,
        reportingPeriodFilter: String? = null,
        dataTypeFilter: QaControllerApi.DataTypesGetNumberOfUnreviewedDatasets? = null,
    ): Int {
        return qaServiceController.getNumberOfUnreviewedDatasets(
            reportingPeriods = reportingPeriodFilter?.let { setOf(it) } ?: emptySet(),
            dataTypes = dataTypeFilter?.let { listOf(it) } ?: emptyList(),
            companyName = companyNameFilter,
        )
    }

    private fun getDataMetaInfo(dataId: String): DataMetaInformation {
        return apiAccessor.metaDataControllerApi.getDataMetaInfo(dataId)
    }

    private fun assignQaStatus(dataId: String, qaStatus: QaStatus) {
        apiAccessor.qaServiceControllerApi.assignQaStatus(dataId, qaStatus)
    }

    private fun getReviewInfoById(dataId: UUID): ReviewInformationResponse {
        return qaServiceController.getDatasetById(dataId)
    }

    @Test
    fun `check that filtering works as expected when retrieving meta info on unreviewed datasets`() {
        val repPeriodAlpha = "abcdefgh-1"
        val repPeriodBeta = "abcdefgh-2"
        val datasetAlpha = dummyEuTaxoDataAlpha.copy(reportingPeriod = repPeriodAlpha)
        val datasetBeta = dummySfdrDataBeta.copy(reportingPeriod = repPeriodBeta)

        withTechnicalUser(TechnicalUser.Admin) {
            val dataIdAlpha = apiAccessor.dataControllerApiForEuTaxonomyNonFinancials
                .postCompanyAssociatedEutaxonomyNonFinancialsData(datasetAlpha).dataId
            val dataIdBeta =
                apiAccessor.dataControllerApiForSfdrData.postCompanyAssociatedSfdrData(datasetBeta).dataId

            await().atMost(2, TimeUnit.SECONDS).until {
                getInfoOnUnreviewedDatasets(reportingPeriodFilter = repPeriodAlpha).first().dataId == dataIdAlpha &&
                    getNumberOfUnreviewedDatasets(reportingPeriodFilter = repPeriodAlpha) == 1
            }

            await().atMost(2, TimeUnit.SECONDS).until {
                getInfoOnUnreviewedDatasets(dataTypeFilter = QaControllerApi.DataTypesGetInfoOnUnreviewedDatasets.sfdr)
                    .first().dataId == dataIdBeta &&
                    getNumberOfUnreviewedDatasets(
                        dataTypeFilter = QaControllerApi.DataTypesGetNumberOfUnreviewedDatasets.sfdr,
                    ) == 1
            }
            await().atMost(2, TimeUnit.SECONDS).until {
                getInfoOnUnreviewedDatasets(companyNameFilter = "Beta-Company-").first().dataId == dataIdBeta &&
                    getNumberOfUnreviewedDatasets(companyNameFilter = "Beta-Company-") == 1
            }
        }
    }
}
