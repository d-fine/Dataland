package org.dataland.e2etests.tests

import org.dataland.datalandbackend.openApiClient.model.CompanyAssociatedDataEuTaxonomyDataForNonFinancials
import org.dataland.datalandbackend.openApiClient.model.StoredCompany
import org.dataland.e2etests.utils.ApiAccessor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class DataRetrievalViaApiKeyTest {

    private val apiAccessor = ApiAccessor()

    // private val apiKeyHandler = ApiKeyHandler()

    @Test
    fun `Create a non-teaser company, generate an API key and get the non-teaser company with it`() {
        val uploadInfo = apiAccessor.uploadOneCompanyWithoutIdentifiersWithExplicitTeaserConfig(false)
        val companyId = uploadInfo.actualStoredCompany.companyId

        val expectedStoredCompany = StoredCompany(companyId, uploadInfo.inputCompanyInformation, emptyList())

        /* TDO
        * Explicitly use ONLY ApiKey in the following data retrieval!
        * */
        // val apiKey = apiKeyHandler.obtainApiKeyForUserType(UserType.Reader, 1)
        val downloadedStoredCompany = apiAccessor.companyDataControllerApi.getCompanyById(companyId)

        assertEquals(
            expectedStoredCompany,
            downloadedStoredCompany,
            "The received company $expectedStoredCompany does not equal the expected company $expectedStoredCompany"
        )
    }

    @Test
    fun `Create a non-teaser company, upload data, generate an API key and get the data with it`() {
        val testDataEuTaxonomyNonFinancials = apiAccessor.testDataProviderForEuTaxonomyDataForNonFinancials
            .getTData(1).first()
        val testCompanyInformationNonTeaser = apiAccessor.testDataProviderForEuTaxonomyDataForNonFinancials
            .getCompanyInformationWithoutIdentifiers(1).first().copy(isTeaserCompany = false)
        val mapOfIds = apiAccessor.uploadOneCompanyAndEuTaxonomyDataForNonFinancials(
            testCompanyInformationNonTeaser,
            testDataEuTaxonomyNonFinancials
        )
        /* TDO
        * Explicitly use ONLY ApiKey in the following data retrieval!
        * */
        // val apiKey = apiKeyHandler.obtainApiKeyForUserType(UserType.Reader, 1)
        val companyAssociatedDataEuTaxonomyDataForNonFinancials =
            apiAccessor.dataControllerApiForEuTaxonomyNonFinancials
                .getCompanyAssociatedEuTaxonomyDataForNonFinancials(mapOfIds["dataId"]!!)

        assertEquals(
            CompanyAssociatedDataEuTaxonomyDataForNonFinancials(mapOfIds["companyId"], testDataEuTaxonomyNonFinancials),
            companyAssociatedDataEuTaxonomyDataForNonFinancials,
            "The posted and the received eu taxonomy data sets and/or their company IDs are not equal."
        )
    }

    /* TDO   include tests for revoking api key in tests.  Also consider different cases! (Api key doesnt even exist,
    Api key exists)*/
}
