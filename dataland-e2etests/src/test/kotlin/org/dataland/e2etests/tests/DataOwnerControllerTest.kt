package org.dataland.e2etests.tests

import org.dataland.datalandbackend.openApiClient.api.DataOwnerControllerApi
import org.dataland.datalandbackend.openApiClient.infrastructure.ClientError
import org.dataland.datalandbackend.openApiClient.infrastructure.ClientException
import org.dataland.datalandbackend.openApiClient.model.CompanyDataOwners
import org.dataland.datalandbackend.openApiClient.model.EuTaxonomyDataForNonFinancials
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
    fun `test functionality of the data owner`() {
        val firstCompanyId = UUID.fromString(
            apiAccessor.uploadOneCompanyWithRandomIdentifier().actualStoredCompany.companyId,
        )
        val secondCompanyId = UUID.fromString(
            apiAccessor.uploadOneCompanyWithRandomIdentifier().actualStoredCompany.companyId,
        )

        val dataReaderUserId = UUID.fromString("18b67ecc-1176-4506-8414-1e81661017ca")
        apiAccessor.authenticateAsTechnicalUser(TechnicalUser.Reader)
        val frameworkSampleData = apiAccessor.testDataProviderForEuTaxonomyDataForNonFinancials
            .getTData(1)[0]

        assertFailingApiUploadToCompany(firstCompanyId, frameworkSampleData)
        assertFailingApiUploadToCompany(secondCompanyId, frameworkSampleData)

        apiAccessor.authenticateAsTechnicalUser(TechnicalUser.Admin)
        dataOwnerApi.postDataOwner(firstCompanyId, dataReaderUserId)
        apiAccessor.authenticateAsTechnicalUser(TechnicalUser.Reader)

        assertSucceedingApiUploadToCompany(firstCompanyId, frameworkSampleData)
        assertFailingApiUploadToCompany(secondCompanyId, frameworkSampleData)

        apiAccessor.authenticateAsTechnicalUser(TechnicalUser.Admin)
        dataOwnerApi.deleteDataOwner(firstCompanyId, dataReaderUserId)
        apiAccessor.authenticateAsTechnicalUser(TechnicalUser.Reader)

        assertFailingApiUploadToCompany(firstCompanyId, frameworkSampleData)
        assertFailingApiUploadToCompany(secondCompanyId, frameworkSampleData)
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

        val dataOwnersForCompanyAfterRemovingLastUser = dataOwnerApi.deleteDataOwner(
            companyId,
            anotherUserId,
        )
        validateDataOwnersForCompany(companyId, listOf(userId), dataOwnersForCompanyAfterRemovingLastUser)

        val dataOwnersAfterRemovingBothUsers = dataOwnerApi.deleteDataOwner(
            companyId,
            userId,
        )
        assertEquals(dataOwnersAfterRemovingBothUsers, CompanyDataOwners(companyId.toString(), mutableListOf()))
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

    private fun assertFailingApiUploadToCompany(companyId: UUID, dataSet: EuTaxonomyDataForNonFinancials) {
        val reportingPeriod = "2022"
        val unauthorizedRequestResponse = assertThrows<ClientException> {
            apiAccessor.euTaxonomyNonFinancialsUploaderFunction(
                companyId.toString(),
                dataSet,
                reportingPeriod,
            )
        }
        assertErrorCodeForClientException(unauthorizedRequestResponse, 403)
    }

    private fun assertSucceedingApiUploadToCompany(companyId: UUID, dataSet: EuTaxonomyDataForNonFinancials) {
        val reportingPeriod = "2022"
        apiAccessor.euTaxonomyNonFinancialsUploaderFunction(
            companyId.toString(),
            dataSet,
            reportingPeriod,
            false,
        )
    }

    private fun checkErrorMessageForUnauthorizedException(clientException: ClientException) {
        assertErrorCodeForClientException(clientException, 403)
        val responseBody = (clientException.response as ClientError<*>).body as String
        assertTrue(responseBody.contains("Access Denied"))
    }

    @Test
    fun `post data owners either to an unknown company or not as an admin and check exceptions`() {
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

        apiAccessor.authenticateAsTechnicalUser(TechnicalUser.entries.filter { it != TechnicalUser.Admin }.random())
        val postExceptionForUnauthorizedRequest = assertThrows<ClientException> {
            dataOwnerApi.postDataOwner(companyId, userId)
        }
        checkErrorMessageForUnauthorizedException(postExceptionForUnauthorizedRequest)
        val headExceptionForUnauthorizedRequest = assertThrows<ClientException> {
            dataOwnerApi.isUserDataOwnerForCompany(companyId, userId)
        }
        assertErrorCodeForClientException(headExceptionForUnauthorizedRequest, 403)
    }

    @Test
    fun `delete unknown data owner from an existing company`() {
        val companyId = UUID.fromString(
            apiAccessor.uploadOneCompanyWithRandomIdentifier().actualStoredCompany.companyId,
        )
        val userId = UUID.randomUUID()
        val unknownUserId = UUID.randomUUID()
        apiAccessor.authenticateAsTechnicalUser(TechnicalUser.Admin)
        val dataOwnersForCompany = dataOwnerApi.postDataOwner(companyId, userId)
        validateDataOwnersForCompany(companyId, listOf(userId), dataOwnersForCompany)
        val dataOwnersAfterInvalidDeleteRequest =
            assertThrows<ClientException> {
                dataOwnerApi.deleteDataOwner(companyId, unknownUserId)
            }
        assertErrorCodeForClientException(dataOwnersAfterInvalidDeleteRequest, 404)
    }
}
