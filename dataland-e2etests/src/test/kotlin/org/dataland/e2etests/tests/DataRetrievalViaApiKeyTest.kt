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

class DataRetrievalViaApiKeyTest {

    private val apiAccessor = ApiAccessor()

    private val apiKeyHandler = ApiKeyHandler()

    @Test
    fun `create a non teaser company, generate an API key and get the non teaser company with it`() {
        val uploadInfo = apiAccessor.uploadOneCompanyWithoutIdentifiersWithExplicitTeaserConfig(false)
        val companyId = uploadInfo.actualStoredCompany.companyId
        val expectedStoredCompany = StoredCompany(companyId, uploadInfo.inputCompanyInformation, emptyList())

        apiKeyHandler.obtainApiKeyForUserTypeAndRevokeBearerToken(UserType.Reader, 1)
        val downloadedStoredCompany = apiAccessor.companyDataControllerApi.getCompanyById(companyId)

        assertEquals(
            expectedStoredCompany,
            downloadedStoredCompany,
            "The received company $downloadedStoredCompany does not equal the expected company $expectedStoredCompany"
        )
    }

    @Test
    fun `create a non teaser company, upload framework data for it, generate an API key and get the data with it`() {
        val testDataEuTaxonomyNonFinancials = apiAccessor.testDataProviderForEuTaxonomyDataForNonFinancials
            .getTData(1).first()
        val testCompanyInformationNonTeaser = apiAccessor.testDataProviderForEuTaxonomyDataForNonFinancials
            .getCompanyInformationWithoutIdentifiers(1).first().copy(isTeaserCompany = false)
        val mapOfIds = apiAccessor.uploadOneCompanyAndEuTaxonomyDataForNonFinancials(
            testCompanyInformationNonTeaser,
            testDataEuTaxonomyNonFinancials
        )
        apiKeyHandler.obtainApiKeyForUserTypeAndRevokeBearerToken(UserType.Reader, 1)
        val downloadedCompanyAssociatedDataEuTaxonomyDataForNonFinancials =
            apiAccessor.dataControllerApiForEuTaxonomyNonFinancials
                .getCompanyAssociatedEuTaxonomyDataForNonFinancials(mapOfIds["dataId"]!!)

        assertEquals(
            CompanyAssociatedDataEuTaxonomyDataForNonFinancials(mapOfIds["companyId"], testDataEuTaxonomyNonFinancials),
            downloadedCompanyAssociatedDataEuTaxonomyDataForNonFinancials,
            "The posted and the received eu taxonomy data sets and/or their company IDs are not equal."
        )
    }

    @Test
    fun `create a non teaser company, get it with a valid API key, revoke the API key and try to get the company`() {
        val uploadInfo = apiAccessor.uploadOneCompanyWithoutIdentifiersWithExplicitTeaserConfig(false)
        val companyId = uploadInfo.actualStoredCompany.companyId
        val expectedStoredCompany = StoredCompany(companyId, uploadInfo.inputCompanyInformation, emptyList())

        apiKeyHandler.obtainApiKeyForUserTypeAndRevokeBearerToken(UserType.Reader, 1)
        val downloadedStoredCompany = apiAccessor.companyDataControllerApi.getCompanyById(companyId)
        assertEquals(expectedStoredCompany, downloadedStoredCompany)

        apiKeyHandler.revokeApiKeyForUserTypeAndRevokeBearerToken(UserType.Reader)
        val exception =
            assertThrows<ClientException> {
                apiAccessor.companyDataControllerApi.getCompanyById(companyId).companyId
            }
        assertEquals(
            "Client error : 401 ", exception.message,
        )
    }

    @Test
    fun `generate an API key and then validate it`() {
        val apiKeyToValidate = apiKeyHandler.obtainApiKeyForUserTypeAndRevokeBearerToken(UserType.Reader, 1)
        val actualValidationResponseMessage = apiKeyHandler.validateApiKeyAndReturnValidationMessage(apiKeyToValidate)
        assertEquals(
            "The API key you provided was successfully validated.",
            actualValidationResponseMessage,
            "The received validation message does not match the expected one."
        )
    }

    @Test
    fun `validate a non existing API key`() {
        val nonExistingApiKey = apiKeyHandler.obtainApiKeyForUserTypeAndRevokeBearerToken(UserType.Reader, 1)
        apiKeyHandler.revokeApiKeyForUserTypeAndRevokeBearerToken(UserType.Reader)
        val actualValidationResponseMessage = apiKeyHandler.validateApiKeyAndReturnValidationMessage(nonExistingApiKey)
        assertEquals(
            "Your Dataland account has no API key registered. Please generate one.",
            actualValidationResponseMessage,
            "The tested api key was unexpectedly validated."
        )
    }

    @Test
    fun `validate an API key which has the right format, but a wrong secret `() {
        apiKeyHandler.obtainApiKeyForUserTypeAndRevokeBearerToken(UserType.Reader, 1)
        val apiKeyWithWrongSecret = "MThiNjdlY2MtMTE3Ni00NTA2LTg0MTQtMWU4MTY2MTAxN2Nh_" +
            "f7d037b92dd8c15022a9761853bcd88d014aab6d34c53705d61d6174a4589ee464c5adee09c9494e_3573499914"
        val actualValidationResponseMessage =
            apiKeyHandler.validateApiKeyAndReturnValidationMessage(apiKeyWithWrongSecret)
        assertEquals(
            "The API key you provided for your Dataland account is not correct.",
            actualValidationResponseMessage,
            "Message for an invalid api-key was not as expected"
        )
    }

    @Test
    fun `generate an API key and then revoke it`() {
        apiKeyHandler.obtainApiKeyForUserTypeAndRevokeBearerToken(UserType.Reader, 1)
        val actualRevokeMessage = apiKeyHandler.revokeApiKeyForUserTypeAndRevokeBearerToken(UserType.Reader)
        assertEquals(
            "The API key for your Dataland account was successfully revoked.",
            actualRevokeMessage,
            "The received api key could not be revoked."
        )
    }

    @Test
    fun `genereate an API key, revoke it once so that it is gone, then revoke it a seconde time`() {
        apiKeyHandler.obtainApiKeyForUserTypeAndRevokeBearerToken(UserType.Reader, 1)
        apiKeyHandler.revokeApiKeyForUserTypeAndRevokeBearerToken(UserType.Reader)
        val actualRevokeMessage = apiKeyHandler.revokeApiKeyForUserTypeAndRevokeBearerToken(UserType.Reader)
        assertEquals(
            "Your Dataland account has no API key registered. Therefore no revokement took place.",
            actualRevokeMessage,
            "The tested api key was unexpectedly revoked."
        )
    }

    @Test
    fun `generate an API key for a user and get the meta info on that API key for that user`() {
        // TODO
        /*assertEquals(
            "Your Dataland account has no API key registered. Therefore no revokement took place.",
            actualRevokeMessage,
            "The tested api key was unexpectedly revoked."
        )*/
    }
}
