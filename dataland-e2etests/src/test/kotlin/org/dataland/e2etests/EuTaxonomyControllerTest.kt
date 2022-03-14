package org.dataland.e2etests

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.api.EuTaxonomyDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.DataIdentifier
import org.dataland.datalandbackend.openApiClient.model.DataSetMetaInformation
import org.dataland.datalandbackend.openApiClient.model.EuTaxonomyData
import org.dataland.datalandbackend.openApiClient.model.EuTaxonomyDataSet
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.math.BigDecimal

// TODO Cleanup the println()s

class EuTaxonomyControllerTest {
    val companyDataControllerApi = CompanyDataControllerApi(basePath = "http://proxy:80/api")
    val euTaxonomyDataControllerApi = EuTaxonomyDataControllerApi(basePath = "http://proxy:80/api")
    val testEuTaxonomyDataSet = EuTaxonomyDataSet(
        reportingObligation = EuTaxonomyDataSet.ReportingObligation.no,
        attestation = EuTaxonomyDataSet.Attestation.some,
        capex = EuTaxonomyData(
            amount = BigDecimal(5000),
            taxonomyAlignedProportionOfTurnoverPercent = BigDecimal(50)
        ),
        opex = EuTaxonomyData(
            amount = BigDecimal(90000),
            taxonomyAlignedProportionOfTurnoverPercent = BigDecimal(45)
        ),
        revenues = EuTaxonomyData(
            amount = BigDecimal(380000),
            taxonomyAlignedProportionOfTurnoverPercent = BigDecimal(25)
        )
    )

    @Test
    fun `post a dummy company and a dummy data set for it and check if that dummy data set can be retrieved`() {
        val testCompanyName = "Test-Company_A"
        val postCompanyResponse = companyDataControllerApi.postCompany(testCompanyName)
        val testCompanyId = postCompanyResponse.companyId

        val testEuTaxonomyDataSetId = euTaxonomyDataControllerApi.postData(testCompanyId, testEuTaxonomyDataSet)

        val getDataSetResponse = euTaxonomyDataControllerApi.getDataSet(testEuTaxonomyDataSetId)

        assertEquals(
            testEuTaxonomyDataSet,
            getDataSetResponse,
            "The posted and the received eu taxonomy data sets are not equal."
        )
    }

    @Test
    fun `post a dummy company and dummy data set and check if the list of all existing data contains that data set`() {
        val testCompanyName = "Fictitious-Company_B"
        val postCompanyResponse = companyDataControllerApi.postCompany(testCompanyName)
        val testCompanyId = postCompanyResponse.companyId

        val testEuTaxonomyDataSetId = euTaxonomyDataControllerApi.postData(testCompanyId, testEuTaxonomyDataSet)
        println(testEuTaxonomyDataSetId)

        val getDataResponse = euTaxonomyDataControllerApi.getData()

        println(getDataResponse)
        assertTrue(
            getDataResponse.contains(
                DataSetMetaInformation(
                    dataIdentifier = DataIdentifier(
                        dataID = testEuTaxonomyDataSetId,
                        dataType = testEuTaxonomyDataSet.javaClass.kotlin.qualifiedName!!.substringAfterLast(".")
                    ),
                    companyId = testCompanyId
                )
            ),
            "The list of all existing eu taxonomy data does not contain the posted data set."
        )
    }
}
