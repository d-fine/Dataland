package org.dataland.e2etests.tests

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.api.EuTaxonomyDataControllerApi
import org.dataland.datalandbackend.openApiClient.api.MetaDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.CompanyAssociatedDataEuTaxonomyData
import org.dataland.datalandbackend.openApiClient.model.DataMetaInformation
import org.dataland.datalandbackend.openApiClient.model.StoredCompany
import org.dataland.e2etests.BASE_PATH_TO_DATALAND_PROXY
import org.dataland.e2etests.TestDataProvider
import org.dataland.e2etests.accessmanagement.TokenRequester
import org.dataland.e2etests.accessmanagement.UnauthorizedRequester
import org.dataland.e2etests.accessmanagement.UserType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class CompanyDataControllerTest {

    private val metaDataControllerApi = MetaDataControllerApi(BASE_PATH_TO_DATALAND_PROXY)
    private val companyDataControllerApi = CompanyDataControllerApi(BASE_PATH_TO_DATALAND_PROXY)
    private val euTaxonomyDataControllerApi = EuTaxonomyDataControllerApi(BASE_PATH_TO_DATALAND_PROXY)
    private val testDataProvider = TestDataProvider()
    private val tokenRequester = TokenRequester()
    private val unauthorizedRequester = UnauthorizedRequester()

    @Test
    fun `post a dummy company and check if post was successful`() {
        val testCompanyInformation = testDataProvider.getCompanyInformation(1).first()
        tokenRequester.requestTokenForUserType(UserType.Admin).setToken()
        val postCompanyResponse = companyDataControllerApi.postCompany(testCompanyInformation)
        assertEquals(
            testCompanyInformation, postCompanyResponse.companyInformation,
            "The company information in the post-response does not match " +
                "the actual information of the company to be posted."
        )
        assertTrue(
            postCompanyResponse.companyId.isNotEmpty(),
            "No valid company Id was assigned to the posted company."
        )
    }

    @Test
    fun `post a dummy company and check if that specific company can be queried by its name`() {
        val testCompanyInformation = testDataProvider.getCompanyInformation(1).first()
        tokenRequester.requestTokenForUserType(UserType.Admin).setToken()
        val postCompanyResponse = companyDataControllerApi.postCompany(testCompanyInformation)
        tokenRequester.requestTokenForUserType(UserType.SomeUser).setToken()
        val getCompaniesByNameResponse = companyDataControllerApi.getCompanies(
            testCompanyInformation.companyName, null, true
        )
        assertTrue(
            getCompaniesByNameResponse.contains(
                StoredCompany(
                    postCompanyResponse.companyId,
                    testCompanyInformation,
                    dataRegisteredByDataland = emptyList()
                )
            ),
            "Dataland does not contain the posted company."
        )
    }

    @Test
    fun `post some dummy companies and check if the number of companies increased accordingly`() {
        val listOfTestCompanyInformation = testDataProvider.getCompanyInformation(3)
        tokenRequester.requestTokenForUserType(UserType.Admin).setToken()
        val allCompaniesListSizeBefore = companyDataControllerApi.getCompanies("", null, true).size
        for (companyInformation in listOfTestCompanyInformation) {
            companyDataControllerApi.postCompany(companyInformation)
        }
        tokenRequester.requestTokenForUserType(UserType.SomeUser).setToken()
        val allCompaniesListSizeAfter = companyDataControllerApi.getCompanies("", null, true).size
        assertEquals(
            listOfTestCompanyInformation.size, allCompaniesListSizeAfter - allCompaniesListSizeBefore,
            "The size of the all-companies-list did not increase by ${listOfTestCompanyInformation.size}."
        )
    }

    @Test
    fun `post a dummy company and a dummy data set for it and check if the company contains that data set ID`() {
        val testCompanyInformation = testDataProvider.getCompanyInformation(1).first()
        val testData = testDataProvider.getEuTaxonomyData(1).first()
        val testDataType = testData.javaClass.kotlin.qualifiedName!!.substringAfterLast(".")
        tokenRequester.requestTokenForUserType(UserType.Admin).setToken()
        val testCompanyId = companyDataControllerApi.postCompany(testCompanyInformation).companyId
        val testDataId = euTaxonomyDataControllerApi.postCompanyAssociatedData(
            CompanyAssociatedDataEuTaxonomyData(testCompanyId, testData)
        ).dataId
        tokenRequester.requestTokenForUserType(UserType.SomeUser).setToken()
        val listOfDataMetaInfoForTestCompany = metaDataControllerApi.getListOfDataMetaInfo(
            testCompanyId,
            testDataType
        )
        assertTrue(
            listOfDataMetaInfoForTestCompany.contains(
                DataMetaInformation(testDataId, testDataType, testCompanyId)
            ),
            "The all-data-sets-list of the posted company does not contain the posted data set."
        )
    }

    @Test
    fun `post a dummy company and check if it can be searched for by identifier`() {
        val testCompanyInformation = testDataProvider.getCompanyInformation(1).first()
        tokenRequester.requestTokenForUserType(UserType.Admin).setToken()
        val testCompanyId = companyDataControllerApi.postCompany(testCompanyInformation).companyId
        tokenRequester.requestTokenForUserType(UserType.SomeUser).setToken()
        assertTrue(
            companyDataControllerApi.getCompanies(
                searchString = testCompanyInformation.identifiers.first().identifierValue,
                selectedIndex = null,
                onlyCompanyNames = false
            ).any { it.companyId == testCompanyId }
        )
    }

    @Test
    fun `post the teaser company and test if it can be retrieved by its company ID as unauthorized user`() {
        val teaserCompanyInformation = testDataProvider.getFakeTeaserCompany()
        tokenRequester.requestTokenForUserType(UserType.Admin).setToken()
        val teaserCompanyId = companyDataControllerApi.postCompany(teaserCompanyInformation).companyId
        val getCompanyByIdResponse = unauthorizedRequester.getCompanyById(teaserCompanyId)
        val expectedStoredTeaserCompany = StoredCompany(
            companyId = teaserCompanyId,
            companyInformation = teaserCompanyInformation,
            dataRegisteredByDataland = emptyList()
        )
        assertEquals(
            expectedStoredTeaserCompany, getCompanyByIdResponse,
            "The posted company does not equal the teaser company."
        )
    }
}
