package org.dataland.e2etests.tests

import org.awaitility.Awaitility.await
import org.dataland.communitymanager.openApiClient.model.CompanyRole
import org.dataland.datalandbackend.openApiClient.model.CompanyAssociatedDataEutaxonomyNonFinancialsData
import org.dataland.datalandbackend.openApiClient.model.CompanyAssociatedDataSfdrData
import org.dataland.datalandbackend.openApiClient.model.DataMetaInformation
import org.dataland.datalandqaservice.openApiClient.api.QaControllerApi
import org.dataland.datalandqaservice.openApiClient.model.QaReviewResponse
import org.dataland.datalandqaservice.openApiClient.model.QaStatus
import org.dataland.e2etests.auth.GlobalAuth.withTechnicalUser
import org.dataland.e2etests.auth.TechnicalUser
import org.dataland.e2etests.utils.ApiAccessor
import org.dataland.e2etests.utils.DocumentControllerApiAccessor
import org.dataland.e2etests.utils.api.ApiAwait
import org.dataland.e2etests.utils.testDataProviders.GeneralTestDataProvider
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.springframework.http.HttpStatus
import java.time.Instant
import java.util.UUID
import java.util.concurrent.TimeUnit
import org.dataland.datalandbackend.openApiClient.infrastructure.ClientException as BackendClientException
import org.dataland.datalandbackend.openApiClient.model.QaStatus as BackendQaStatus
import org.dataland.datalandqaservice.openApiClient.infrastructure.ClientException as QaServiceClientException
import org.dataland.datalandqaservice.openApiClient.model.QaStatus as QaServiceQaStatus

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class QaServiceTest {
    private val apiAccessor = ApiAccessor()
    private val documentManagerAccessor = DocumentControllerApiAccessor()
    private val dataController = apiAccessor.dataControllerApiForEuTaxonomyNonFinancials
    private val qaServiceController = apiAccessor.qaServiceControllerApi

    private lateinit var dummyEuTaxoDataAlpha: CompanyAssociatedDataEutaxonomyNonFinancialsData
    private lateinit var dummySfdrDataBeta: CompanyAssociatedDataSfdrData
    private lateinit var companyIdAlpha: String
    private lateinit var companyIdBeta: String

    private val expectedClientError403Text = "Client error : 403 "
    private val getPendingSfdrType = QaControllerApi.DataTypesGetInfoOnPendingDatasets.sfdr
    private val getNumberPendingSfdrType = QaControllerApi.DataTypesGetNumberOfPendingDatasets.sfdr

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
    fun setAllPendingDatasetsToRejected() {
        withTechnicalUser(TechnicalUser.Reviewer) {
            while (getNumberOfPendingDatasets() > 0) {
                getInfoOnPendingDatasets().forEach {
                    changeQaStatus(it.dataId, QaServiceQaStatus.Rejected)
                }
            }
            await().atMost(2, TimeUnit.SECONDS).until {
                getNumberOfPendingDatasets() == 0 && getInfoOnPendingDatasets().isEmpty()
            }
        }
    }

    @Test
    fun `post dummy data and accept it and check the qa status changes and check different users access permissions`() {
        val dataId = uploadEuTaxoDataAndValidatePendingState()
        changeQaStatusAsReviewer(dataId, QaServiceQaStatus.Accepted)
        waitForExpectedQaStatus(dataId, BackendQaStatus.Accepted)
        canUserSeeUploaderData(
            dataId = dataId,
            viewingUser = TechnicalUser.Reader,
            shouldDataBeVisible = true,
            shouldUploaderBeVisible = true,
        )
        canUserSeeUploaderData(
            dataId = dataId,
            viewingUser = TechnicalUser.Uploader,
            shouldDataBeVisible = true,
            shouldUploaderBeVisible = true,
        )
        canUserSeeUploaderData(
            dataId = dataId,
            viewingUser = TechnicalUser.Reviewer,
            shouldDataBeVisible = true,
            shouldUploaderBeVisible = true,
        )
        canUserSeeUploaderData(
            dataId = dataId,
            viewingUser = TechnicalUser.Admin,
            shouldDataBeVisible = true,
            shouldUploaderBeVisible = true,
        )
    }

    @Test
    fun `post dummy data and reject it and check the qa status changes and check different users access permissions`() {
        val dataId = uploadEuTaxoDataAndValidatePendingState()
        changeQaStatusAsReviewer(dataId, QaServiceQaStatus.Rejected)
        withTechnicalUser(TechnicalUser.Uploader) {
            waitForExpectedQaStatus(dataId = dataId, expectedQaStatus = BackendQaStatus.Rejected)
            canUserSeeUploaderData(dataId = dataId, viewingUser = TechnicalUser.Reader, shouldDataBeVisible = false)
            canUserSeeUploaderData(
                dataId = dataId,
                viewingUser = TechnicalUser.Uploader,
                shouldDataBeVisible = true,
                shouldUploaderBeVisible = true,
            )
            canUserSeeUploaderData(
                dataId = dataId,
                viewingUser = TechnicalUser.Reviewer,
                shouldDataBeVisible = true,
                shouldUploaderBeVisible = true,
            )
            canUserSeeUploaderData(
                dataId = dataId,
                viewingUser = TechnicalUser.Admin,
                shouldDataBeVisible = true,
                shouldUploaderBeVisible = true,
            )
        }
    }

    @Test
    fun `post dummy data and check different users access permissions`() {
        val dataId = uploadEuTaxoDataAndValidatePendingState()
        canUserSeeUploaderData(dataId = dataId, viewingUser = TechnicalUser.Reader, shouldDataBeVisible = false)
        canUserSeeUploaderData(
            dataId = dataId,
            viewingUser = TechnicalUser.Uploader,
            shouldDataBeVisible = true,
            shouldUploaderBeVisible = true,
        )
        canUserSeeUploaderData(
            dataId = dataId,
            viewingUser = TechnicalUser.Reviewer,
            shouldDataBeVisible = true,
            shouldUploaderBeVisible = true,
        )
        canUserSeeUploaderData(
            dataId = dataId,
            viewingUser = TechnicalUser.Admin,
            shouldDataBeVisible = true,
            shouldUploaderBeVisible = true,
        )
    }

    @Test
    fun `check that the information on pending datasets is correctly ordered`() {
        val expectedDataIdsOfPendingDatasets = mutableListOf<String>()

        withTechnicalUser(TechnicalUser.Admin) {
            (1..5).map {
                val currentDataId = postEuTaxoData(dummyEuTaxoDataAlpha).dataId
                await().atMost(2, TimeUnit.SECONDS).until {
                    getInfoOnPendingDatasets().map { it.dataId }.firstOrNull() == currentDataId
                }
                expectedDataIdsOfPendingDatasets.addFirst(currentDataId)
            }
        }

        withTechnicalUser(TechnicalUser.Reviewer) {
            val actualDataIdsOfPendingDatasets = getInfoOnPendingDatasets().map { it.dataId }
            assertEquals(expectedDataIdsOfPendingDatasets, actualDataIdsOfPendingDatasets)
        }
    }

    @Test
    fun `check that an already reviewed dataset can be assigned a different qa status`() {
        val dataId = uploadEuTaxoDataAndValidatePendingState()
        changeQaStatusAsReviewer(dataId, QaServiceQaStatus.Accepted)
        waitForExpectedQaStatus(dataId, BackendQaStatus.Accepted)
        assertDoesNotThrow { changeQaStatus(dataId, QaServiceQaStatus.Rejected) }
        await().atMost(2, TimeUnit.SECONDS).until {
            BackendQaStatus.Rejected == getDataMetaInfo(dataId).qaStatus
        }
    }

    @Test
    fun `check that dataset with review information can only be retrieved by admin reviewer and uploader of the data`() {
        val dataId = uploadEuTaxoDataAndValidatePendingState()
        changeQaStatusAsReviewer(dataId, QaServiceQaStatus.Accepted)
        waitForExpectedQaStatus(dataId, BackendQaStatus.Accepted)
        val usersWithAccessToQaReviewRepository =
            listOf(
                TechnicalUser.Admin, TechnicalUser.Reviewer,
                TechnicalUser.Uploader,
            )
        usersWithAccessToQaReviewRepository.forEach {
            withTechnicalUser(it) {
                assertDoesNotThrow {
                    val reviewInfo = getReviewInfoById(UUID.fromString(dataId))
                    if (it == TechnicalUser.Admin) {
                        assertEquals(TechnicalUser.Reviewer.technicalUserId, reviewInfo.triggeringUserId)
                    } else {
                        assertEquals(null, reviewInfo.triggeringUserId)
                    }
                }
            }
        }
        val usersWithoutAccessToQaReviewRepository = listOf(TechnicalUser.Reader, TechnicalUser.PremiumUser)
        usersWithoutAccessToQaReviewRepository.forEach {
            withTechnicalUser(it) {
                val exception = assertThrows<QaServiceClientException> { getReviewInfoById(UUID.fromString(dataId)) }
                assertEquals(expectedClientError403Text, exception.message)
            }
        }
    }

    @Test
    fun `check that a reader can access the review information of the dataset they uploaded but an uploader cannot`() {
        val reader = TechnicalUser.Reader
        withTechnicalUser(TechnicalUser.Admin) {
            apiAccessor.companyRolesControllerApi.assignCompanyRole(
                CompanyRole.CompanyOwner, UUID.fromString(companyIdAlpha), UUID.fromString(reader.technicalUserId),
            )
        }

        val dataId = uploadEuTaxoDataAndValidatePendingState(reader)
        changeQaStatusAsReviewer(dataId, QaServiceQaStatus.Accepted)
        waitForExpectedQaStatus(dataId, BackendQaStatus.Accepted)

        withTechnicalUser(reader) {
            val reviewInformationResponse = getReviewInfoById(UUID.fromString(dataId))
            assertEquals(null, reviewInformationResponse.triggeringUserId)
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
    fun `check that content of the review repository can be retrieved after a pending dataset was deleted`() {
        val dataIdAlpha = uploadEuTaxoDataAndValidatePendingState()
        val dataIdBeta = uploadEuTaxoDataAndValidatePendingState()

        withTechnicalUser(TechnicalUser.Admin) {
            await()
                .atMost(2, TimeUnit.SECONDS)
                .until {
                    val dataIdsOfPendingDatasets = getInfoOnPendingDatasets().map { it.dataId }
                    dataIdsOfPendingDatasets.contains(dataIdAlpha) && dataIdsOfPendingDatasets.contains(dataIdBeta)
                }

            apiAccessor.dataDeletionControllerApi.deleteCompanyAssociatedData(dataIdAlpha)
            await()
                .atMost(2, TimeUnit.SECONDS)
                .until {
                    val dataIdsOfPendingDatasets = getInfoOnPendingDatasets().map { it.dataId }
                    !dataIdsOfPendingDatasets.contains(dataIdAlpha) && dataIdsOfPendingDatasets.contains(dataIdBeta)
                }
        }
    }

    @Test
    fun `check that filtering works as expected when retrieving meta info on unreviewed datasets`() {
        val repPeriodAlpha = "abcdefgh-1"
        val repPeriodBeta = "abcdefgh-2"
        val datasetAlpha = dummyEuTaxoDataAlpha.copy(reportingPeriod = repPeriodAlpha)
        val datasetBeta = dummySfdrDataBeta.copy(reportingPeriod = repPeriodBeta)

        withTechnicalUser(TechnicalUser.Admin) {
            val dataIdAlpha = postEuTaxoData(datasetAlpha).dataId
            val dataIdBeta = postSfdrData(datasetBeta).dataId

            await().atMost(2, TimeUnit.SECONDS).until {
                val unreviewedDataIds = getInfoOnPendingDatasets(reportingPeriod = repPeriodAlpha).map { it.dataId }
                unreviewedDataIds.firstOrNull() == dataIdAlpha &&
                    getNumberOfPendingDatasets(reportingPeriodFilter = repPeriodAlpha) == 1
            }
            await().atMost(2, TimeUnit.SECONDS).until {
                val unreviewedDataIds = getInfoOnPendingDatasets(dataType = getPendingSfdrType).map { it.dataId }
                unreviewedDataIds.firstOrNull() == dataIdBeta &&
                    getNumberOfPendingDatasets(dataTypeFilter = getNumberPendingSfdrType) == 1
            }
            await().atMost(2, TimeUnit.SECONDS).until {
                val unreviewedDataIds = getInfoOnPendingDatasets(dataType = getPendingSfdrType).map { it.dataId }
                unreviewedDataIds.firstOrNull() == dataIdBeta &&
                    getNumberOfPendingDatasets(companyNameFilter = "Beta-Company-") == 1
            }
        }
    }

    private fun uploadEuTaxoDataAndValidatePendingState(user: TechnicalUser = TechnicalUser.Uploader): String {
        withTechnicalUser(user) {
            val dataId = postEuTaxoData(dummyEuTaxoDataAlpha).dataId
            assertEquals(BackendQaStatus.Pending, getDataMetaInfo(dataId).qaStatus)
            return dataId
        }
    }

    private fun changeQaStatusAsReviewer(
        dataId: String,
        qaStatus: QaServiceQaStatus,
    ) {
        withTechnicalUser(TechnicalUser.Reviewer) {
            val qaReviewResponse = qaServiceController.getQaReviewResponseByDataId(UUID.fromString(dataId))
            changeQaStatus(qaReviewResponse.dataId, qaStatus)

            await().atMost(2, TimeUnit.SECONDS).until {
                qaServiceController.getQaReviewResponseByDataId(UUID.fromString(dataId)).qaStatus == qaStatus
            }
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
                val dataException =
                    assertThrows<BackendClientException> {
                        dataController.getCompanyAssociatedEutaxonomyNonFinancialsData(dataId)
                    }
                assertEquals(expectedClientError403Text, dataException.message)
            }
        }
    }

    private fun waitForExpectedQaStatus(
        dataId: String,
        expectedQaStatus: BackendQaStatus,
    ) {
        await().atMost(2, TimeUnit.SECONDS).until { getDataMetaInfo(dataId).qaStatus == expectedQaStatus }
    }

    private fun getInfoOnPendingDatasets(
        companyName: String? = null,
        reportingPeriod: String? = null,
        dataType: QaControllerApi.DataTypesGetInfoOnPendingDatasets? = null,
    ): List<QaReviewResponse> =
        qaServiceController.getInfoOnPendingDatasets(
            reportingPeriods = reportingPeriod?.let { setOf(it) } ?: emptySet(),
            dataTypes = dataType?.let { listOf(it) } ?: emptyList(),
            companyName = companyName,
        )

    private fun getNumberOfPendingDatasets(
        companyNameFilter: String? = null,
        reportingPeriodFilter: String? = null,
        dataTypeFilter: QaControllerApi.DataTypesGetNumberOfPendingDatasets? = null,
    ): Int =
        qaServiceController.getNumberOfPendingDatasets(
            reportingPeriods = reportingPeriodFilter?.let { setOf(it) } ?: emptySet(),
            dataTypes = dataTypeFilter?.let { listOf(it) } ?: emptyList(),
            companyName = companyNameFilter,
        )

    private fun getDataMetaInfo(dataId: String): DataMetaInformation = apiAccessor.metaDataControllerApi.getDataMetaInfo(dataId)

    private fun changeQaStatus(
        dataId: String,
        qaStatus: QaStatus,
    ) {
        // Longer wait time to avoid flakiness in the CI pipeline due to low speed
        ApiAwait.waitForSuccess(timeoutInSeconds = 60, retryOnHttpErrors = setOf(HttpStatus.NOT_FOUND)) {
            qaServiceController.changeQaStatus(dataId, qaStatus)
        }
    }

    private fun getReviewInfoById(dataId: UUID): QaReviewResponse = qaServiceController.getQaReviewResponseByDataId(dataId)

    private fun postEuTaxoData(dataset: CompanyAssociatedDataEutaxonomyNonFinancialsData): DataMetaInformation =
        apiAccessor.dataControllerApiForEuTaxonomyNonFinancials
            .postCompanyAssociatedEutaxonomyNonFinancialsData(dataset)

    private fun postSfdrData(dataset: CompanyAssociatedDataSfdrData): DataMetaInformation =
        apiAccessor.dataControllerApiForSfdrData
            .postCompanyAssociatedSfdrData(dataset)
}
