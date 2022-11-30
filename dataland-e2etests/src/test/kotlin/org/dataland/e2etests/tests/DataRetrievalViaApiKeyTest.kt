package org.dataland.e2etests.tests

import org.dataland.datalandbackend.openApiClient.infrastructure.ClientException
import org.dataland.datalandbackend.openApiClient.model.CompanyAssociatedDataEuTaxonomyDataForNonFinancials
import org.dataland.datalandbackend.openApiClient.model.StoredCompany
import org.dataland.e2etests.accessmanagement.ApiKeyHandler
import org.dataland.e2etests.utils.ApiAccessor
import org.dataland.e2etests.utils.UserType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

private const val REVOCATION_ERROR_MESSAGE = "The received api key could not be revoked."

class DataRetrievalViaApiKeyTest {

    private val apiAccessor = ApiAccessor()

    private val apiKeyHandler = ApiKeyHandler()

    @Test
    fun `create a non teaser company and generate an API key and get the non teaser company with it`() {
        val uploadInfo = apiAccessor.uploadOneCompanyWithoutIdentifiersWithExplicitTeaserConfig(false)
        val companyId = uploadInfo.actualStoredCompany.companyId
        val expectedStoredCompany = StoredCompany(companyId, uploadInfo.inputCompanyInformation, emptyList())

        apiKeyHandler.obtainApiKeyForUserType(UserType.Reader, 1)
        val downloadedStoredCompany = apiAccessor.companyDataControllerApi.getCompanyById(companyId)

        assertEquals(
            expectedStoredCompany,
            downloadedStoredCompany,
            "The received company $expectedStoredCompany does not equal the expected company $expectedStoredCompany"
        )
    }

    @Test
    fun `create a non teaser company and upload data and generate an API key and get the data with it`() {
        val testDataEuTaxonomyNonFinancials = apiAccessor.testDataProviderForEuTaxonomyDataForNonFinancials
            .getTData(1).first()
        val testCompanyInformationNonTeaser = apiAccessor.testDataProviderForEuTaxonomyDataForNonFinancials
            .getCompanyInformationWithoutIdentifiers(1).first().copy(isTeaserCompany = false)
        val mapOfIds = apiAccessor.uploadOneCompanyAndEuTaxonomyDataForNonFinancials(
            testCompanyInformationNonTeaser,
            testDataEuTaxonomyNonFinancials
        )
        apiKeyHandler.obtainApiKeyForUserType(UserType.Reader, 1)
        val companyAssociatedDataEuTaxonomyDataForNonFinancials =
            apiAccessor.dataControllerApiForEuTaxonomyNonFinancials
                .getCompanyAssociatedEuTaxonomyDataForNonFinancials(mapOfIds["dataId"]!!)

        assertEquals(
            CompanyAssociatedDataEuTaxonomyDataForNonFinancials(mapOfIds["companyId"], testDataEuTaxonomyNonFinancials),
            companyAssociatedDataEuTaxonomyDataForNonFinancials,
            "The posted and the received eu taxonomy data sets and/or their company IDs are not equal."
        )
    }

    /* TODO   include tests for revoking api key in tests.  Also consider different cases! (Api key doesnt even exist,
    Api key exists)*/
    @Test
    fun `create api key to retrieve company by id revoke api key and try to retrieve the same company again`() {
        val uploadInfo = apiAccessor.uploadOneCompanyWithoutIdentifiersWithExplicitTeaserConfig(false)
        val companyId = uploadInfo.actualStoredCompany.companyId
        val expectedStoredCompany = StoredCompany(companyId, uploadInfo.inputCompanyInformation, emptyList())

        apiKeyHandler.obtainApiKeyForUserType(UserType.Reader, 1)

        val authorizedRequest = apiAccessor.companyDataControllerApi.getCompanyById(companyId)
        assertEquals(authorizedRequest, expectedStoredCompany)
        apiKeyHandler.revokeApiKeyForUser(UserType.Reader)
        val exception =
            assertThrows<ClientException> {
                apiAccessor.companyDataControllerApi.getCompanyById(companyId).companyId
            }
        assertEquals(
            "Client error : 401 ", exception.message,
        )
    }

    @Test
    fun `create a test in which a api key is successfully validated`() {
        val apiKeyToValidate = apiKeyHandler.obtainApiKeyForUserType(UserType.Reader, 1)
        val responseMessage = apiKeyHandler.validateApiKeyValidationMessage(apiKeyToValidate)
        assertEquals(
            "The API key you provided was successfully validated.",
            responseMessage,
            "The received validation message does not match the expected one."
        )
    }

    @Test
    fun `create a test which tries to validate a non existing api key`() {
        val nonExistingApiKey = apiKeyHandler.obtainApiKeyForUserType(UserType.Reader, 1)
        apiKeyHandler.revokeApiKeyForUser(UserType.Reader)
        val responseMessage = apiKeyHandler.validateApiKeyValidationMessage(nonExistingApiKey)
        assertEquals(
            "Your Dataland account has no API key registered. Please generate one.",
            responseMessage,
            "The tested api key was unexpectedly validated."
        )
    }

    @Test
    fun `create a test which tries to validate a incorrect api key`() {
        apiKeyHandler.obtainApiKeyForUserType(UserType.Reader, 1)
        val incorrectApiKey = "MThiNjdlY2MtMTE3Ni00NTA2LTg0MTQtMWU4MTY2MTAxN2Nh_" +
            "f7d037b92dd8c15022a9761853bcd88d014aab6d34c53705d61d6174a4589ee464c5adee09c9494e_3573499914"
        val responseMessage = apiKeyHandler.validateApiKeyValidationMessage(incorrectApiKey)
        println(responseMessage)
        assertEquals(
            "The API key you provided for your Dataland account is not correct.",
            responseMessage,
            "Message for an invalid api-key was not as expected"
        )
    }

    // ToDo
    /*
        @Test
        fun `create a test which tries to validate an expired api key`() {
            val expiredApiKey = apiKeyHandler.obtainApiKeyForUserType(UserType.Reader, 0)
            // println(expiredApiKey.apiKeyMetaInfo.expiryDate)

            val responseMessage = apiKeyHandler.validateApiKeyValidationMessage(expiredApiKey)
            println(responseMessage)
            assertEquals(
                "The API key you provided for your Dataland account is expired.",
                responseMessage,
                REVOCATION_ERROR_MESSAGE
            )
        }*/
    @Test
    fun `create a test in which an existing api key is revoked`() {
        apiKeyHandler.obtainApiKeyForUserType(UserType.Reader, 1)
        val responseMessage = apiKeyHandler.revokeApiKeyForUser(UserType.Reader)
        assertEquals(
            "The API key for your Dataland account was successfully revoked.",
            responseMessage,
                REVOCATION_ERROR_MESSAGE
        )
    }

    @Test
    fun `create a test in which a non existing api key is revoked`() {
        apiKeyHandler.obtainApiKeyForUserType(UserType.Reader, 1)
        apiKeyHandler.revokeApiKeyForUser(UserType.Reader)
        val responseMessage = apiKeyHandler.revokeApiKeyForUser(UserType.Reader)
        assertEquals(
            "Your Dataland account has no API key registered. Therefore no revokement took place.",
            responseMessage,
            "The tested api key was unexpectedly revoked."
        )
    }
}
