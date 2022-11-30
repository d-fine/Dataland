package org.dataland.e2etests.tests

import org.dataland.datalandapikeymanager.openApiClient.infrastructure.ApiClient
import org.dataland.datalandbackend.openApiClient.infrastructure.ClientException
import org.dataland.datalandbackend.openApiClient.model.CompanyAssociatedDataEuTaxonomyDataForNonFinancials
import org.dataland.datalandbackend.openApiClient.model.StoredCompany
import org.dataland.e2etests.accessmanagement.ApiKeyHandler
import org.dataland.e2etests.utils.ApiAccessor
import org.dataland.e2etests.utils.UserType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.dataland.datalandbackend.openApiClient.infrastructure.ApiClient as ApiClientBackend

class DataRetrievalViaApiKeyTest {

    private val apiAccessor = ApiAccessor()

    private val apiKeyHandler = ApiKeyHandler()

    @Test
    fun `create a non teaser company and generate an API key and get the non teaser company with it`() {
        val uploadInfo = apiAccessor.uploadOneCompanyWithoutIdentifiersWithExplicitTeaserConfig(false)
        val companyId = uploadInfo.actualStoredCompany.companyId
        val expectedStoredCompany = StoredCompany(companyId, uploadInfo.inputCompanyInformation, emptyList())

        apiKeyHandler.obtainApiKeyForUserType(UserType.Reader, 1)
        ApiClient.Companion.accessToken = null
        ApiClientBackend.Companion.accessToken = null
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
        ApiClient.Companion.accessToken = null
        ApiClientBackend.Companion.accessToken = null
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
    fun `create api key to retrieve company by id, then revoke api key and try to retrieve the same company again`() {
        val uploadInfo = apiAccessor.uploadOneCompanyWithoutIdentifiersWithExplicitTeaserConfig(false)
        val companyId = uploadInfo.actualStoredCompany.companyId
        val expectedStoredCompany = StoredCompany(companyId, uploadInfo.inputCompanyInformation, emptyList())

        apiKeyHandler.obtainApiKeyForUserType(UserType.Reader, 1)
        ApiClient.Companion.accessToken = null
        ApiClientBackend.Companion.accessToken = null

        val authorizedRequest = apiAccessor.companyDataControllerApi.getCompanyById(companyId)
        assertEquals(
            authorizedRequest,
            expectedStoredCompany,
            "The received company $expectedStoredCompany does not equal the expected company $expectedStoredCompany"
        )
        apiKeyHandler.revokeApiKeyForUser(UserType.Reader)
        ApiClient.Companion.accessToken = null
        ApiClientBackend.Companion.accessToken = null
        val exception =
            assertThrows<ClientException> {
                //apiKeyHandler.revokeApiKeyForUser(UserType.Reader)
                apiAccessor.companyDataControllerApi.getCompanyById(companyId)
                    .companyId
            }
        assertEquals(
            "Client error : 401 ", exception.message,
            "The exception message does not say that a 401 client error was the cause."
        )
    }
}
