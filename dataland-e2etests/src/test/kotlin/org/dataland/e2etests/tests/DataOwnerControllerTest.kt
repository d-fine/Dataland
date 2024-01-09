package org.dataland.e2etests.tests

import org.dataland.datalandbackend.openApiClient.api.DataOwnerControllerApi
import org.dataland.datalandbackend.openApiClient.infrastructure.ClientError
import org.dataland.datalandbackend.openApiClient.infrastructure.ClientException
import org.dataland.datalandbackend.openApiClient.model.CompanyDataOwners
import org.dataland.e2etests.BASE_PATH_TO_DATALAND_BACKEND
import org.dataland.e2etests.auth.TechnicalUser
import org.dataland.e2etests.utils.ApiAccessor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import java.util.UUID

class DataOwnerControllerTest {
    private val apiAccessor = ApiAccessor()
    private val dataOwnerApi = DataOwnerControllerApi(BASE_PATH_TO_DATALAND_BACKEND)

    private fun validateDataOwnersForCompany(
        companyId: UUID,
        userIds: List<UUID>,
        dataOwnersForCompany: CompanyDataOwners,
    ) {
        val dataOwners = dataOwnersForCompany.dataOwners
        assertEquals(companyId.toString(), dataOwnersForCompany.companyId)
        assertEquals(userIds.size, dataOwners.size)
        userIds.map { it.toString() }.forEach { assertTrue(dataOwners.contains(it)) }
    }

    @Test
    fun `post data owners to a known company and check happy paths`() {
        val companyId = UUID.fromString(
            apiAccessor.uploadOneCompanyWithRandomIdentifier().actualStoredCompany.companyId,
        )
        apiAccessor.authenticateAsTechnicalUser(TechnicalUser.Admin)
        val userId = UUID.randomUUID()
        val dataOwnersForCompany = dataOwnerApi.postDataOwner(companyId, userId)
        validateDataOwnersForCompany(companyId, listOf(userId), dataOwnersForCompany)
        assertDoesNotThrow { dataOwnerApi.isUserDataOwnerForCompany(companyId, userId) }

        val anotherUserId = UUID.randomUUID()
        val dataOwnersForCompanyAfterSecondRequest = dataOwnerApi.postDataOwner(companyId, anotherUserId)
        validateDataOwnersForCompany(companyId, listOf(userId, anotherUserId), dataOwnersForCompanyAfterSecondRequest)

        val dataOwnersForCompanyAfterDuplicateRequest = dataOwnerApi.postDataOwner(companyId, userId)
        assertEquals(dataOwnersForCompanyAfterSecondRequest, dataOwnersForCompanyAfterDuplicateRequest)
    }

    private fun assertErrorCodeForClientException(clientException: ClientException, statusCode: Number) {
        assertEquals("Client error : $statusCode ", clientException.message)
    }

    private fun checkErrorMessageForUnknownCompanyException(clientException: ClientException, companyId: UUID) {
        assertErrorCodeForClientException(clientException, 404)
        val responseBody = (clientException.response as ClientError<*>).body as String
        assertTrue(responseBody.contains("Company not found"))
        assertTrue(
            responseBody.contains(
                "There is no company corresponding to the provided Id $companyId stored on " +
                    "Dataland.",
            ),
        )
    }

    private fun checkErrorMessageForUnauthorizedException(clientException: ClientException) {
        assertErrorCodeForClientException(clientException, 403)
        val responseBody = (clientException.response as ClientError<*>).body as String
        assertTrue(responseBody.contains("Access Denied"))
    }

    @Test
    fun `post data owners either to an unknown company or as a non-admin and check exceptions`() {
        apiAccessor.authenticateAsTechnicalUser(TechnicalUser.Admin)
        val randomCompanyId = UUID.randomUUID()
        val userId = UUID.randomUUID()
        val postExceptionForUnknownCompany = assertThrows<ClientException> {
            dataOwnerApi.postDataOwner(randomCompanyId, userId)
        }
        checkErrorMessageForUnknownCompanyException(postExceptionForUnknownCompany, randomCompanyId)
        val headExceptionForInvalidCompany = assertThrows<ClientException> {
            dataOwnerApi.isUserDataOwnerForCompany(randomCompanyId, userId)
        }
        assertErrorCodeForClientException(headExceptionForInvalidCompany, 400)

        val companyId = UUID.fromString(
            apiAccessor.uploadOneCompanyWithRandomIdentifier().actualStoredCompany.companyId,
        )
        val headExceptionForNotFoundDataOwner = assertThrows<ClientException> {
            dataOwnerApi.isUserDataOwnerForCompany(companyId, userId)
        }
        assertErrorCodeForClientException(headExceptionForNotFoundDataOwner, 404)

        apiAccessor.authenticateAsTechnicalUser(TechnicalUser.values().filter { it != TechnicalUser.Admin }.random())
        val postExceptionForUnauthorizedRequest = assertThrows<ClientException> {
            dataOwnerApi.postDataOwner(companyId, userId)
        }
        checkErrorMessageForUnauthorizedException(postExceptionForUnauthorizedRequest)
        val headExceptionForUnauthorizedRequest = assertThrows<ClientException> {
            dataOwnerApi.isUserDataOwnerForCompany(companyId, userId)
        }
        assertErrorCodeForClientException(headExceptionForUnauthorizedRequest, 403)
    }
}
