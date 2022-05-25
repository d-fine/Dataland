package org.dataland.e2etests.tests

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.api.EuTaxonomyDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.CompanyAssociatedDataEuTaxonomyData
import org.dataland.e2etests.BASE_PATH_TO_DATALAND_PROXY
import org.dataland.e2etests.TestDataProvider
import org.dataland.e2etests.acessmanagement.TokenRequester
import org.dataland.e2etests.acessmanagement.UserType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class EuTaxonomyControllerTest {
    private val companyDataControllerApi = CompanyDataControllerApi(BASE_PATH_TO_DATALAND_PROXY)
    private val euTaxonomyDataControllerApi = EuTaxonomyDataControllerApi(BASE_PATH_TO_DATALAND_PROXY)
    private val testDataProvider = TestDataProvider()
    private val tokenRequester = TokenRequester()

    @Test
    fun `post a dummy company and a dummy data set for it and check if that dummy data set can be retrieved`() {
        val testCompanyInformation = testDataProvider.getCompanyInformation(1).first()
        val testData = testDataProvider.getEuTaxonomyData(1).first()
        tokenRequester.requestTokenForUserType(UserType.Admin).setToken()
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
