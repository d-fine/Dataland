package org.dataland.e2etests.tests

import org.dataland.communitymanager.openApiClient.infrastructure.ClientError
import org.dataland.communitymanager.openApiClient.infrastructure.ClientException
import org.dataland.communitymanager.openApiClient.model.CompanyDataOwners
import org.dataland.datalandbackend.openApiClient.model.EutaxonomyNonFinancialsData
import org.dataland.e2etests.auth.GlobalAuth
import org.dataland.e2etests.auth.JwtAuthenticationHelper
import org.dataland.e2etests.auth.TechnicalUser
import org.dataland.e2etests.utils.ApiAccessor
import org.dataland.e2etests.utils.DocumentManagerAccessor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import java.util.UUID
import org.dataland.communitymanager.openApiClient.infrastructure.ClientException as CommunityManagerClientException
import org.dataland.datalandbackend.openApiClient.infrastructure.ClientException as BackendClientException

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DataOwnerControllerTest {
    private val apiAccessor = ApiAccessor()
    private val documentManagerAccessor = DocumentManagerAccessor()
    val jwtHelper = JwtAuthenticationHelper()

    private val dataReaderUserId = UUID.fromString("18b67ecc-1176-4506-8414-1e81661017ca")
    private val frameworkSampleData = apiAccessor.testDataProviderForEuTaxonomyDataForNonFinancials
        .getTData(1)[0]

    @BeforeAll
    fun postRequiredDummyDocuments() {
        documentManagerAccessor.uploadAllTestDocumentsAndAssurePersistence()
    }

    private fun removeBearerTokenFromApiClients() {
        GlobalAuth.setBearerToken(null)
    }

    private fun validateDataOwnersForCompany(
        companyId: UUID,
        expectedDataOwnerUserIds: List<UUID>,
        actualCompanyDataOwnersResponse: CompanyDataOwners,
    ) {
        assertEquals(companyId.toString(), actualCompanyDataOwnersResponse.companyId)

        val actualDataOwners = actualCompanyDataOwnersResponse.dataOwners
        assertEquals(expectedDataOwnerUserIds.size, actualDataOwners.size)
        expectedDataOwnerUserIds.map { it.toString() }.forEach { assertTrue(actualDataOwners.contains(it)) }
    }

    private fun assertAccessDeniedWhenUploadingFrameworkData(
        companyId: UUID,
        dataSet: EutaxonomyNonFinancialsData,
        bypassQa: Boolean = false,
    ) {
        val reportingPeriod = "2022"
        val expectedAccessDeniedClientException = assertThrows<BackendClientException> {
            apiAccessor.euTaxonomyNonFinancialsUploaderFunction(
                companyId.toString(),
                dataSet,
                reportingPeriod,
                bypassQa,
            )
        }
        assertEquals("Client error : 403 ", expectedAccessDeniedClientException.message)
    }

    private fun uploadEuTaxoData(companyId: UUID, dataSet: EutaxonomyNonFinancialsData) {
        val reportingPeriod = "2022"
        apiAccessor.euTaxonomyNonFinancialsUploaderFunction(
            companyId.toString(),
            dataSet,
            reportingPeriod,
            false,
        )
    }

    private fun assertErrorCodeInCommunityManagerClientException(
        communityManagerClientException: CommunityManagerClientException,
        expectedErrorCode: Number,
    ) {
        assertEquals("Client error : $expectedErrorCode ", communityManagerClientException.message)
    }

    private fun assertCompanyNotFoundResponseBodyInCommunityManagerClientException(
        communityManagerClientException: CommunityManagerClientException,
        companyId: UUID,
    ) {
        assertErrorCodeInCommunityManagerClientException(communityManagerClientException, 404)
        val responseBody = (communityManagerClientException.response as ClientError<*>).body as String
        assertTrue(responseBody.contains("Company not found"))
        assertTrue(
            responseBody.contains(
                "\"Dataland does not know the company ID $companyId\"",
            ),
        )
    }

    private fun assertAccessDeniedResponseBodyInCommunityManagerClientException(
        communityManagerClientException: CommunityManagerClientException,
    ) {
        assertErrorCodeInCommunityManagerClientException(communityManagerClientException, 403)
        val responseBody = (communityManagerClientException.response as ClientError<*>).body as String
        assertTrue(responseBody.contains("Access Denied"))
    }

    @Test
    fun `check that data ownership enables a user with only reader rights to upload data`() {
        val firstCompanyId = UUID.fromString(
            apiAccessor.uploadOneCompanyWithRandomIdentifier().actualStoredCompany.companyId,
        )
        val secondCompanyId = UUID.fromString(
            apiAccessor.uploadOneCompanyWithRandomIdentifier().actualStoredCompany.companyId,
        )

        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reader)
        assertAccessDeniedWhenUploadingFrameworkData(firstCompanyId, frameworkSampleData, false)
        assertAccessDeniedWhenUploadingFrameworkData(secondCompanyId, frameworkSampleData, false)

        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        val dataOwnersAfterPostRequest =
            apiAccessor.dataOwnerControllerApi.postDataOwner(firstCompanyId, dataReaderUserId)
        validateDataOwnersForCompany(firstCompanyId, listOf(dataReaderUserId), dataOwnersAfterPostRequest)
        assertDoesNotThrow {
            apiAccessor.dataOwnerControllerApi.isUserDataOwnerForCompany(firstCompanyId, dataReaderUserId)
        }

        val dataOwnersAfterDuplicatePostRequest =
            apiAccessor.dataOwnerControllerApi.postDataOwner(firstCompanyId, dataReaderUserId)
        validateDataOwnersForCompany(firstCompanyId, listOf(dataReaderUserId), dataOwnersAfterDuplicatePostRequest)

        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reader)
        uploadEuTaxoData(firstCompanyId, frameworkSampleData)
        assertAccessDeniedWhenUploadingFrameworkData(secondCompanyId, frameworkSampleData, false)

        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        val dataOwnersAfterRemovingUser =
            apiAccessor.dataOwnerControllerApi.deleteDataOwner(firstCompanyId, dataReaderUserId)
        validateDataOwnersForCompany(firstCompanyId, listOf(), dataOwnersAfterRemovingUser)
        val exceptionWhenCheckingIfUserIsDataOwner = assertThrows<ClientException> {
            apiAccessor.dataOwnerControllerApi.isUserDataOwnerForCompany(firstCompanyId, dataReaderUserId)
        }
        assertErrorCodeInCommunityManagerClientException(exceptionWhenCheckingIfUserIsDataOwner, 404)

        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reader)
        assertAccessDeniedWhenUploadingFrameworkData(firstCompanyId, frameworkSampleData, false)
    }

    @Test
    fun `assure that users without admin rights can always find out if they are a data owner of a company`() {
        val companyId = UUID.fromString(
            apiAccessor.uploadOneCompanyWithRandomIdentifier().actualStoredCompany.companyId,
        )
        apiAccessor.dataOwnerControllerApi.postDataOwner(companyId, dataReaderUserId)

        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reader)
        assertDoesNotThrow {
            apiAccessor.dataOwnerControllerApi.isUserDataOwnerForCompany(companyId, dataReaderUserId)
        }
    }

    @Test
    fun `check that accessing data ownership endpoints with an unknown companyId results in exceptions`() {
        val nonExistingCompanyId = UUID.randomUUID()

        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        val exceptionWhenPostingDataOwner = assertThrows<ClientException> {
            apiAccessor.dataOwnerControllerApi.postDataOwner(nonExistingCompanyId, dataReaderUserId)
        }
        assertCompanyNotFoundResponseBodyInCommunityManagerClientException(
            exceptionWhenPostingDataOwner,
            nonExistingCompanyId,
        )

        val exceptionWhenGettingDataOwners = assertThrows<ClientException> {
            apiAccessor.dataOwnerControllerApi.getDataOwners(nonExistingCompanyId)
        }
        assertCompanyNotFoundResponseBodyInCommunityManagerClientException(
            exceptionWhenGettingDataOwners,
            nonExistingCompanyId,
        )

        val exceptionWhenDeletingDataOwner = assertThrows<ClientException> {
            apiAccessor.dataOwnerControllerApi.deleteDataOwner(nonExistingCompanyId, dataReaderUserId)
        }
        assertCompanyNotFoundResponseBodyInCommunityManagerClientException(
            exceptionWhenDeletingDataOwner,
            nonExistingCompanyId,
        )

        val exceptionWhenCheckingIfUserIsDataOwner = assertThrows<ClientException> {
            apiAccessor.dataOwnerControllerApi.isUserDataOwnerForCompany(nonExistingCompanyId, dataReaderUserId)
        }
        assertErrorCodeInCommunityManagerClientException(exceptionWhenCheckingIfUserIsDataOwner, 404)
    }

    @Test
    fun `check that data ownership endpoints deny access if unauthorized or not sufficient rights`() {
        val companyId = UUID.fromString(
            apiAccessor.uploadOneCompanyWithRandomIdentifier().actualStoredCompany.companyId,
        )

        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(
            TechnicalUser.Uploader,
        )
        val postDataOwnerExceptionBecauseOfMissingRights = assertThrows<ClientException> {
            apiAccessor.dataOwnerControllerApi.postDataOwner(companyId, dataReaderUserId)
        }
        assertAccessDeniedResponseBodyInCommunityManagerClientException(postDataOwnerExceptionBecauseOfMissingRights)

        val deleteExceptionBecauseOfMissingRights = assertThrows<ClientException> {
            apiAccessor.dataOwnerControllerApi.deleteDataOwner(companyId, dataReaderUserId)
        }
        assertAccessDeniedResponseBodyInCommunityManagerClientException(deleteExceptionBecauseOfMissingRights)

        val expectedClientExceptionWhenCallingHeadEndpoint = assertThrows<ClientException> {
            apiAccessor.dataOwnerControllerApi.isUserDataOwnerForCompany(
                companyId, dataReaderUserId,
            )
        }
        assertErrorCodeInCommunityManagerClientException(expectedClientExceptionWhenCallingHeadEndpoint, 403)

        val expectedClientExceptionWhenCallingGetDataOwnersEndpoint = assertThrows<ClientException> {
            apiAccessor.dataOwnerControllerApi.getDataOwners(companyId)
        }
        assertErrorCodeInCommunityManagerClientException(expectedClientExceptionWhenCallingGetDataOwnersEndpoint, 403)
    }

    @Test
    fun `assure that bypassQa is forbidden for users even if they are a data owner`() {
        val companyId = UUID.fromString(
            apiAccessor.uploadOneCompanyWithRandomIdentifier().actualStoredCompany.companyId,
        )
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        apiAccessor.dataOwnerControllerApi.postDataOwner(companyId, dataReaderUserId)
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reader)
        assertAccessDeniedWhenUploadingFrameworkData(companyId, frameworkSampleData, true)
        uploadEuTaxoData(companyId, frameworkSampleData)
    }

    @Test
    fun `assure that the sheer existence of a data owner can be found out even by unauthorized users`() {
        val companyId = UUID.fromString(
            apiAccessor.uploadOneCompanyWithRandomIdentifier().actualStoredCompany.companyId,
        )

        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        apiAccessor.dataOwnerControllerApi.postDataOwner(companyId, dataReaderUserId)

        removeBearerTokenFromApiClients()
        assertDoesNotThrow { apiAccessor.dataOwnerControllerApi.hasCompanyDataOwner(companyId) }

        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        assertDoesNotThrow { apiAccessor.dataOwnerControllerApi.deleteDataOwner(companyId, dataReaderUserId) }

        removeBearerTokenFromApiClients()

        val headExceptionForNonExistingDataOwners = assertThrows<ClientException> {
            apiAccessor.dataOwnerControllerApi.hasCompanyDataOwner(companyId)
        }
        assertErrorCodeInCommunityManagerClientException(headExceptionForNonExistingDataOwners, 404)
    }
}
