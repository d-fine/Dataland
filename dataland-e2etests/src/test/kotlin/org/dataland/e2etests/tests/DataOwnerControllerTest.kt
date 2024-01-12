package org.dataland.e2etests.tests

import org.dataland.datalandbackend.openApiClient.infrastructure.ClientError
import org.dataland.datalandbackend.openApiClient.infrastructure.ClientException
import org.dataland.datalandbackend.openApiClient.model.CompanyDataOwners
import org.dataland.datalandbackend.openApiClient.model.EuTaxonomyDataForNonFinancials
import org.dataland.e2etests.auth.JwtAuthenticationHelper
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
    val jwtHelper = JwtAuthenticationHelper()

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
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reader)
        val frameworkSampleData = apiAccessor.testDataProviderForEuTaxonomyDataForNonFinancials
            .getTData(1)[0]

        assertFailingApiUploadToCompany(firstCompanyId, frameworkSampleData, false)
        assertFailingApiUploadToCompany(secondCompanyId, frameworkSampleData, false)

        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        apiAccessor.companyDataControllerApi.postDataOwner(firstCompanyId, dataReaderUserId)
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reader)

        assertSucceedingApiUploadToCompany(firstCompanyId, frameworkSampleData)
        assertFailingApiUploadToCompany(secondCompanyId, frameworkSampleData, false)

        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        apiAccessor.companyDataControllerApi.deleteDataOwner(firstCompanyId, dataReaderUserId)
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reader)

        assertFailingApiUploadToCompany(firstCompanyId, frameworkSampleData, false)
        assertFailingApiUploadToCompany(secondCompanyId, frameworkSampleData, false)
    }

    @Test
    fun `post and delete data owners to a known company and check happy paths`() {
        val companyId = UUID.fromString(
            apiAccessor.uploadOneCompanyWithRandomIdentifier().actualStoredCompany.companyId,
        )
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        val userId = UUID.randomUUID()
        val dataOwnersForCompany = apiAccessor.companyDataControllerApi.postDataOwner(companyId, userId)
        validateDataOwnersForCompany(companyId, listOf(userId), dataOwnersForCompany)
        assertDoesNotThrow { apiAccessor.companyDataControllerApi.isUserDataOwnerForCompany(companyId, userId) }

        val anotherUserId = UUID.randomUUID()
        val dataOwnersForCompanyAfterSecondRequest = apiAccessor.companyDataControllerApi
            .postDataOwner(companyId, anotherUserId)
        validateDataOwnersForCompany(companyId, listOf(userId, anotherUserId), dataOwnersForCompanyAfterSecondRequest)

        val dataOwnersForCompanyAfterDuplicateRequest = apiAccessor.companyDataControllerApi
            .postDataOwner(companyId, userId)
        assertEquals(dataOwnersForCompanyAfterSecondRequest, dataOwnersForCompanyAfterDuplicateRequest)

        val dataOwnersForCompanyAfterRemovingLatestUser = apiAccessor.companyDataControllerApi.deleteDataOwner(
            companyId,
            anotherUserId,
        )
        validateDataOwnersForCompany(companyId, listOf(userId), dataOwnersForCompanyAfterRemovingLatestUser)

        val dataOwnersAfterRemovingBothUsers = apiAccessor.companyDataControllerApi.deleteDataOwner(
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
        assertTrue(responseBody.contains("Company is invalid"))
        assertTrue(
            responseBody.contains(
                "There is no company corresponding to the provided Id $companyId stored on Dataland.",
            ),
        )
    }

    private fun assertFailingApiUploadToCompany(
        companyId: UUID,
        dataSet: EuTaxonomyDataForNonFinancials,
        bypassQa: Boolean = false,
    ) {
        val reportingPeriod = "2022"
        val unauthorizedRequestResponse = assertThrows<ClientException> {
            apiAccessor.euTaxonomyNonFinancialsUploaderFunction(
                companyId.toString(),
                dataSet,
                reportingPeriod,
                bypassQa,
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
    fun `post and delete data owners to an unknown company`() {
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        val randomCompanyId = UUID.randomUUID()
        val userId = UUID.randomUUID()
        val postExceptionForUnknownCompany = assertThrows<ClientException> {
            apiAccessor.companyDataControllerApi.postDataOwner(randomCompanyId, userId)
        }
        checkErrorMessageForUnknownCompanyException(postExceptionForUnknownCompany, randomCompanyId)
        val headExceptionForInvalidCompany = assertThrows<ClientException> {
            apiAccessor.companyDataControllerApi.isUserDataOwnerForCompany(randomCompanyId, userId)
        }
        assertErrorCodeForClientException(headExceptionForInvalidCompany, 404)
        val responseFromInvalidCompanyDeleteRequest = assertThrows<ClientException> {
            apiAccessor.companyDataControllerApi.deleteDataOwner(randomCompanyId, userId)
        }
        checkErrorMessageForUnknownCompanyException(responseFromInvalidCompanyDeleteRequest, randomCompanyId)
    }

    @Test
    fun `post data owners as a non admin and check exceptions`() {
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        val userId = UUID.randomUUID()
        val companyId = UUID.fromString(
            apiAccessor.uploadOneCompanyWithRandomIdentifier().actualStoredCompany.companyId,
        )
        val headExceptionForNotFoundDataOwner = assertThrows<ClientException> {
            apiAccessor.companyDataControllerApi.isUserDataOwnerForCompany(companyId, userId)
        }
        assertErrorCodeForClientException(headExceptionForNotFoundDataOwner, 404)
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(
            TechnicalUser.entries.filter { it != TechnicalUser.Admin }.random(),
        )
        val postExceptionForUnauthorizedRequest = assertThrows<ClientException> {
            apiAccessor.companyDataControllerApi.postDataOwner(companyId, userId)
        }
        checkErrorMessageForUnauthorizedException(postExceptionForUnauthorizedRequest)
        checkHeadException(companyId, userId)
    }

    private fun checkHeadException(companyId: UUID, userId: UUID) {
        val headExceptionForUnauthorizedRequest = assertThrows<ClientException> {
            apiAccessor.companyDataControllerApi.isUserDataOwnerForCompany(companyId, userId)
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
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        val dataOwnersForCompany = apiAccessor.companyDataControllerApi.postDataOwner(companyId, userId)
        validateDataOwnersForCompany(companyId, listOf(userId), dataOwnersForCompany)
        val dataOwnersAfterInvalidDeleteRequest =
            assertThrows<ClientException> {
                apiAccessor.companyDataControllerApi.deleteDataOwner(companyId, unknownUserId)
            }
        assertErrorCodeForClientException(dataOwnersAfterInvalidDeleteRequest, 404)
        // TODO fix error message comparison
    }

    @Test
    fun `get data owner from an existing company as authorized user`() {
        val companyId = UUID.fromString(
            apiAccessor.uploadOneCompanyWithRandomIdentifier().actualStoredCompany.companyId,
        )
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        val userId = UUID.randomUUID()
        val dataOwnersForCompany = apiAccessor.companyDataControllerApi.postDataOwner(companyId, userId)
        validateDataOwnersForCompany(companyId, listOf(userId), dataOwnersForCompany)
        assertDoesNotThrow { apiAccessor.companyDataControllerApi.isUserDataOwnerForCompany(companyId, userId) }
        val dataOwnerFromGetRequest = apiAccessor.companyDataControllerApi.getDataOwners(companyId)
        assertEquals(listOf(userId), dataOwnerFromGetRequest.map { UUID.fromString(it) })
    }

    @Test
    fun `get endpoint unknown company and unauthorized user exception `() {
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        val anotherRandomCompanyId = UUID.randomUUID()
        val getResultForUnknownCompany = apiAccessor.companyDataControllerApi.getDataOwners(anotherRandomCompanyId)
        assertEquals(getResultForUnknownCompany, mutableListOf<String>())
        val anotherCompanyId = UUID.randomUUID()
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(
            TechnicalUser.entries.filter
                { it != TechnicalUser.Admin }.random(),
        )
        val getExceptionForUnauthorizedRequest = assertThrows<ClientException> {
            apiAccessor.companyDataControllerApi.getDataOwners(anotherCompanyId)
        }
        checkErrorMessageForUnauthorizedException(getExceptionForUnauthorizedRequest)
    }

    @Test
    fun `post as a data owner and check if bypassQa is forbidden`() {
        val companyId = UUID.fromString(
            apiAccessor.uploadOneCompanyWithRandomIdentifier().actualStoredCompany.companyId,
        )
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        val dataReaderUserId = UUID.fromString("18b67ecc-1176-4506-8414-1e81661017ca")
        apiAccessor.companyDataControllerApi.postDataOwner(companyId, dataReaderUserId)
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reader)
        val frameworkSampleData = apiAccessor.testDataProviderForEuTaxonomyDataForNonFinancials
            .getTData(1)[0]
        assertFailingApiUploadToCompany(companyId, frameworkSampleData, true)
        assertSucceedingApiUploadToCompany(companyId, frameworkSampleData)
    }
}
