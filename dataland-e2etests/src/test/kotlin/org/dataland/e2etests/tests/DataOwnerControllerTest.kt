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
        assertErrorCodeInBackendClientException(expectedAccessDeniedClientException, 403)
    }

    private fun assertErrorCodeInCommunityManagerClientException(
        communityManagerClientException: CommunityManagerClientException,
        expectedStatusCode: Number,
    ) {
        assertEquals("Client error : $expectedStatusCode ", communityManagerClientException.message)
    }

    private fun assertErrorCodeInBackendClientException(
        backendClientException: BackendClientException,
        expectedStatusCode: Number,
    ) {
        assertEquals("Client error : $expectedStatusCode ", backendClientException.message)
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

    private fun uploadEuTaxoData(companyId: UUID, dataSet: EutaxonomyNonFinancialsData) {
        val reportingPeriod = "2022"
        apiAccessor.euTaxonomyNonFinancialsUploaderFunction(
            companyId.toString(),
            dataSet,
            reportingPeriod,
            false,
        )
    }

    private fun assertAccessDeniedResponseBodyInCommunityManagerClientException(
        communityManagerClientException: CommunityManagerClientException,
    ) {
        assertErrorCodeInCommunityManagerClientException(communityManagerClientException, 403)
        val responseBody = (communityManagerClientException.response as ClientError<*>).body as String
        assertTrue(responseBody.contains("Access Denied"))
    }

    private fun assertAccessDeniedErrorCodeForRequestToHeadEndpoint(companyId: UUID, userId: UUID) {
        val expectedClientExceptionWhenCallingHeadEndpoint = assertThrows<ClientException> {
            apiAccessor.dataOwnerControllerApi.isUserDataOwnerForCompany(
                companyId, userId,
            )
        }
        assertErrorCodeInCommunityManagerClientException(expectedClientExceptionWhenCallingHeadEndpoint, 403)
    }

    private fun assertAccessDeniedErrorCodeForRequestToGetDataOwnersEndpoint(companyId: UUID) {
        val expectedClientExceptionWhenCallingGetDataOwnersEndpoint = assertThrows<ClientException> {
            apiAccessor.dataOwnerControllerApi.getDataOwners(companyId)
        }
        assertErrorCodeInCommunityManagerClientException(expectedClientExceptionWhenCallingGetDataOwnersEndpoint, 403)
    }

    @Test
    fun `check that data ownership allows even a data reader to upload data`() {
        val frameworkSampleData = apiAccessor.testDataProviderForEuTaxonomyDataForNonFinancials
            .getTData(1)[0]

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

        val dataOwnersAfterDuplicatePostRequest =
            apiAccessor.dataOwnerControllerApi.postDataOwner(firstCompanyId, dataReaderUserId)
        validateDataOwnersForCompany(firstCompanyId, listOf(dataReaderUserId), dataOwnersAfterDuplicatePostRequest)

        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reader)
        uploadEuTaxoData(firstCompanyId, frameworkSampleData)
        assertAccessDeniedWhenUploadingFrameworkData(secondCompanyId, frameworkSampleData, false)

        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        val dataOwnersAfterRemovingUser =
            apiAccessor.dataOwnerControllerApi.deleteDataOwner(firstCompanyId, dataReaderUserId)

        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reader)
        assertAccessDeniedWhenUploadingFrameworkData(firstCompanyId, frameworkSampleData, false)
        assertAccessDeniedWhenUploadingFrameworkData(secondCompanyId, frameworkSampleData, false)

        validateDataOwnersForCompany(firstCompanyId, listOf(), dataOwnersAfterRemovingUser)
    }

    @Test
    fun `check that accessing data ownership endpoints with an unknown companyId results in respective exceptions`() {
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        val nonExistingCompanyId = UUID.randomUUID()
        val userId = UUID.fromString(
            TechnicalUser.Reader.technicalUserId,
        )

        val postExceptionForUnknownCompany = assertThrows<ClientException> {
            apiAccessor.dataOwnerControllerApi.postDataOwner(nonExistingCompanyId, userId)
        }
        assertCompanyNotFoundResponseBodyInCommunityManagerClientException(
            postExceptionForUnknownCompany,
            nonExistingCompanyId,
        )

        val getDataOwnersExceptionForUnknownCompany = assertThrows<ClientException> {
            apiAccessor.dataOwnerControllerApi.getDataOwners(nonExistingCompanyId)
        }
        assertCompanyNotFoundResponseBodyInCommunityManagerClientException(
            getDataOwnersExceptionForUnknownCompany,
            nonExistingCompanyId,
        )

        val responseFromUnknownCompanyDeleteRequest = assertThrows<ClientException> {
            apiAccessor.dataOwnerControllerApi.deleteDataOwner(nonExistingCompanyId, userId)
        }
        assertCompanyNotFoundResponseBodyInCommunityManagerClientException(
            responseFromUnknownCompanyDeleteRequest,
            nonExistingCompanyId,
        )

        val headExceptionForUnknownCompany = assertThrows<ClientException> {
            apiAccessor.dataOwnerControllerApi.isUserDataOwnerForCompany(nonExistingCompanyId, userId)
        }
        assertErrorCodeInCommunityManagerClientException(headExceptionForUnknownCompany, 404)
    }

    @Test
    fun `check that data ownership endpoints deny access if unauthorized or not sufficient rights`() {
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        val userId = UUID.randomUUID()
        val companyId = UUID.fromString(
            apiAccessor.uploadOneCompanyWithRandomIdentifier().actualStoredCompany.companyId,
        )
        val headExceptionForNotFoundDataOwner = assertThrows<ClientException> {
            apiAccessor.dataOwnerControllerApi.isUserDataOwnerForCompany(companyId, userId)
        }
        assertErrorCodeInCommunityManagerClientException(headExceptionForNotFoundDataOwner, 404)

        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(
            TechnicalUser.Reader,
        )
        val postDataOwnerExceptionForUnauthorizedRequest = assertThrows<ClientException> {
            apiAccessor.dataOwnerControllerApi.postDataOwner(companyId, userId)
        }
        assertAccessDeniedResponseBodyInCommunityManagerClientException(postDataOwnerExceptionForUnauthorizedRequest)
        assertAccessDeniedErrorCodeForRequestToHeadEndpoint(companyId, userId)
        assertAccessDeniedErrorCodeForRequestToGetDataOwnersEndpoint(companyId)
    }

    @Test
    fun `delete a data owner as a non admin and check exception`() {
        val companyId = UUID.fromString(
            apiAccessor.uploadOneCompanyWithRandomIdentifier().actualStoredCompany.companyId,
        )
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        apiAccessor.dataOwnerControllerApi.postDataOwner(companyId, dataReaderUserId)

        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reader)
        val deleteExceptionFromUnauthorized = assertThrows<ClientException> {
            apiAccessor.dataOwnerControllerApi.deleteDataOwner(companyId, dataReaderUserId)
        }
        assertAccessDeniedResponseBodyInCommunityManagerClientException(deleteExceptionFromUnauthorized)
    }

    @Test
    fun `get data owner from an existing company as authorized user`() {
        val companyId = UUID.fromString(
            apiAccessor.uploadOneCompanyWithRandomIdentifier().actualStoredCompany.companyId,
        )

        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        val dataOwnersForCompany = apiAccessor.dataOwnerControllerApi.postDataOwner(companyId, dataReaderUserId)
        validateDataOwnersForCompany(companyId, listOf(dataReaderUserId), dataOwnersForCompany)

        assertDoesNotThrow { apiAccessor.dataOwnerControllerApi.isUserDataOwnerForCompany(companyId, dataReaderUserId) }
        val dataOwnersFromGetRequest = apiAccessor.dataOwnerControllerApi.getDataOwners(companyId)
        assertEquals(listOf(dataReaderUserId), dataOwnersFromGetRequest.map { UUID.fromString(it) })
    }

    @Test
    fun `get data owner from an existing company as data owner`() {
        val companyId = UUID.fromString(
            apiAccessor.uploadOneCompanyWithRandomIdentifier().actualStoredCompany.companyId,
        )
        val dataOwnersForCompany = apiAccessor.dataOwnerControllerApi.postDataOwner(companyId, dataReaderUserId)
        validateDataOwnersForCompany(companyId, listOf(dataReaderUserId), dataOwnersForCompany)
        assertDoesNotThrow {
            apiAccessor.dataOwnerControllerApi.isUserDataOwnerForCompany(companyId, dataReaderUserId)
        }

        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reviewer)
        assertAccessDeniedErrorCodeForRequestToHeadEndpoint(companyId, dataReaderUserId)

        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reader)
        assertDoesNotThrow {
            apiAccessor.dataOwnerControllerApi.isUserDataOwnerForCompany(companyId, dataReaderUserId)
        }
    }

    @Test
    fun `post as a data owner and check if bypassQa is forbidden`() {
        val companyId = UUID.fromString(
            apiAccessor.uploadOneCompanyWithRandomIdentifier().actualStoredCompany.companyId,
        )
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        apiAccessor.dataOwnerControllerApi.postDataOwner(companyId, dataReaderUserId)
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reader)
        val frameworkSampleData = apiAccessor.testDataProviderForEuTaxonomyDataForNonFinancials
            .getTData(1)[0]
        assertAccessDeniedWhenUploadingFrameworkData(companyId, frameworkSampleData, true)
        uploadEuTaxoData(companyId, frameworkSampleData)
    }

    @Test
    fun `check for a company if it has a data owner with an existing and non existing data owner `() {
        val companyId = UUID.fromString(
            apiAccessor.uploadOneCompanyWithRandomIdentifier().actualStoredCompany.companyId,
        )
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        apiAccessor.dataOwnerControllerApi.postDataOwner(companyId, dataReaderUserId)

        assertDoesNotThrow { apiAccessor.dataOwnerControllerApi.hasCompanyDataOwner(companyId) }

        assertDoesNotThrow { apiAccessor.dataOwnerControllerApi.deleteDataOwner(companyId, dataReaderUserId) }
        val headExceptionForNonExistingDataOwners = assertThrows<ClientException> {
            apiAccessor.dataOwnerControllerApi.hasCompanyDataOwner(companyId)
        }
        assertErrorCodeInCommunityManagerClientException(headExceptionForNonExistingDataOwners, 404)
    }

    @Test
    fun `check company without a data owner if it has a data owner as unauthorized user`() {
        val companyId = UUID.fromString(
            apiAccessor.uploadOneCompanyWithRandomIdentifier().actualStoredCompany.companyId,
        )

        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        apiAccessor.dataOwnerControllerApi.postDataOwner(companyId, dataReaderUserId)

        removeBearerTokenFromApiClients()

        val checkIfUserIsUnauthorizedResponse = assertThrows<ClientException> {
            apiAccessor.dataOwnerControllerApi.getDataOwners(companyId)
        }
        assertErrorCodeInCommunityManagerClientException(checkIfUserIsUnauthorizedResponse, 403)
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
