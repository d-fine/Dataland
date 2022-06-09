package org.dataland.e2etests.tests

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.api.EuTaxonomyDataControllerApi
import org.dataland.datalandbackend.openApiClient.infrastructure.ClientException
import org.dataland.datalandbackend.openApiClient.model.CompanyAssociatedDataEuTaxonomyData
import org.dataland.datalandbackend.openApiClient.model.CompanyInformation
import org.dataland.datalandbackend.openApiClient.model.EuTaxonomyData
import org.dataland.e2etests.BASE_PATH_TO_DATALAND_PROXY
import org.dataland.e2etests.TestDataProvider
import org.dataland.e2etests.accessmanagement.TokenRequester
import org.dataland.e2etests.accessmanagement.UnauthorizedEuTaxonomyDataControllerApi
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.lang.IllegalArgumentException

class EuTaxonomyControllerTest {
    private val companyDataControllerApi = CompanyDataControllerApi(BASE_PATH_TO_DATALAND_PROXY)
    private val euTaxonomyDataControllerApi = EuTaxonomyDataControllerApi(BASE_PATH_TO_DATALAND_PROXY)
    private val testDataProvider = TestDataProvider()
    private val tokenRequester = TokenRequester()
    private val unauthorizedEuTaxonomyDataControllerApi = UnauthorizedEuTaxonomyDataControllerApi()

    private fun postOneCompanyAndEuTaxonomyData(companyInformation: CompanyInformation, euTaxonomyData: EuTaxonomyData):
        Map<String, String> {
        tokenRequester.requestTokenForUserType(TokenRequester.UserType.Admin)
        val companyId = companyDataControllerApi.postCompany((companyInformation)).companyId
        val dataId = euTaxonomyDataControllerApi.postCompanyAssociatedData(
            CompanyAssociatedDataEuTaxonomyData(companyId, euTaxonomyData)
        ).dataId
        return mapOf("companyId" to companyId, "dataId" to dataId)
    }

    @Test
    fun `post a dummy company and a dummy data set for it and check if that dummy data set can be retrieved`() {
        val testCompanyInformation = testDataProvider.getCompanyInformation(1).first()
        val testData = testDataProvider.getEuTaxonomyData(1).first()
        val mapOfIds = postOneCompanyAndEuTaxonomyData(testCompanyInformation, testData)
        val companyAssociatedDataEuTaxonomyData =
            euTaxonomyDataControllerApi.getCompanyAssociatedData(mapOfIds["dataId"]!!)
        assertEquals(
            CompanyAssociatedDataEuTaxonomyData(mapOfIds["companyId"], testData),
            companyAssociatedDataEuTaxonomyData,
            "The posted and the received eu taxonomy data sets and their company IDs are not equal."
        )
    }

    @Test
    fun `post the teaser dummy company and a dummy data set for it and test if unauthorized access is possible`() {
        val teaserCompanyInformation = testDataProvider.getTeaserDummyCompany()
        val testData = testDataProvider.getEuTaxonomyData(1).first()
        val mapOfIds = postOneCompanyAndEuTaxonomyData(teaserCompanyInformation, testData)
        val getDataByIdResponse =
            unauthorizedEuTaxonomyDataControllerApi.getCompanyAssociatedDataEuTaxonomyData(mapOfIds["dataId"]!!)
        val expectedCompanyAssociatedData = CompanyAssociatedDataEuTaxonomyData(mapOfIds["companyId"]!!, testData)
        assertEquals(
            expectedCompanyAssociatedData, getDataByIdResponse,
            "The posted data does not equal the expected test data."
        )
    }

    @Test
    fun `post a regular dummy company and a dummy data set for it and test if unauthorized access is denied`() {
        val teaserCompanyInformation = testDataProvider.getNonTeaserDummyCompany()
        val testData = testDataProvider.getEuTaxonomyData(1).first()
        val mapOfIds = postOneCompanyAndEuTaxonomyData(teaserCompanyInformation, testData)
        val exception = assertThrows<IllegalArgumentException> {
            unauthorizedEuTaxonomyDataControllerApi.getCompanyAssociatedDataEuTaxonomyData(mapOfIds["dataId"]!!)
        }
        assertTrue(exception.message!!.contains("Unauthorized access failed"))
    }

    @Test
    fun `post data as a user type which does not have the rights to do so and receive an error code 403`() {
        val testCompanyInformation = testDataProvider.getCompanyInformation(1).first()
        val testData = testDataProvider.getEuTaxonomyData(1).first()
        tokenRequester.requestTokenForUserType(TokenRequester.UserType.Admin).setToken()
        val testCompanyId = companyDataControllerApi.postCompany(testCompanyInformation).companyId
        tokenRequester.requestTokenForUserType(TokenRequester.UserType.SomeUser).setToken()
        val exception =
            assertThrows<ClientException> {
                euTaxonomyDataControllerApi.postCompanyAssociatedData(
                    CompanyAssociatedDataEuTaxonomyData(testCompanyId, testData)
                )
            }
        assertEquals("Client error : 403 ", exception.message)
    }
}
