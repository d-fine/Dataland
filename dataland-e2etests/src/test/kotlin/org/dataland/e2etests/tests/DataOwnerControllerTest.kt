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

    private val dataReaderUserId = UUID.fromString("18b67ecc-1176-4506-8414-1e81661017ca")

    @Test
    fun `test functionality of the data owner`() {
        val firstCompanyId = UUID.fromString(
            apiAccessor.uploadOneCompanyWithRandomIdentifier().actualStoredCompany.companyId,
        )
        val secondCompanyId = UUID.fromString(
            apiAccessor.uploadOneCompanyWithRandomIdentifier().actualStoredCompany.companyId,
        )

        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reader)
        val frameworkSampleData = apiAccessor.testDataProviderForEuTaxonomyDataForNonFinancials
            .getTData(1)[0]

        assertFailingApiUploadToCompany(firstCompanyId, frameworkSampleData, false)
        assertFailingApiUploadToCompany(secondCompanyId, frameworkSampleData, false)

        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        val dataOwnersAfterPostRequest = apiAccessor.companyDataControllerApi.postDataOwner(
            firstCompanyId,
            dataReaderUserId,
        )
        validateDataOwnersForCompany(firstCompanyId, listOf(dataReaderUserId), dataOwnersAfterPostRequest)
        checkDuplicateRequest(firstCompanyId)

        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reader)

        assertSucceedingApiUploadToCompany(firstCompanyId, frameworkSampleData)
        assertFailingApiUploadToCompany(secondCompanyId, frameworkSampleData, false)

        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        val dataOwnersAfterRemovingUser =
            apiAccessor.companyDataControllerApi.deleteDataOwner(firstCompanyId, dataReaderUserId)
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reader)

        assertFailingApiUploadToCompany(firstCompanyId, frameworkSampleData, false)
        assertFailingApiUploadToCompany(secondCompanyId, frameworkSampleData, false)

        assertEquals(dataOwnersAfterRemovingUser, CompanyDataOwners(firstCompanyId.toString(), mutableListOf()))
    }

    private fun checkDuplicateRequest(companyId: UUID) {
        val dataOwnersAfterDuplicatePostRequest = apiAccessor.companyDataControllerApi
            .postDataOwner(companyId, dataReaderUserId)
        validateDataOwnersForCompany(companyId, listOf(dataReaderUserId), dataOwnersAfterDuplicatePostRequest)
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
    fun `post get head and delete data owners to an unknown company`() {
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
        val getResultForUnknownCompany = apiAccessor.companyDataControllerApi.getDataOwners(randomCompanyId)
        assertEquals(getResultForUnknownCompany, mutableListOf<String>())
        val responseFromInvalidCompanyDeleteRequest = assertThrows<ClientException> {
            apiAccessor.companyDataControllerApi.deleteDataOwner(randomCompanyId, userId)
        }
        checkErrorMessageForUnknownCompanyException(responseFromInvalidCompanyDeleteRequest, randomCompanyId)
    }

    @Test
    fun `post head get and delete data owners as a non admin and check exceptions`() {
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
        checkGetException(companyId)
    }

    @Test
    fun `delete a data owner as a non admin and check exception`() {
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        val companyId = UUID.fromString(
            apiAccessor.uploadOneCompanyWithRandomIdentifier().actualStoredCompany.companyId,
        )
        val userId = UUID.randomUUID()
        apiAccessor.companyDataControllerApi.postDataOwner(companyId, userId)
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(
            TechnicalUser.entries.filter { it != TechnicalUser.Admin }.random(),
        )
        val deleteExceptionFromUnauthorized = assertThrows<ClientException> {
            apiAccessor.companyDataControllerApi.deleteDataOwner(companyId, userId)
        }
        checkErrorMessageForUnauthorizedException(deleteExceptionFromUnauthorized)
    }

    private fun checkHeadException(companyId: UUID, userId: UUID) {
        val headExceptionForUnauthorizedRequest = assertThrows<ClientException> {
            apiAccessor.companyDataControllerApi.isUserDataOwnerForCompany(
                companyId, userId,
            )
        }
        assertErrorCodeForClientException(headExceptionForUnauthorizedRequest, 403)
    }

    private fun checkGetException(companyId: UUID) {
        val getExceptionForUnauthorizedRequest = assertThrows<ClientException> {
            apiAccessor.companyDataControllerApi.getDataOwners(companyId)
        }
        assertErrorCodeForClientException(getExceptionForUnauthorizedRequest, 403)
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
    fun `get data owner from an existing company as data owner`() {
        val companyId = UUID.fromString(
            apiAccessor.uploadOneCompanyWithRandomIdentifier().actualStoredCompany.companyId,
        )
        val dataOwnersForCompany = apiAccessor.companyDataControllerApi.postDataOwner(companyId, dataReaderUserId)
        validateDataOwnersForCompany(companyId, listOf(dataReaderUserId), dataOwnersForCompany)
        assertDoesNotThrow {
            apiAccessor.companyDataControllerApi.isUserDataOwnerForCompany(companyId, dataReaderUserId)
        }

        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reviewer)
        checkHeadException(companyId, dataReaderUserId)

        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reader)
        assertDoesNotThrow {
            apiAccessor.companyDataControllerApi.isUserDataOwnerForCompany(companyId, dataReaderUserId)
        }
    }

    @Test
    fun `post as a data owner and check if bypassQa is forbidden`() {
        val companyId = UUID.fromString(
            apiAccessor.uploadOneCompanyWithRandomIdentifier().actualStoredCompany.companyId,
        )
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        apiAccessor.companyDataControllerApi.postDataOwner(companyId, dataReaderUserId)
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reader)
        val frameworkSampleData = apiAccessor.testDataProviderForEuTaxonomyDataForNonFinancials
            .getTData(1)[0]
        assertFailingApiUploadToCompany(companyId, frameworkSampleData, true)
        assertSucceedingApiUploadToCompany(companyId, frameworkSampleData)
    }

    @Test
    fun `check for a company that is has a data owner `() {
        val companyId = UUID.fromString(
            apiAccessor.uploadOneCompanyWithRandomIdentifier().actualStoredCompany.companyId,
        )
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        apiAccessor.companyDataControllerApi.postDataOwner(companyId, dataReaderUserId)
        val dataOwnersAfterPostRequest = apiAccessor.companyDataControllerApi.postDataOwner(
            companyId,
            dataReaderUserId,
        )
        apiAccessor.companyDataControllerApi.hasCompanyDataOwner(companyId)
    }
}
