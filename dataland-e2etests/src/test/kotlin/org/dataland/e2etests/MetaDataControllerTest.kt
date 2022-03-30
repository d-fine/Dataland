package org.dataland.e2etests

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.api.EuTaxonomyDataControllerApi
import org.dataland.datalandbackend.openApiClient.api.MetaDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.CompanyAssociatedDataEuTaxonomyData
import org.dataland.datalandbackend.openApiClient.model.DataMetaInformation
import org.dataland.datalandbackend.openApiClient.model.PostCompanyRequestBody
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class MetaDataControllerTest {

    private val basePathToDatalandProxy = "http://proxy:80/api"
    private val metaDataControllerApi = MetaDataControllerApi(basePathToDatalandProxy)
    private val companyDataControllerApi = CompanyDataControllerApi(basePathToDatalandProxy)
    private val euTaxonomyDataControllerApi = EuTaxonomyDataControllerApi(basePathToDatalandProxy)

    @Test
    fun `post a dummy company and a dummy data set for it and check if meta info about that data can be retrieved`() {
        val testCompanyName = "Non-Existent-Company_1"
        val testData = DummyDataCreator().createEuTaxonomyTestDataSet()
        val testDataType = testData.javaClass.kotlin.qualifiedName!!.substringAfterLast(".")

        val testCompanyId = companyDataControllerApi.postCompany(PostCompanyRequestBody(testCompanyName)).companyId
        val testDataId = euTaxonomyDataControllerApi.postCompanyAssociatedData(
            CompanyAssociatedDataEuTaxonomyData(testCompanyId, testData)
        )

        val listOfDataMetaInfo =
            metaDataControllerApi.getDataMetaInfo(testDataId)

        assertEquals(
            listOf(DataMetaInformation(testDataId, testDataType, testCompanyId)),
            listOfDataMetaInfo,
            "The posted and the received eu taxonomy data sets and their company IDs are not equal."
        )
    }
}
