package org.dataland.e2etests

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.api.EuTaxonomyDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.CompanyAssociatedDataEuTaxonomyData
import org.dataland.datalandbackend.openApiClient.model.PostCompanyRequestBody
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class EuTaxonomyControllerTest {
    private val companyDataControllerApi = CompanyDataControllerApi(basePath = "http://proxy:80/api")
    private val euTaxonomyDataControllerApi = EuTaxonomyDataControllerApi(basePath = "http://proxy:80/api")

    @Test
    fun `post a dummy company and a dummy data set for it and check if that dummy data set can be retrieved`() {
        val testCompanyName = "Test-Company_A"
        val testData = DummyDataCreator().createEuTaxonomyTestDataSet()
        val testCompanyId = companyDataControllerApi.postCompany(PostCompanyRequestBody(testCompanyName)).companyId

        val testDataId = euTaxonomyDataControllerApi.postCompanyAssociatedData(
            CompanyAssociatedDataEuTaxonomyData(testCompanyId, testData)
        ).dataId

        val companyAssociatedDataSetEuTaxonomyData =
            euTaxonomyDataControllerApi.getCompanyAssociatedDataSet(testDataId)

        assertEquals(
            CompanyAssociatedDataEuTaxonomyData(testCompanyId, testData),
            companyAssociatedDataSetEuTaxonomyData,
            "The posted and the received eu taxonomy data sets and their company IDs are not equal."
        )
    }
}
