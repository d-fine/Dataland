package org.dataland.e2etests.tests

import org.awaitility.Awaitility.await
import org.dataland.datalandbackend.openApiClient.infrastructure.ClientException
import org.dataland.datalandbackend.openApiClient.model.CompanyAssociatedDataEuTaxonomyDataForNonFinancials
import org.dataland.e2etests.auth.TechnicalUser
import org.dataland.e2etests.utils.ApiAccessor
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import java.util.*
import java.util.concurrent.TimeUnit
import org.dataland.datalandbackend.openApiClient.model.QaStatus as BackendQaStatus
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
    fun `post dummy data, accept it, check the qa status changes and check different users access permissions`() {
        val acceptedDataId = uploadDatasetAndValidatePendingState()
        reviewDatasetAndValidateItIsNotReviewable(acceptedDataId, QaServiceQaStatus.accepted)
        awaitQaStatusChange(acceptedDataId, BackendQaStatus.accepted)
        canUserSeeUploaderData(acceptedDataId, TechnicalUser.Reader, true, false)
        canUserSeeUploaderData(acceptedDataId, TechnicalUser.Uploader, true, true)
        canUserSeeUploaderData(acceptedDataId, TechnicalUser.Reviewer, true, false)
        canUserSeeUploaderData(acceptedDataId, TechnicalUser.Admin, true, true)
    }

    @Test
    fun `post dummy data, reject it, check the qa status changes and check different users access permissions`() {
        val rejectedDataId = uploadDatasetAndValidatePendingState()
        reviewDatasetAndValidateItIsNotReviewable(rejectedDataId, QaServiceQaStatus.rejected)
        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Uploader)
        awaitQaStatusChange(rejectedDataId, BackendQaStatus.rejected)
        canUserSeeUploaderData(rejectedDataId, TechnicalUser.Reader, false)
        canUserSeeUploaderData(rejectedDataId, TechnicalUser.Uploader, true, true)
        canUserSeeUploaderData(rejectedDataId, TechnicalUser.Reviewer, false)
        canUserSeeUploaderData(rejectedDataId, TechnicalUser.Admin, true, true)
    }

    @Test
    fun `post dummy data and check different users access permissions`() {
        val pendingDataId = uploadDatasetAndValidatePendingState()
        canUserSeeUploaderData(pendingDataId, TechnicalUser.Reader, false)
        canUserSeeUploaderData(pendingDataId, TechnicalUser.Uploader, true, true)
        canUserSeeUploaderData(pendingDataId, TechnicalUser.Reviewer, true, false)
        canUserSeeUploaderData(pendingDataId, TechnicalUser.Admin, true, true)
    }

    private fun uploadDatasetAndValidatePendingState(): String {
        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Uploader)
        val dataId = dataController.postCompanyAssociatedEuTaxonomyDataForNonFinancials(
            dummyCompanyAssociatedData, false,
        ).dataId
        assertEquals(
            BackendQaStatus.pending,
            apiAccessor.metaDataControllerApi.getDataMetaInfo(dataId).qaStatus
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
        if(shouldDataBeVisible) {
            require(shouldUploaderBeVisible != null)
            assertEquals(
                if (shouldUploaderBeVisible) TechnicalUser.Uploader.technicalUserId else null,
                apiAccessor.metaDataControllerApi.getDataMetaInfo(dataId).uploaderUserId,
            )
            dataController.getCompanyAssociatedEuTaxonomyDataForNonFinancials(dataId)
        } else {
            val metaInfoException = assertThrows<ClientException> {
                apiAccessor.metaDataControllerApi.getDataMetaInfo(dataId)
            }
            assertEquals("Client error : 403 ", metaInfoException.message)
            val dataException = assertThrows<ClientException> {
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
        println(reviewQueue)
        println(listOfDataIdsAsExpectedFromReviewQueue)
        assertTrue(listOfDataIdsAsExpectedFromReviewQueue.toTypedArray().contentDeepEquals(reviewQueue.toTypedArray()))
    }

    private fun clearReviewQueue() {
        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reviewer)
        apiAccessor.qaServiceControllerApi.getUnreviewedDatasetsIds().forEach {
            apiAccessor.qaServiceControllerApi.assignQualityStatus(it, QaServiceQaStatus.rejected)
        }
    }
}
