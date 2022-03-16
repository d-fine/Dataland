package org.dataland.e2etests

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.api.EuTaxonomyDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.math.BigDecimal

// TODO All Assertions need error messages!
// TODO Cleanup the println()s from debugging

class CompanyDataControllerTest {
    val companyDataControllerApi = CompanyDataControllerApi(basePath = "http://proxy:80/api")
    val euTaxonomyDataControllerApi = EuTaxonomyDataControllerApi(basePath = "http://proxy:80/api")

    @Test
    fun `post a dummy company and check if post was successful`() {
        val testCompanyName = "Test-Company_01"

        println(testCompanyName)

        val postCompanyResponse = companyDataControllerApi.postCompany(CompaniesRequestBody(companyName = testCompanyName))

        println(postCompanyResponse)
        println(postCompanyResponse.companyName)

        assertEquals(testCompanyName, postCompanyResponse.companyName)
        assertTrue(postCompanyResponse.companyId.toInt() > 0)
    }

    @Test
    fun `post a dummy company and check if that specific company can be queried by its name`() {
        val testCompanyName = "Dummy-Company_02"
        val postCompanyResponse = companyDataControllerApi.postCompany(CompaniesRequestBody(companyName = testCompanyName))
        val getCompaniesByNameResponse = companyDataControllerApi.getCompaniesByName(testCompanyName)

        // DEBUGGING STUFF:
        println(getCompaniesByNameResponse)
        val shouldContain = CompanyMetaInformation(
            companyName = testCompanyName,
            companyId = postCompanyResponse.companyId
        )
        println(shouldContain)
        // DEBUGGING STUFF END

        assertTrue(
            getCompaniesByNameResponse.contains(
                shouldContain
            )
        )
    }

    @Test
    fun `post some dummy companies and check if the number of companies increased accordingly`() {
        val testCompanyNames = listOf("Imaginary-Company_03", "Company_04", "Some-Company_05")
        val allCompaniesListSizeBefore = companyDataControllerApi.getCompaniesByName("").size
        for (i in testCompanyNames) {
            companyDataControllerApi.postCompany(CompaniesRequestBody(companyName = i))
        }
        val allCompaniesListSizeAfter = companyDataControllerApi.getCompaniesByName("").size
        assertEquals(testCompanyNames.size, allCompaniesListSizeAfter - allCompaniesListSizeBefore)
    }

    @Test
    fun `post a dummy company and a dummy data set for it and check if the company contains that data set ID`() {
        val testCompanyName = "Possible-Company_06"
        val testEuTaxonomyDataSet = EuTaxonomyDataSet(
            reportingObligation = EuTaxonomyDataSet.ReportingObligation.yes,
            attestation = EuTaxonomyDataSet.Attestation.full,
            capex = EuTaxonomyData(
                total = BigDecimal(52705000),
                alignedTurnover = BigDecimal(20),
                eligibleTurnover = BigDecimal(10)
            ),
            opex = EuTaxonomyData(
                total = BigDecimal(80490000),
                alignedTurnover = BigDecimal(15),
                eligibleTurnover = BigDecimal(5)
            ),
            revenue = EuTaxonomyData(
                total = BigDecimal(432590000),
                alignedTurnover = BigDecimal(5),
                eligibleTurnover = BigDecimal(3)
            )
        )
        println(testEuTaxonomyDataSet)
        val postCompanyResponse = companyDataControllerApi.postCompany(CompaniesRequestBody(testCompanyName))
        println(postCompanyResponse)
        val testCompanyId = postCompanyResponse.companyId
        println(testCompanyId)
        val testEuTaxonomyDataSetId = euTaxonomyDataControllerApi.postData(testCompanyId, testEuTaxonomyDataSet)
        println(testEuTaxonomyDataSetId)
        val getCompanyDataSetsResponse = companyDataControllerApi.getCompanyDataSets(testCompanyId)
        println(getCompanyDataSetsResponse)
        assertTrue(
            getCompanyDataSetsResponse.contains(
                DataIdentifier(
                    dataID = testEuTaxonomyDataSetId,
                    dataType = testEuTaxonomyDataSet.javaClass.kotlin.qualifiedName!!.substringAfterLast(".")
                )
            )
        )
    }
}
