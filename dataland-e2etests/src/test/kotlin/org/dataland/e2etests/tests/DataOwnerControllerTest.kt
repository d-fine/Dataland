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
import org.junit.jupiter.api.assertThrows
import java.util.UUID

class DataOwnerControllerTest {
    private val apiAccessor = ApiAccessor()
    private val dataOwnerApi = DataOwnerControllerApi(BASE_PATH_TO_DATALAND_BACKEND)

    private fun validateDataOwnersForCompany(
        companyId: String,
        userIds: List<String>,
        dataOwnersForCompany: CompanyDataOwners,
    ) {
        val dataOwners = dataOwnersForCompany.dataOwners
        assertEquals(companyId, dataOwnersForCompany.companyId)
        assertEquals(userIds.size, dataOwners.size)
        userIds.forEach { assertTrue(dataOwners.contains(it)) }
    }

    @Test
    fun `post data owners to a known company and check happy paths`() {
        val companyId = apiAccessor.uploadOneCompanyWithRandomIdentifier().actualStoredCompany.companyId
        apiAccessor.authenticateAsTechnicalUser(TechnicalUser.Admin)
        val userId = UUID.randomUUID().toString()
        val dataOwnersForCompany = dataOwnerApi.postDataOwner(companyId, userId)
        validateDataOwnersForCompany(companyId, listOf(userId), dataOwnersForCompany)

        val anotherUserId = UUID.randomUUID().toString()
        val dataOwnersForCompanyAfterSecondRequest = dataOwnerApi.postDataOwner(companyId, anotherUserId)
        validateDataOwnersForCompany(companyId, listOf(userId, anotherUserId), dataOwnersForCompanyAfterSecondRequest)

        val dataOwnersForCompanyAfterDuplicateRequest = dataOwnerApi.postDataOwner(companyId, userId)
        assertEquals(dataOwnersForCompanyAfterSecondRequest, dataOwnersForCompanyAfterDuplicateRequest)
    }

    private fun checkErrorMessageForClientException(clientException: ClientException, companyId: String) {
        assertEquals("Client error : 404 ", clientException.message)
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
        assertEquals("Client error : 403 ", clientException.message)
        val responseBody = (clientException.response as ClientError<*>).body as String
        assertTrue(responseBody.contains("Access Denied"))
    }

    @Test
    fun `post data owners either to an unknown company or as a non-admin and check exceptions`() {
        apiAccessor.authenticateAsTechnicalUser(TechnicalUser.Admin)
        val randomCompanyId = UUID.randomUUID().toString()
        val userId = UUID.randomUUID().toString()
        val exceptionForUnknownCompany = assertThrows<ClientException> {
            dataOwnerApi.postDataOwner(randomCompanyId, userId)
        }
        checkErrorMessageForClientException(exceptionForUnknownCompany, randomCompanyId)

        val companyId = apiAccessor.uploadOneCompanyWithRandomIdentifier().actualStoredCompany.companyId
        apiAccessor.authenticateAsTechnicalUser(TechnicalUser.values().filter { it != TechnicalUser.Admin }.random())
        val exceptionForUnauthorizedRequest = assertThrows<ClientException> {
            dataOwnerApi.postDataOwner(companyId, userId)
        }
        checkErrorMessageForUnauthorizedException(exceptionForUnauthorizedRequest)
    }
}
