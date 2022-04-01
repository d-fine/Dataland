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
        industrialSector = "Test-IndustrialSector_10",
        marketCap = BigDecimal(200),
        reportingDateOfMarketCap = LocalDate.now()
    )

    @Test
    fun `post a dummy company with dummy data set and check if the dummy data set can be retrieved`() {
        val testData = DummyDataCreator().createEuTaxonomyTestDataSet()
        val testCompanyId = companyDataControllerApi.postCompany(testCompanyInformation).companyId
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
