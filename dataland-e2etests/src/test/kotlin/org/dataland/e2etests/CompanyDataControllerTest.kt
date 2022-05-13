package org.dataland.e2etests

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.api.EuTaxonomyDataControllerApi
import org.dataland.datalandbackend.openApiClient.api.MetaDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.CompanyAssociatedDataEuTaxonomyData
import org.dataland.datalandbackend.openApiClient.model.DataMetaInformation
import org.dataland.datalandbackend.openApiClient.model.StoredCompany
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class CompanyDataControllerTest {

    private val metaDataControllerApi = MetaDataControllerApi(BASE_PATH_TO_DATALAND_PROXY)
    private val companyDataControllerApi = CompanyDataControllerApi(BASE_PATH_TO_DATALAND_PROXY)
    private val euTaxonomyDataControllerApi = EuTaxonomyDataControllerApi(BASE_PATH_TO_DATALAND_PROXY)
    private val testDataProvider = TestDataProvider()

    @Test
    fun `post a dummy company and check if post was successful`() {
        val testCompanyInformation = testDataProvider.getCompanyInformation(1).first()

        val postCompanyResponse =
            companyDataControllerApi.postCompany(testCompanyInformation)

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
        val postCompanyResponse = companyDataControllerApi.postCompany(testCompanyInformation)
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
        val allCompaniesListSizeBefore = companyDataControllerApi.getCompanies("", null, true).size
        for (companyInformation in listOfTestCompanyInformation) {
            companyDataControllerApi.postCompany(companyInformation)
        }
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

        val testCompanyId = companyDataControllerApi.postCompany(testCompanyInformation).companyId
        val testDataId = euTaxonomyDataControllerApi.postCompanyAssociatedData(
            CompanyAssociatedDataEuTaxonomyData(testCompanyId, testData)
        ).dataId
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
        val testCompanyId = companyDataControllerApi.postCompany(testCompanyInformation).companyId
        assertTrue(
            companyDataControllerApi.getCompanies(
                searchString = testCompanyInformation.identifiers.first().identifierValue,
                selectedIndex = null,
                onlyCompanyNames = false
            ).any { it.companyId == testCompanyId }
        )
    }
}
