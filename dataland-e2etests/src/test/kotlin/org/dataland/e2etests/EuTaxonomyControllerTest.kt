package org.dataland.e2etests

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.api.EuTaxonomyDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.CompaniesRequestBody
import org.dataland.datalandbackend.openApiClient.model.DataIdentifier
import org.dataland.datalandbackend.openApiClient.model.DataSetMetaInformation
import org.dataland.datalandbackend.openApiClient.model.EuTaxonomyData
import org.dataland.datalandbackend.openApiClient.model.EuTaxonomyDataSet
import org.dataland.datalandbackend.openApiClient.model.UploadableDataSetEuTaxonomyDataSet
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class EuTaxonomyControllerTest {
    val companyDataControllerApi = CompanyDataControllerApi(basePath = "http://proxy:80/api")
    val euTaxonomyDataControllerApi = EuTaxonomyDataControllerApi(basePath = "http://proxy:80/api")
    val testEuTaxonomyDataSet = EuTaxonomyDataSet(
        reportingObligation = EuTaxonomyDataSet.ReportingObligation.yes,
        attestation = EuTaxonomyDataSet.Attestation.full,
        capex = EuTaxonomyData(
            total = BigDecimal(2300000),
            alignedTurnover = BigDecimal(50),
            eligibleTurnover = BigDecimal(40)
        ),
        opex = EuTaxonomyData(
            total = BigDecimal(52230000),
            alignedTurnover = BigDecimal(30),
            eligibleTurnover = BigDecimal(20)
        ),
        revenue = EuTaxonomyData(
            total = BigDecimal(6000000000),
            alignedTurnover = BigDecimal(10),
            eligibleTurnover = BigDecimal(5)
        )
    )

    @Test
    fun `post a dummy company and a dummy data set for it and check if that dummy data set can be retrieved`() {
        val testCompanyName = "Test-Company_A"
        val postCompanyResponse =
            companyDataControllerApi.postCompany(CompaniesRequestBody(companyName = testCompanyName))
        val testCompanyId = postCompanyResponse.companyId

        val testEuTaxonomyDataSetId = euTaxonomyDataControllerApi.postData(
            UploadableDataSetEuTaxonomyDataSet(testEuTaxonomyDataSet, testCompanyId)
        )

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
        val postCompanyResponse =
            companyDataControllerApi.postCompany(CompaniesRequestBody(companyName = testCompanyName))
        val testCompanyId = postCompanyResponse.companyId
        val testEuTaxonomyDataSetId = euTaxonomyDataControllerApi.postData(
            UploadableDataSetEuTaxonomyDataSet(testEuTaxonomyDataSet, testCompanyId)
        )

        val getDataResponse = euTaxonomyDataControllerApi.getData()
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
