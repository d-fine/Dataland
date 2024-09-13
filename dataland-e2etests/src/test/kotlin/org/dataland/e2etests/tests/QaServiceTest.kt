package org.dataland.e2etests.tests
//TODO reactive the tests
/*
import org.awaitility.Awaitility.await
import org.dataland.communitymanager.openApiClient.model.CompanyRole
import org.dataland.datalandbackend.openApiClient.model.CompanyAssociatedDataEutaxonomyNonFinancialsData
import org.dataland.e2etests.auth.GlobalAuth.withTechnicalUser
import org.dataland.e2etests.auth.TechnicalUser
import org.dataland.e2etests.utils.ApiAccessor
import org.dataland.e2etests.utils.DocumentManagerAccessor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import java.util.*
import java.util.concurrent.TimeUnit
import org.dataland.datalandbackend.openApiClient.infrastructure.ClientException as BackendClientException
import org.dataland.datalandbackend.openApiClient.model.QaStatus as BackendQaStatus
import org.dataland.datalandqaservice.openApiClient.infrastructure.ClientException as QaServiceClientException
import org.dataland.datalandqaservice.openApiClient.model.QaStatus as QaServiceQaStatus

private const val CLIENT_ERROR_403 = "Client error : 403 "

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class QaServiceTest {
    private val apiAccessor = ApiAccessor()
    private val documentManagerAccessor = DocumentManagerAccessor()
    private val dataController = apiAccessor.dataControllerApiForEuTaxonomyNonFinancials
    private lateinit var dummyCompanyAssociatedData: CompanyAssociatedDataEutaxonomyNonFinancialsData
    companion object {
        private const val SLEEP_DURATION_MS: Long = 500
    }

    @BeforeAll
    fun postCompany() {
        documentManagerAccessor.uploadAllTestDocumentsAndAssurePersistence()
        val testCompanyInformation = apiAccessor.testDataProviderForEuTaxonomyDataForNonFinancials
            .getCompanyInformationWithoutIdentifiers(1).first()
        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        val storedCompanyInfos = apiAccessor.companyDataControllerApi.postCompany(testCompanyInformation)
        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Uploader)
        val testDataEuTaxonomyNonFinancials = apiAccessor.testDataProviderForEuTaxonomyDataForNonFinancials
            .getTData(1).first()
        dummyCompanyAssociatedData =
            CompanyAssociatedDataEutaxonomyNonFinancialsData(
                storedCompanyInfos.companyId,
                "",
                testDataEuTaxonomyNonFinancials,
            )
    }

    @Test
    fun `post dummy data and accept it and check the qa status changes and check different users access permissions`() {
        val dataId = uploadDatasetAndValidatePendingState()
        reviewDatasetAndValidateItIsNotReviewable(dataId, QaServiceQaStatus.Accepted)
        awaitQaStatusChange(dataId, BackendQaStatus.Accepted)
        canUserSeeUploaderData(dataId, TechnicalUser.Reader, true, false)
        canUserSeeUploaderData(dataId, TechnicalUser.Uploader, true, true)
        canUserSeeUploaderData(dataId, TechnicalUser.Reviewer, true, false)
        canUserSeeUploaderData(dataId, TechnicalUser.Admin, true, true)
    }

    @Test
    fun `post dummy data and reject it and check the qa status changes and check different users access permissions`() {
        val dataId = uploadDatasetAndValidatePendingState()
        reviewDatasetAndValidateItIsNotReviewable(dataId, QaServiceQaStatus.Rejected)
        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Uploader)
        awaitQaStatusChange(dataId, BackendQaStatus.Rejected)
        canUserSeeUploaderData(dataId, TechnicalUser.Reader, false)
        canUserSeeUploaderData(dataId, TechnicalUser.Uploader, true, true)
        canUserSeeUploaderData(dataId, TechnicalUser.Reviewer, true, false)
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

    private fun uploadDatasetAndValidatePendingState(user: TechnicalUser = TechnicalUser.Uploader): String {
        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(user)
        val dataId = dataController.postCompanyAssociatedEutaxonomyNonFinancialsData(
            dummyCompanyAssociatedData, false,
        ).dataId
        assertEquals(
            BackendQaStatus.Pending,
            apiAccessor.metaDataControllerApi.getDataMetaInfo(dataId).qaStatus,
        )
        return dataId
    }

    private fun reviewDatasetAndValidateItIsNotReviewable(dataId: String, qaStatus: QaServiceQaStatus) {
        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reviewer)
        val qaServiceController = apiAccessor.qaServiceControllerApi
        await().atMost(2, TimeUnit.SECONDS)
            .until { qaServiceController.getUnreviewedMetadataSets().contains(dataId) }
        qaServiceController.assignQaStatus(dataId, qaStatus)
        assertFalse(qaServiceController.getUnreviewedMetadataSets().contains(dataId))
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
            dataController.getCompanyAssociatedEutaxonomyNonFinancialsData(dataId)
        } else {
            val metaInfoException = assertThrows<BackendClientException> {
                apiAccessor.metaDataControllerApi.getDataMetaInfo(dataId)
            }
            assertEquals(CLIENT_ERROR_403, metaInfoException.message)
            val dataException = assertThrows<BackendClientException> {
                dataController.getCompanyAssociatedEutaxonomyNonFinancialsData(dataId)
            }
            assertEquals(CLIENT_ERROR_403, dataException.message)
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
            dataController.postCompanyAssociatedEutaxonomyNonFinancialsData(
                dummyCompanyAssociatedData, false,
            ).dataId.also { Thread.sleep(SLEEP_DURATION_MS) }
        }
        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reviewer)
        val reviewQueue = apiAccessor.qaServiceControllerApi.getUnreviewedMetadataSets()

        assertTrue(listOfDataIdsAsExpectedFromReviewQueue.toTypedArray().contentDeepEquals(reviewQueue.toTypedArray()))
    }

    private fun clearReviewQueue() {
        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reviewer)
        apiAccessor.qaServiceControllerApi.getUnreviewedMetadataSets().forEach {
            apiAccessor.qaServiceControllerApi.assignQaStatus(it, QaServiceQaStatus.Rejected)
        }
    }

    @Test
    fun `check that an already reviewed dataset can not be assigned a different qa status`() {
        val dataId = uploadDatasetAndValidatePendingState()
        reviewDatasetAndValidateItIsNotReviewable(dataId, QaServiceQaStatus.Accepted)
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
        reviewDatasetAndValidateItIsNotReviewable(dataId, QaServiceQaStatus.Accepted)
        awaitQaStatusChange(dataId, BackendQaStatus.Accepted)
        val usersWithAccessToReviewHistory = listOf(
            TechnicalUser.Admin, TechnicalUser.Reviewer,
            TechnicalUser.Uploader,
        )
        usersWithAccessToReviewHistory.forEach {
            apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(it)
            assertDoesNotThrow {
                val reviewInformationResponse =
                    apiAccessor.qaServiceControllerApi.getDatasetById(UUID.fromString(dataId))
                if (it == TechnicalUser.Admin) {
                    assertEquals(TechnicalUser.Reviewer.technicalUserId, reviewInformationResponse.reviewerKeycloakId)
                } else {
                    assertEquals(null, reviewInformationResponse.reviewerKeycloakId)
                }
            }
        }
        val usersWithoutAccessToReviewHistory = listOf(TechnicalUser.Reader, TechnicalUser.PremiumUser)
        usersWithoutAccessToReviewHistory.forEach {
            apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(it)
            val exception = assertThrows<QaServiceClientException> {
                apiAccessor.qaServiceControllerApi.getDatasetById(UUID.fromString(dataId))
            }
            assertEquals(CLIENT_ERROR_403, exception.message)
        }
    }

    @Test
    fun `check the a reader can access the review history of the data set he uploaded but an uploader cant`() {
        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        apiAccessor.companyRolesControllerApi.assignCompanyRole(
            CompanyRole.CompanyOwner, UUID.fromString(dummyCompanyAssociatedData.companyId),
            UUID.fromString(TechnicalUser.Reader.technicalUserId),
        )
        val dataId = uploadDatasetAndValidatePendingState(TechnicalUser.Reader)
        reviewDatasetAndValidateItIsNotReviewable(dataId, QaServiceQaStatus.Accepted)
        awaitQaStatusChange(dataId, BackendQaStatus.Accepted)
        val reviewInformationResponse =
            apiAccessor.qaServiceControllerApi.getDatasetById(UUID.fromString(dataId))
        assertEquals(null, reviewInformationResponse.reviewerKeycloakId)
        assertEquals(dataId, reviewInformationResponse.dataId)

        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Uploader)
        val exception = assertThrows<QaServiceClientException> {
            apiAccessor.qaServiceControllerApi.getDatasetById(UUID.fromString(dataId))
        }
        assertEquals(CLIENT_ERROR_403, exception.message)
    }

    @Test
    fun `check that content of the review queue can be retrieved after a pending dataset was deleted`() {
        val dataIdAlpha = uploadDatasetAndValidatePendingState()
        val dataIdBeta = uploadDatasetAndValidatePendingState()
        withTechnicalUser(TechnicalUser.Admin) {
            val qaServiceController = apiAccessor.qaServiceControllerApi
            await().atMost(2, TimeUnit.SECONDS)
                .until { qaServiceController.getUnreviewedMetadataSets().contains(dataIdAlpha) }
            await().atMost(2, TimeUnit.SECONDS)
                .until { qaServiceController.getUnreviewedMetadataSets().contains(dataIdBeta) }
            val dataDeletionControllerApi = apiAccessor.dataDeletionControllerApi
            dataDeletionControllerApi.deleteCompanyAssociatedData(dataIdAlpha)
            await().atMost(2, TimeUnit.SECONDS)
                .until {
                    !qaServiceController.getUnreviewedMetadataSets().contains(dataIdAlpha) &&
                        qaServiceController.getUnreviewedMetadataSets().contains(dataIdBeta)
                }
        }
    }
}
*/
