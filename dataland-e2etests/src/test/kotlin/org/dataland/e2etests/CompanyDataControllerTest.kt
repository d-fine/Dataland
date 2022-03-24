package org.dataland.e2etests

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.api.EuTaxonomyDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.CompanyAssociatedDataSetEuTaxonomyData
import org.dataland.datalandbackend.openApiClient.model.CompanyMetaInformation
import org.dataland.datalandbackend.openApiClient.model.DataIdentifier
import org.dataland.datalandbackend.openApiClient.model.PostCompanyRequestBody
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class CompanyDataControllerTest {

    val companyDataControllerApi = CompanyDataControllerApi(basePath = "http://proxy:80/api")
    val euTaxonomyDataControllerApi = EuTaxonomyDataControllerApi(basePath = "http://proxy:80/api")

    @Test
    fun `post a dummy company and check if post was successful`() {
        val testCompanyName = "Test-Company_01"

        val postCompanyResponse =
            companyDataControllerApi.postCompany(PostCompanyRequestBody(companyName = testCompanyName))

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
            companyDataControllerApi.postCompany(PostCompanyRequestBody(companyName = testCompanyName))
        val getCompaniesByNameResponse = companyDataControllerApi.getCompaniesByName(testCompanyName)

        assertTrue(
            getCompaniesByNameResponse.contains(
                CompanyMetaInformation(
                    companyName = testCompanyName,
                    companyId = postCompanyResponse.companyId,
                )
            ),
            "The data store does not contain the posted company."
        )
    }

    @Test
    fun `post some dummy companies and check if the number of companies increased accordingly`() {
        val testCompanyNames = listOf("Imaginary-Company_03", "Company_04", "Some-Company_05")
        val allCompaniesListSizeBefore = companyDataControllerApi.getCompaniesByName("").size
        for (i in testCompanyNames) {
            companyDataControllerApi.postCompany(PostCompanyRequestBody(companyName = i))
        }
        val allCompaniesListSizeAfter = companyDataControllerApi.getCompaniesByName("").size
        assertEquals(
            testCompanyNames.size, allCompaniesListSizeAfter - allCompaniesListSizeBefore,
            "The size of the all-companies-list did not increase by one."
        )
    }

    @Test
    fun `post a dummy company and a dummy data set for it and check if the company contains that data set ID`() {
        val testCompanyName = "Possible-Company_06"
        val testEuTaxonomyData = DummyDataCreator().createEuTaxonomyTestDataSet()

        val postCompanyResponse = companyDataControllerApi.postCompany(PostCompanyRequestBody(testCompanyName))
        val testCompanyId = postCompanyResponse.companyId

        val testEuTaxonomyDataId = euTaxonomyDataControllerApi.postCompanyAssociatedDataSet(
            CompanyAssociatedDataSetEuTaxonomyData(testEuTaxonomyData, testCompanyId)
        )
        val getCompanyDataSetsResponse = companyDataControllerApi.getCompanyDataSets(testCompanyId)

        assertTrue(
            getCompanyDataSetsResponse.contains(
                DataIdentifier(
                    dataID = testEuTaxonomyDataId,
                    dataType = testEuTaxonomyData.javaClass.kotlin.qualifiedName!!.substringAfterLast("."),
                )
            ),
            "The all-data-sets-list of the posted company does not contain the posted data set."
        )
    }
}
