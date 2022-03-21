package org.dataland.e2etests

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.api.EuTaxonomyDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.PostCompanyRequestBody
import org.dataland.datalandbackend.openApiClient.model.DataIdentifier
import org.dataland.datalandbackend.openApiClient.model.DataSetMetaInformation
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class EuTaxonomyControllerTest {
    val companyDataControllerApi = CompanyDataControllerApi(basePath = "http://proxy:80/api")
    val euTaxonomyDataControllerApi = EuTaxonomyDataControllerApi(basePath = "http://proxy:80/api")

    @Test
    fun `post a dummy company and a dummy data set for it and check if that dummy data set can be retrieved`() {
        val testCompanyName = "Test-Company_A"
        val testEuTaxonomyDataSet = DummyDataCreator().createEuTaxonomyTestDataSet()
        val postCompanyResponse =
            companyDataControllerApi.postCompany(PostCompanyRequestBody(companyName = testCompanyName))
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
        val testEuTaxonomyDataSet = DummyDataCreator().createEuTaxonomyTestDataSet()
        val postCompanyResponse = companyDataControllerApi.postCompany(PostCompanyRequestBody(testCompanyName))
        val testCompanyId = postCompanyResponse.companyId
        val testEuTaxonomyDataSetId = euTaxonomyDataControllerApi.postData(testCompanyId, testEuTaxonomyDataSet)

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
