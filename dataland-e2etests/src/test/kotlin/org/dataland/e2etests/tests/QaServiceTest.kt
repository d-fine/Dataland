package org.dataland.e2etests.tests

import org.awaitility.Awaitility.await
import org.dataland.communitymanager.openApiClient.model.CompanyRole
import org.dataland.datalandbackend.openApiClient.model.CompanyAssociatedDataEutaxonomyNonFinancialsData
import org.dataland.datalandbackend.openApiClient.model.CompanyAssociatedDataSfdrData
import org.dataland.datalandqaservice.openApiClient.api.QaControllerApi
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
    fun postCompany() {
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

    @AfterEach
    fun clearTheReviewQueue() {
        withTechnicalUser(TechnicalUser.Reviewer) {
            apiAccessor.qaServiceControllerApi.getInfoOnUnreviewedDatasets().forEach {
                apiAccessor.qaServiceControllerApi.assignQaStatus(it.dataId, QaServiceQaStatus.Rejected)
            }
        }
    }

    @Test
    fun `post dummy data and accept it and check the qa status changes and check different users access permissions`() {
        val dataId = uploadDatasetAndValidatePendingState()
        reviewDatasetAsReviewerAndAssertSuccess(dataId, QaServiceQaStatus.Accepted)
        awaitQaStatusChange(dataId, BackendQaStatus.Accepted)
        canUserSeeUploaderData(dataId, TechnicalUser.Reader, true, false)
        canUserSeeUploaderData(dataId, TechnicalUser.Uploader, true, true)
        canUserSeeUploaderData(dataId, TechnicalUser.Reviewer, true, false)
        canUserSeeUploaderData(dataId, TechnicalUser.Admin, true, true)
    }

    @Test
    fun `post dummy data and reject it and check the qa status changes and check different users access permissions`() {
        val dataId = uploadDatasetAndValidatePendingState()
        reviewDatasetAsReviewerAndAssertSuccess(dataId, QaServiceQaStatus.Rejected)
        withTechnicalUser(TechnicalUser.Uploader) {
            awaitQaStatusChange(dataId, BackendQaStatus.Rejected)
            canUserSeeUploaderData(dataId, TechnicalUser.Reader, false)
            canUserSeeUploaderData(dataId, TechnicalUser.Uploader, true, true)
            canUserSeeUploaderData(dataId, TechnicalUser.Reviewer, true, false)
            canUserSeeUploaderData(dataId, TechnicalUser.Admin, true, true)
        }
    }

    @Test
    fun `post dummy data and check different users access permissions`() {
        val dataId = uploadDatasetAndValidatePendingState()
        canUserSeeUploaderData(dataId, TechnicalUser.Reader, false)
        canUserSeeUploaderData(dataId, TechnicalUser.Uploader, true, true)
        canUserSeeUploaderData(dataId, TechnicalUser.Reviewer, true, false)
        canUserSeeUploaderData(dataId, TechnicalUser.Admin, true, true)
    }

    private fun uploadDatasetAndValidatePendingState(user: TechnicalUser = TechnicalUser.Uploader): String {
        withTechnicalUser(user) {
            val dataId =
                dataController.postCompanyAssociatedEutaxonomyNonFinancialsData(dummyEuTaxoDataAlpha, false).dataId
            assertEquals(BackendQaStatus.Pending, apiAccessor.metaDataControllerApi.getDataMetaInfo(dataId).qaStatus)
            return dataId
        }
    }

    private fun reviewDatasetAsReviewerAndAssertSuccess(dataId: String, qaStatus: QaServiceQaStatus) {
        withTechnicalUser(TechnicalUser.Reviewer) {
            val qaServiceController = apiAccessor.qaServiceControllerApi
            await().atMost(2, TimeUnit.SECONDS)
                .until { qaServiceController.getInfoOnUnreviewedDatasets().map { it.dataId }.contains(dataId) }
            qaServiceController.assignQaStatus(dataId, qaStatus)
            assertFalse(qaServiceController.getInfoOnUnreviewedDatasets().map { it.dataId }.contains(dataId))
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
                    apiAccessor.metaDataControllerApi.getDataMetaInfo(dataId).uploaderUserId,
                )
                dataController.getCompanyAssociatedEutaxonomyNonFinancialsData(dataId)
            } else {
                val metaInfoException = assertThrows<BackendClientException> {
                    apiAccessor.metaDataControllerApi.getDataMetaInfo(dataId)
                }
                assertEquals(expectedClientError403Text, metaInfoException.message)
                val dataException = assertThrows<BackendClientException> {
                    dataController.getCompanyAssociatedEutaxonomyNonFinancialsData(dataId)
                }
                assertEquals(expectedClientError403Text, dataException.message)
            }
        }
    }

    private fun awaitQaStatusChange(dataId: String, expectedQaStatus: BackendQaStatus) {
        await().atMost(2, TimeUnit.SECONDS)
            .until { apiAccessor.metaDataControllerApi.getDataMetaInfo(dataId).qaStatus == expectedQaStatus }
    }

    @Test
    fun `check that the review queue is correctly ordered`() {
        var expectedDataIdsInReviewQueue = emptyList<String>()

        withTechnicalUser(TechnicalUser.Uploader) {
            val numberOfUploadedDatasets = 10
            expectedDataIdsInReviewQueue = (1..numberOfUploadedDatasets).map {
                dataController.postCompanyAssociatedEutaxonomyNonFinancialsData(dummyEuTaxoDataAlpha, false).dataId
            }
        }

        withTechnicalUser(TechnicalUser.Reviewer) {
            val actualDataIdsInReviewQueue =
                apiAccessor.qaServiceControllerApi.getInfoOnUnreviewedDatasets().map { it.dataId }
            assertEquals(expectedDataIdsInReviewQueue, actualDataIdsInReviewQueue)
        }
    }

    @Test
    fun `check that an already reviewed dataset can not be assigned a different qa status`() {
        val dataId = uploadDatasetAndValidatePendingState()
        reviewDatasetAsReviewerAndAssertSuccess(dataId, QaServiceQaStatus.Accepted)
        awaitQaStatusChange(dataId, BackendQaStatus.Accepted)
        val exception = assertThrows<QaServiceClientException> {
            apiAccessor.qaServiceControllerApi.assignQaStatus(dataId, QaServiceQaStatus.Rejected)
        }
        assertEquals("Client error : 400 ", exception.message)
        assertNotEquals(
            BackendQaStatus.Rejected,
            apiAccessor.metaDataControllerApi.getDataMetaInfo(dataId).qaStatus,
        )
    }

    @Test
    fun `check the a data set with review history can only retrieved by admin reviewer and uploader of the data`() {
        val dataId = uploadDatasetAndValidatePendingState()
        reviewDatasetAsReviewerAndAssertSuccess(dataId, QaServiceQaStatus.Accepted)
        awaitQaStatusChange(dataId, BackendQaStatus.Accepted)
        val usersWithAccessToReviewHistory = listOf(
            TechnicalUser.Admin, TechnicalUser.Reviewer,
            TechnicalUser.Uploader,
        )
        usersWithAccessToReviewHistory.forEach {
            withTechnicalUser(it) {
                assertDoesNotThrow {
                    val reviewInformationResponse =
                        apiAccessor.qaServiceControllerApi.getDatasetById(UUID.fromString(dataId))
                    if (it == TechnicalUser.Admin) {
                        assertEquals(
                            TechnicalUser.Reviewer.technicalUserId,
                            reviewInformationResponse.reviewerKeycloakId,
                        )
                    } else {
                        assertEquals(null, reviewInformationResponse.reviewerKeycloakId)
                    }
                }
            }
        }
        val usersWithoutAccessToReviewHistory = listOf(TechnicalUser.Reader, TechnicalUser.PremiumUser)
        usersWithoutAccessToReviewHistory.forEach {
            withTechnicalUser(it) {
                val exception = assertThrows<QaServiceClientException> {
                    apiAccessor.qaServiceControllerApi.getDatasetById(UUID.fromString(dataId))
                }
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

        val dataId = uploadDatasetAndValidatePendingState(reader)
        reviewDatasetAsReviewerAndAssertSuccess(dataId, QaServiceQaStatus.Accepted)
        awaitQaStatusChange(dataId, BackendQaStatus.Accepted)

        withTechnicalUser(reader) {
            val reviewInformationResponse = apiAccessor.qaServiceControllerApi.getDatasetById(UUID.fromString(dataId))
            assertEquals(null, reviewInformationResponse.reviewerKeycloakId)
            assertEquals(dataId, reviewInformationResponse.dataId)
        }

        withTechnicalUser(TechnicalUser.Uploader) {
            val exception = assertThrows<QaServiceClientException> {
                apiAccessor.qaServiceControllerApi.getDatasetById(UUID.fromString(dataId))
            }
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
        val dataIdAlpha = uploadDatasetAndValidatePendingState()
        val dataIdBeta = uploadDatasetAndValidatePendingState()
        withTechnicalUser(TechnicalUser.Admin) {
            val qaServiceController = apiAccessor.qaServiceControllerApi
            await().atMost(2, TimeUnit.SECONDS)
                .until { qaServiceController.getInfoOnUnreviewedDatasets().map { it.dataId }.contains(dataIdAlpha) }
            await().atMost(2, TimeUnit.SECONDS)
                .until { qaServiceController.getInfoOnUnreviewedDatasets().map { it.dataId }.contains(dataIdBeta) }
            val dataDeletionControllerApi = apiAccessor.dataDeletionControllerApi
            dataDeletionControllerApi.deleteCompanyAssociatedData(dataIdAlpha)
            await().atMost(2, TimeUnit.SECONDS)
                .until {
                    !qaServiceController.getInfoOnUnreviewedDatasets().map { it.dataId }.contains(dataIdAlpha) &&
                        qaServiceController.getInfoOnUnreviewedDatasets().map { it.dataId }.contains(dataIdBeta)
                }
        }
    }

    @Test
    fun `check that filtering works as expected when retrieving meta info on unreviewed datasets`() {
        val datasetAlpha = dummyEuTaxoDataAlpha.copy(reportingPeriod = "abcdefgh-1")
        val datasetBeta = dummySfdrDataBeta.copy(reportingPeriod = "abcdefgh-2")

        withTechnicalUser(TechnicalUser.Admin) {
            val dataIdAlpha = apiAccessor.dataControllerApiForEuTaxonomyNonFinancials
                .postCompanyAssociatedEutaxonomyNonFinancialsData(datasetAlpha).dataId
            val dataIdBeta =
                apiAccessor.dataControllerApiForSfdrData.postCompanyAssociatedSfdrData(datasetBeta).dataId

            await().atMost(2, TimeUnit.SECONDS).until {
                qaServiceController.getInfoOnUnreviewedDatasets(reportingPeriod = setOf("abcdefgh-1"))
                    .map { it.dataId }.first() == dataIdAlpha &&
                    qaServiceController.getNumberOfUnreviewedDatasets(reportingPeriod = setOf("abcdefgh-1")) == 1
            }
            await().atMost(2, TimeUnit.SECONDS).until {
                qaServiceController.getInfoOnUnreviewedDatasets(
                    dataType = listOf(
                        QaControllerApi.DataTypeGetInfoOnUnreviewedDatasets.sfdr,
                    ),
                )
                    .map { it.dataId }.first() == dataIdBeta &&
                    qaServiceController.getNumberOfUnreviewedDatasets(
                        dataType = listOf(
                            QaControllerApi.DataTypeGetNumberOfUnreviewedDatasets.sfdr,
                        ),
                    ) == 1
            }
            await().atMost(2, TimeUnit.SECONDS).until {
                qaServiceController.getInfoOnUnreviewedDatasets(companyName = "Beta-Company-")
                    .map { it.dataId }.first() == dataIdBeta &&
                    qaServiceController.getNumberOfUnreviewedDatasets(
                        companyName = "Beta-Company-",
                    ) == 1
            }
        }
    }

    // TODO aufräumen (repetitiver und unübersichtlicher Code!)
}
