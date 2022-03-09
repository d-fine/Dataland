package org.dataland.e2etests

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.api.EuTaxonomyDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.DataIdentifier
import org.dataland.datalandbackend.openApiClient.model.EuTaxonomyData
import org.dataland.datalandbackend.openApiClient.model.EuTaxonomyDataSet

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.math.BigDecimal

// TODO Durchlesen und durchdenken

class CompanyDataControllerTest {
    val companyDataControllerApi = CompanyDataControllerApi(basePath = "http://proxy:80/api")
    val euTaxonomyDataControllerApi = EuTaxonomyDataControllerApi(basePath = "http://proxy:80/api")


    @Test
    fun `post a dummy company and check if post was successful`() {
        val testCompanyName = "Test-Company_01"
        val postCompanyResponse = companyDataControllerApi.postCompany(testCompanyName)
        assertEquals(postCompanyResponse.companyName, testCompanyName)
        assertTrue(postCompanyResponse.companyId.toInt() > 0)
    }

    @Test
    fun `post a dummy company and check if that specific company can be queried by its name`() {
        val testCompanyName = "Dummy-Company_02"
        val postCompanyResponse = companyDataControllerApi.postCompany(testCompanyName)
        val getCompanyByNameResponse = companyDataControllerApi.getCompanyByName(testCompanyName)
        val indexOfCompany = postCompanyResponse.companyId.toInt() - 1
        assertEquals(getCompanyByNameResponse[indexOfCompany], testCompanyName)
    }

    @Test
    fun `post some dummy companies and check if the all-companies-list size increased accordingly`() {
        val testCompanyNames = listOf("Imaginary-Company_03", "Company_04", "Some-Company_05")
        val allCompaniesListSizeBefore = companyDataControllerApi.getAllCompanies().size
        for (i in testCompanyNames) {
            companyDataControllerApi.postCompany(i)
        }
        val allCompaniesListSizeAfter = companyDataControllerApi.getAllCompanies().size
        assertEquals(testCompanyNames.size, allCompaniesListSizeAfter-allCompaniesListSizeBefore)
    }

    @Test
    fun `post a dummy company and dummy data set and check if the company contains that data set ID `() {
        val testCompanyName = "Possible-Company_06"
        val testEuTaxonomyDataSet = EuTaxonomyDataSet(
            reportingObligation = EuTaxonomyDataSet.ReportingObligation.yes,
            attestation = EuTaxonomyDataSet.Attestation.full,
            capex = EuTaxonomyData(
                amount = BigDecimal(52705000),
                taxonomyAlignedProportionOfTurnoverPercent = BigDecimal(20)
            ),
            opex = EuTaxonomyData(
                amount = BigDecimal(80490000),
                taxonomyAlignedProportionOfTurnoverPercent = BigDecimal(15)
            ),
            revenues = EuTaxonomyData(
                amount = BigDecimal(432590000),
                taxonomyAlignedProportionOfTurnoverPercent = BigDecimal(5)
            )
        )
        val postCompanyResponse = companyDataControllerApi.postCompany(testCompanyName)
        val testCompanyId = postCompanyResponse.companyId
        val postDataSetResponse = euTaxonomyDataControllerApi.postData(testCompanyId, testEuTaxonomyDataSet)
        val testEuTaxonomyDataSetId = postDataSetResponse

        val getCompanyDataSetsResponse = companyDataControllerApi.getCompanyDataSets(testCompanyId)
        assertTrue(
            getCompanyDataSetsResponse.contains(
                DataIdentifier(
                    dataID = testEuTaxonomyDataSetId,
                    dataType = "EuTaxonomyDataSet"
                )
            )
        )
    }
}
