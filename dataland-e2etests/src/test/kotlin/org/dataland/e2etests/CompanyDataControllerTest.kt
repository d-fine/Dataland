package org.dataland.e2etests

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.api.EuTaxonomyDataControllerApi
import org.dataland.datalandbackend.openApiClient.api.MetaDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.CompanyAssociatedDataEuTaxonomyData
import org.dataland.datalandbackend.openApiClient.model.CompanyInformation
import org.dataland.datalandbackend.openApiClient.model.DataMetaInformation
import org.dataland.datalandbackend.openApiClient.model.StoredCompany
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class CompanyDataControllerTest {

    private val basePathToDatalandProxy = "http://proxy:80/api"
    private val metaDataControllerApi = MetaDataControllerApi(basePathToDatalandProxy)
    private val companyDataControllerApi = CompanyDataControllerApi(basePathToDatalandProxy)
    private val euTaxonomyDataControllerApi = EuTaxonomyDataControllerApi(basePathToDatalandProxy)

    @Test
    fun `post a dummy company and check if post was successful`() {
        val testCompanyInformation = DummyDataCreator().createCompanyTestInformation("A")

        val postCompanyResponse =
            companyDataControllerApi.postCompany(testCompanyInformation)

        assertEquals(
            testCompanyInformation.companyName, postCompanyResponse.companyInformation.companyName,
            "The company name in the post-response does not match the actual name of the company to be posted."
        )
        assertTrue(
            postCompanyResponse.companyId.toInt() > 0,
            "No valid company Id was assigned to the posted company."
        )
    }

    @Test
    fun `post a dummy company and check if that specific company can be queried by its name`() {
        val testCompanyInformation = DummyDataCreator().createCompanyTestInformation("B")
        val postCompanyResponse = companyDataControllerApi.postCompany(testCompanyInformation)
        val getCompaniesByNameResponse = companyDataControllerApi.getCompaniesByName(testCompanyInformation.companyName)
        assertTrue(
            getCompaniesByNameResponse.contains(
                StoredCompany(
                    companyId = postCompanyResponse.companyId,
                    companyInformation = testCompanyInformation,
                    dataRegisteredByDataland = emptyList()
                )
            ),
            "Dataland does not contain the posted company."
        )
    }

    @Test
    fun `post some dummy companies and check if the number of companies increased accordingly`() {
        val dummyDataCreator = DummyDataCreator()
        val listOfTestCompanyInformation = listOf<CompanyInformation>(
            dummyDataCreator.createCompanyTestInformation("C"),
            dummyDataCreator.createCompanyTestInformation("D"),
            dummyDataCreator.createCompanyTestInformation("E")
        )
        val allCompaniesListSizeBefore = companyDataControllerApi.getCompaniesByName("").size
        for (companyInformation in listOfTestCompanyInformation) {
            companyDataControllerApi.postCompany(companyInformation)
        }
        val allCompaniesListSizeAfter = companyDataControllerApi.getCompaniesByName("").size
        assertEquals(
            listOfTestCompanyInformation.size, allCompaniesListSizeAfter - allCompaniesListSizeBefore,
            "The size of the all-companies-list did not increase by ${listOfTestCompanyInformation.size}."
        )
    }

    @Test
    fun `post a dummy company and a dummy data set for it and check if the company contains that data set ID`() {
        val testCompanyInformation = DummyDataCreator().createCompanyTestInformation("F")
        val testData = DummyDataCreator().createEuTaxonomyTestData()
        val testDataType = testData.javaClass.kotlin.qualifiedName!!.substringAfterLast(".")

        val testCompanyId = companyDataControllerApi.postCompany(testCompanyInformation).companyId
        val testDataId = euTaxonomyDataControllerApi.postCompanyAssociatedData(
            CompanyAssociatedDataEuTaxonomyData(testCompanyId, testData)
        ).dataId
        val listOfDataMetaInfoForTestCompany = metaDataControllerApi.getListOfDataMetaInfo(
            companyId = testCompanyId,
            dataType = testDataType
        )
        assertTrue(
            listOfDataMetaInfoForTestCompany.contains(
                DataMetaInformation(testDataId, testDataType, testCompanyId)
            ),
            "The all-data-sets-list of the posted company does not contain the posted data set."
        )
    }
}
