package org.dataland.e2etests

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.api.EuTaxonomyDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.DataIdentifier
import org.dataland.datalandbackend.openApiClient.model.DataSetMetaInformation
import org.dataland.datalandbackend.openApiClient.model.PostCompanyRequestBody
import org.dataland.datalandbackend.openApiClient.model.PostDataRequestBodyEuTaxonomyData
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class EuTaxonomyControllerTest {
    val companyDataControllerApi = CompanyDataControllerApi(basePath = "http://proxy:80/api")
    val euTaxonomyDataControllerApi = EuTaxonomyDataControllerApi(basePath = "http://proxy:80/api")

    val testCompanyName = listOf("Test-Company_A", "Fictitious-Company_B")

    @Test
    fun `post a dummy company and a dummy data set for it and check if that dummy data set can be retrieved`() {
        val testEuTaxonomyData = DummyDataCreator().createEuTaxonomyTestDataSet()
        val postCompanyResponse =
            companyDataControllerApi.postCompany(PostCompanyRequestBody(companyName = testCompanyName[0]))
        val testCompanyId = postCompanyResponse.companyId

        val testEuTaxonomyDataId = euTaxonomyDataControllerApi.postData(
            PostDataRequestBodyEuTaxonomyData(testEuTaxonomyData, testCompanyId)
        )

        val getDataSetResponse = euTaxonomyDataControllerApi.getDataSet(testEuTaxonomyDataId)

        assertEquals(
            testEuTaxonomyData,
            getDataSetResponse,
            "The posted and the received eu taxonomy data sets are not equal."
        )
    }

    @Test
    fun `post a dummy company and dummy data set and check if the list of all existing data contains that data set`() {
        val testData = DummyDataCreator().createEuTaxonomyTestDataSet()
        val postCompanyResponse = companyDataControllerApi.postCompany(PostCompanyRequestBody(testCompanyName[1]))
        val companyId = postCompanyResponse.companyId
        val dataId = euTaxonomyDataControllerApi.postData(PostDataRequestBodyEuTaxonomyData(testData, companyId))
        val getDataResponse = euTaxonomyDataControllerApi.getData()
        assertTrue(
            getDataResponse.contains(
                DataSetMetaInformation(
                    dataIdentifier = DataIdentifier(
                        dataID = dataId,
                        dataType = testData.javaClass.kotlin.qualifiedName!!.substringAfterLast(".")
                    ),
                    companyId = companyId
                )
            ),
            "The list of all existing eu taxonomy data does not contain the posted data set."
        )
    }
}
