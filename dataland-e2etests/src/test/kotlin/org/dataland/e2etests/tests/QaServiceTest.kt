package org.dataland.e2etests.tests

import org.awaitility.Awaitility.await
import org.dataland.datalandbackend.openApiClient.model.CompanyAssociatedDataEuTaxonomyDataForNonFinancials
import org.dataland.e2etests.auth.TechnicalUser
import org.dataland.e2etests.utils.ApiAccessor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import java.util.concurrent.TimeUnit
import org.dataland.datalandbackend.openApiClient.infrastructure.ClientException as BackendClientException
import org.dataland.datalandbackend.openApiClient.model.QaStatus as BackendQaStatus
import org.dataland.datalandqaservice.openApiClient.infrastructure.ClientException as QaServiceClientException
import org.dataland.datalandqaservice.openApiClient.model.QaStatus as QaServiceQaStatus

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class QaServiceTest {
    private val apiAccessor = ApiAccessor()
    private val dataController = apiAccessor.dataControllerApiForEuTaxonomyNonFinancials
    lateinit var dummyCompanyAssociatedData: CompanyAssociatedDataEuTaxonomyDataForNonFinancials

    @BeforeAll
    fun postCompany() {
        val testCompanyInformation = apiAccessor.testDataProviderForEuTaxonomyDataForNonFinancials
            .getCompanyInformationWithoutIdentifiers(1).first()
        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Uploader)
        val storedCompanyInfos = apiAccessor.companyDataControllerApi.postCompany(testCompanyInformation)
        val testDataEuTaxonomyNonFinancials = apiAccessor.testDataProviderForEuTaxonomyDataForNonFinancials
            .getTData(1).first()
        dummyCompanyAssociatedData =
            CompanyAssociatedDataEuTaxonomyDataForNonFinancials(
                storedCompanyInfos.companyId,
                "",
                testDataEuTaxonomyNonFinancials,
            )
    }

    @Test
    fun `post dummy data and accept it and check the qa status changes and check different users access permissions`() {
        val dataId = uploadDatasetAndValidatePendingState()
        reviewDatasetAndValidateItIsNotReviewable(dataId, QaServiceQaStatus.accepted)
        awaitQaStatusChange(dataId, BackendQaStatus.accepted)
        canUserSeeUploaderData(dataId, TechnicalUser.Reader, true, false)
        canUserSeeUploaderData(dataId, TechnicalUser.Uploader, true, true)
        canUserSeeUploaderData(dataId, TechnicalUser.Reviewer, true, false)
        canUserSeeUploaderData(dataId, TechnicalUser.Admin, true, true)
    }

    @Test
    fun `post dummy data and reject it and check the qa status changes and check different users access permissions`() {
        val dataId = uploadDatasetAndValidatePendingState()
        reviewDatasetAndValidateItIsNotReviewable(dataId, QaServiceQaStatus.rejected)
        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Uploader)
        awaitQaStatusChange(dataId, BackendQaStatus.rejected)
        canUserSeeUploaderData(dataId, TechnicalUser.Reader, false)
        canUserSeeUploaderData(dataId, TechnicalUser.Uploader, true, true)
        canUserSeeUploaderData(dataId, TechnicalUser.Reviewer, false)
        canUserSeeUploaderData(dataId, TechnicalUser.Admin, true, true)
    }

    @Test
    fun `post dummy data and check different users access permissions`() {
        val dataId = uploadDatasetAndValidatePendingState()
        canUserSeeUploaderData(dataId, TechnicalUser.Reader, false)
        canUserSeeUploaderData(dataId, TechnicalUser.Uploader, true, true)
        canUserSeeUploaderData(dataId, TechnicalUser.Reviewer, true, false)
        canUserSeeUploaderData(dataId, TechnicalUser.Admin, true, true)
    }

    private fun uploadDatasetAndValidatePendingState(): String {
        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Uploader)
        val dataId = dataController.postCompanyAssociatedEuTaxonomyDataForNonFinancials(
            dummyCompanyAssociatedData, false,
        ).dataId
        assertEquals(
            BackendQaStatus.pending,
            apiAccessor.metaDataControllerApi.getDataMetaInfo(dataId).qaStatus,
        )
        return dataId
    }

    private fun reviewDatasetAndValidateItIsNotReviewable(dataId: String, qaStatus: QaServiceQaStatus) {
        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reviewer)
        val qaServiceController = apiAccessor.qaServiceControllerApi
        await().atMost(2, TimeUnit.SECONDS)
            .until { qaServiceController.getUnreviewedDatasetsIds().contains(dataId) }
        qaServiceController.assignQualityStatus(dataId, qaStatus)
        assertFalse(qaServiceController.getUnreviewedDatasetsIds().contains(dataId))
    }

    private fun canUserSeeUploaderData(
        dataId: String,
        viewingUser: TechnicalUser,
        shouldDataBeVisible: Boolean,
        shouldUploaderBeVisible: Boolean? = null,
    ) {
        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(viewingUser)
        if (shouldDataBeVisible) {
            requireNotNull(shouldUploaderBeVisible)
            assertEquals(
                if (shouldUploaderBeVisible) TechnicalUser.Uploader.technicalUserId else null,
                apiAccessor.metaDataControllerApi.getDataMetaInfo(dataId).uploaderUserId,
            )
            dataController.getCompanyAssociatedEuTaxonomyDataForNonFinancials(dataId)
        } else {
            val metaInfoException = assertThrows<BackendClientException> {
                apiAccessor.metaDataControllerApi.getDataMetaInfo(dataId)
            }
            assertEquals("Client error : 403 ", metaInfoException.message)
            val dataException = assertThrows<BackendClientException> {
                dataController.getCompanyAssociatedEuTaxonomyDataForNonFinancials(dataId)
            }
            assertEquals("Client error : 403 ", dataException.message)
        }
    }

    private fun awaitQaStatusChange(dataId: String, expectedQaStatus: BackendQaStatus) {
        await().atMost(2, TimeUnit.SECONDS)
            .until { apiAccessor.metaDataControllerApi.getDataMetaInfo(dataId).qaStatus == expectedQaStatus }
    }

    @Test
    fun `check that the review queue is correctly ordered`() {
        clearReviewQueue()
        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Uploader)
        val numberOfUploadedDatasets = 20
        val listOfDataIdsAsExpectedFromReviewQueue = (1..numberOfUploadedDatasets).map {
            dataController.postCompanyAssociatedEuTaxonomyDataForNonFinancials(
                dummyCompanyAssociatedData, false,
            ).dataId
        }
        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reviewer)
        val reviewQueue = apiAccessor.qaServiceControllerApi.getUnreviewedDatasetsIds()
        assertTrue(listOfDataIdsAsExpectedFromReviewQueue.toTypedArray().contentDeepEquals(reviewQueue.toTypedArray()))
    }

    private fun clearReviewQueue() {
        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reviewer)
        apiAccessor.qaServiceControllerApi.getUnreviewedDatasetsIds().forEach {
            apiAccessor.qaServiceControllerApi.assignQualityStatus(it, QaServiceQaStatus.rejected)
        }
    }

    @Test
    fun `check that an already reviewed dataset can not be assigned a different qa status`() {
        val dataId = uploadDatasetAndValidatePendingState()
        reviewDatasetAndValidateItIsNotReviewable(dataId, QaServiceQaStatus.accepted)
        awaitQaStatusChange(dataId, BackendQaStatus.accepted)
        val exception = assertThrows<QaServiceClientException> {
            apiAccessor.qaServiceControllerApi.assignQualityStatus(dataId, QaServiceQaStatus.rejected)
        }
        assertEquals("Client error : 400 ", exception.message)
        assertNotEquals(
            BackendQaStatus.rejected,
            apiAccessor.metaDataControllerApi.getDataMetaInfo(dataId).qaStatus,
        )
    }
}
