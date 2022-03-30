package org.dataland.e2etests

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.api.EuTaxonomyDataControllerApi
import org.dataland.datalandbackend.openApiClient.api.MetaDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.CompanyAssociatedDataEuTaxonomyData
import org.dataland.datalandbackend.openApiClient.model.CompanyMetaInformation
import org.dataland.datalandbackend.openApiClient.model.DataMetaInformation
import org.dataland.datalandbackend.openApiClient.model.PostCompanyRequestBody
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
        val testCompanyName = "Test-Company_01"
        val postCompanyResponse =
            companyDataControllerApi.postCompany(PostCompanyRequestBody(testCompanyName))

        assertEquals(
            testCompanyName, postCompanyResponse.companyName,
            "The company name in the post-response does not match the actual name of the company to be posted."
        )
        assertTrue(
            postCompanyResponse.companyId.toInt() > 0,
            "No valid company Id was assigend to the posted company."
        )
    }

    @Test
    fun `post a dummy company and check if that specific company can be queried by its name`() {
        val testCompanyName = "Dummy-Company_02"
        val postCompanyResponse =
            companyDataControllerApi.postCompany(PostCompanyRequestBody(testCompanyName))
        val getCompaniesByNameResponse = companyDataControllerApi.getCompaniesByName(testCompanyName)

        assertTrue(
            getCompaniesByNameResponse.contains(
                CompanyMetaInformation(
                    companyId = postCompanyResponse.companyId,
                    companyName = testCompanyName,
                    dataRegisteredByDataland = emptyList()
                )
            ),
            "Dataland does not contain the posted company."
        )
    }

    @Test
    fun `post some dummy companies and check if the number of companies increased accordingly`() {
        val testCompanyNames = listOf("Imaginary-Company_03", "Company_04", "Some-Company_05")
        val allCompaniesListSizeBefore = companyDataControllerApi.getCompaniesByName("").size
        for (i in testCompanyNames) {
            companyDataControllerApi.postCompany(PostCompanyRequestBody(i))
        }
        val allCompaniesListSizeAfter = companyDataControllerApi.getCompaniesByName("").size
        assertEquals(
            testCompanyNames.size, allCompaniesListSizeAfter - allCompaniesListSizeBefore,
            "The size of the all-companies-list did not increase by ${testCompanyNames.size}."
        )
    }

    @Test
    fun `post a dummy company and a dummy data set for it and check if the company contains that data set ID`() {
        val testCompanyName = "Possible-Company_06"
        val testData = DummyDataCreator().createEuTaxonomyTestDataSet()
        val testDataType = testData.javaClass.kotlin.qualifiedName!!.substringAfterLast(".")

        val testCompanyId = companyDataControllerApi.postCompany(PostCompanyRequestBody(testCompanyName)).companyId
        val testDataId = euTaxonomyDataControllerApi.postCompanyAssociatedData(
            CompanyAssociatedDataEuTaxonomyData(testCompanyId, testData)
        )
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
