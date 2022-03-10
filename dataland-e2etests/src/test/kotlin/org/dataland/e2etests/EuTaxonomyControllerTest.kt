package org.dataland.e2etests

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.api.EuTaxonomyDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.DataIdentifier
import org.dataland.datalandbackend.openApiClient.model.DataSetMetaInformation
import org.dataland.datalandbackend.openApiClient.model.EuTaxonomyData
import org.dataland.datalandbackend.openApiClient.model.EuTaxonomyDataSet
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import java.math.BigDecimal

// TODO All Assertions need error messages.
// TODO Cleanup the println()s

class EuTaxonomyControllerTest {
    val companyDataControllerApi = CompanyDataControllerApi(basePath = "http://localhost:8080/api") // TODO change localhost:8080 to proxy:80
    val euTaxonomyDataControllerApi = EuTaxonomyDataControllerApi(basePath = "http://localhost:8080/api") // TODO change localhost:8080 to proxy:80
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
            getDataSetResponse
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
                    ), companyId = testCompanyId
                )
            )
        )
    }


/*      TEMPLATES:
        val testDataSetName = "TestName_007"
        val testDataSetPayload = "testDataSet_007"
        val testDataSet = DataSet(name = testDataSetName, payload = testDataSetPayload)

        var allData = dataControllerApi.getData()
        val numberOfEntriesBeforePost = allData.size

        val postResponse = dataControllerApi.postData(testDataSet)

        allData = dataControllerApi.getData()
        val numberOfEntriesAfterPost = allData.size

        assertEquals(
            numberOfEntriesAfterPost,
            numberOfEntriesBeforePost + 1,
            "Number of entries did not increase by exactly one data set."
        )
        assertEquals(testDataSetName, postResponse.name, "The actual test data set was not posted.")
    }

    @Test
    fun `post a dummy data set and check if that specific data set can be queried by its ID`() {
        val testDataSetName = "TestName_008"
        val testDataSetPayload = "testDataSet_008"
        val testDataSet = DataSet(name = testDataSetName, payload = testDataSetPayload)

        val postResponse = dataControllerApi.postData(testDataSet)
        val testDataSetID = postResponse.id

        val getResponse = dataControllerApi.getDataSet(testDataSetID)

        assertEquals(
            testDataSetName,
            getResponse.name,
            "Response had name: ${getResponse.name} which does not match the posted name: $testDataSetName."
        )
        assertEquals(
            testDataSetPayload,
            getResponse.payload,
            "Response had payload: ${getResponse.payload} which does not match the posted name: $testDataSetPayload."
        )
    }
    */
}
