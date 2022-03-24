package org.dataland.e2etests

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.api.EuTaxonomyDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.CompanyAssociatedDataSetEuTaxonomyData
import org.dataland.datalandbackend.openApiClient.model.PostCompanyRequestBody
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class EuTaxonomyControllerTest {
    val companyDataControllerApi = CompanyDataControllerApi(basePath = "http://proxy:80/api")
    val euTaxonomyDataControllerApi = EuTaxonomyDataControllerApi(basePath = "http://proxy:80/api")

    @Test
    fun `post a dummy company and a dummy data set for it and check if that dummy data set can be retrieved`() {
        val testCompanyName = "Test-Company_A"
        val testEuTaxonomyData = DummyDataCreator().createEuTaxonomyTestDataSet()
        val postCompanyResponse =
            companyDataControllerApi.postCompany(PostCompanyRequestBody(companyName = testCompanyName))
        val testCompanyId = postCompanyResponse.companyId

        val testEuTaxonomyDataId = euTaxonomyDataControllerApi.postData(
            CompanyAssociatedDataSetEuTaxonomyData(testEuTaxonomyData, testCompanyId)
        )

        val companyAssociatedDataSetEuTaxonomyData =
            euTaxonomyDataControllerApi.getCompanyAssociatedDataSet(testEuTaxonomyDataId)

        assertEquals(
            testEuTaxonomyData,
            companyAssociatedDataSetEuTaxonomyData.dataSet,
            "The posted and the received eu taxonomy data sets are not equal."
        )
    }
}
