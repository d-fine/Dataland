package org.dataland.e2etests

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.api.EuTaxonomyDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.CompanyAssociatedDataEuTaxonomyData
import org.dataland.datalandbackend.openApiClient.model.CompanyInformation
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.math.BigDecimal
import java.time.LocalDate

class EuTaxonomyControllerTest {
    private val basePathToDatalandProxy = "http://proxy:80/api"
    private val companyDataControllerApi = CompanyDataControllerApi(basePathToDatalandProxy)
    private val euTaxonomyDataControllerApi = EuTaxonomyDataControllerApi(basePathToDatalandProxy)
    private val testCompanyInformation = CompanyInformation(
        companyName = "Test-Company_10",
        headquarters = "Test-Headquarters_10",
        sector = "Test-Sector_10",
        marketCap = BigDecimal(200),
        reportingDateOfMarketCap = LocalDate.now()
    )

    @Test
    fun `post a dummy company and a dummy data set for it and check if that dummy data set can be retrieved`() {
        val testData = DummyDataCreator().createEuTaxonomyTestData()
        val testCompanyId = companyDataControllerApi.postCompany(testCompanyInformation).companyId
        val testDataId = euTaxonomyDataControllerApi.postCompanyAssociatedData(
            CompanyAssociatedDataEuTaxonomyData(testCompanyId, testData)
        ).dataId

        val companyAssociatedDataEuTaxonomyData =
            euTaxonomyDataControllerApi.getCompanyAssociatedData(testDataId)

        assertEquals(
            CompanyAssociatedDataEuTaxonomyData(testCompanyId, testData),
            companyAssociatedDataEuTaxonomyData,
            "The posted and the received eu taxonomy data sets and their company IDs are not equal."
        )
    }
}
