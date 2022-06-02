package org.dataland.e2etests.tests

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.api.EuTaxonomyDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.CompanyAssociatedDataEuTaxonomyData
import org.dataland.e2etests.BASE_PATH_TO_DATALAND_PROXY
import org.dataland.e2etests.TestDataProvider
import org.dataland.e2etests.accessmanagement.TokenRequester
import org.dataland.e2etests.accessmanagement.UnauthorizedEuTaxonomyDataControllerApi
import org.dataland.e2etests.accessmanagement.UserType
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

    @Test
    fun `post the teaser dummy company and a dummy data set for it and test if unauthorized access is possible`() {
        val teaserCompanyInformation = testDataProvider.getTeaserDummyCompany()
        val testData = testDataProvider.getEuTaxonomyData(1).first()
        tokenRequester.requestTokenForUserType(UserType.Admin).setToken()
        val teaserCompanyId = companyDataControllerApi.postCompany(teaserCompanyInformation).companyId
        val testDataId = euTaxonomyDataControllerApi.postCompanyAssociatedData(
            CompanyAssociatedDataEuTaxonomyData(teaserCompanyId, testData)
        ).dataId

        val getDataByIdResponse =
            unauthorizedEuTaxonomyDataControllerApi.getCompanyAssociatedDataEuTaxonomyData(testDataId)
        val expectedCompanyAssociatedData = CompanyAssociatedDataEuTaxonomyData(
            companyId = teaserCompanyId,
            data = testData
        )
        assertEquals(
            expectedCompanyAssociatedData, getDataByIdResponse,
            "The posted data does not equal the expected test data."
        )
    }

    @Test
    fun `post a non-teaser dummy company and a dummy data set for it and test if unauthorized access is denied`() {
        val teaserCompanyInformation = testDataProvider.getNonTeaserDummyCompany()
        val testData = testDataProvider.getEuTaxonomyData(1).first()
        tokenRequester.requestTokenForUserType(UserType.Admin).setToken()
        val nonTeaserCompanyId = companyDataControllerApi.postCompany(teaserCompanyInformation).companyId
        val testDataId = euTaxonomyDataControllerApi.postCompanyAssociatedData(
            CompanyAssociatedDataEuTaxonomyData(nonTeaserCompanyId, testData)
        ).dataId
        val exception = assertThrows<IllegalArgumentException> {
            unauthorizedEuTaxonomyDataControllerApi.getCompanyAssociatedDataEuTaxonomyData(testDataId)
        }
        assertTrue(exception.message!!.contains("Unauthorized access failed"))
    }
}

/*
@Test
fun `post a non-teaser dummy company and test if it cannot be retrieved by its company ID as unauthorized user`() {
    val nonTeaserCompanyInformation = testDataProvider.getNonTeaserDummyCompany()
    tokenRequester.requestTokenForUserType(UserType.Admin).setToken()
    val nonTeaserCompanyId = companyDataControllerApi.postCompany(nonTeaserCompanyInformation).companyId
    assertThrows<IllegalArgumentException> {
        unauthorizedCompanyDataControllerApi.getCompanyById(
            nonTeaserCompanyId
        )
    }
}*/

/*
fun `post a dummy company and a dummy data set for it and test if access to it is denied if no token is passed`() {
    val testCompanyInformation = testDataProvider.getCompanyInformation(1).first()
    val testData = testDataProvider.getEuTaxonomyData(1).first()
    Token("").setToken()
    val testCompanyId = companyDataControllerApi.postCompany(testCompanyInformation).companyId
    val testDataId = euTaxonomyDataControllerApi.postCompanyAssociatedData(
        CompanyAssociatedDataEuTaxonomyData(testCompanyId, testData)
    ).dataId

   //Throw unauthorized exception

    assertThrows<IllegalArgumentException> {
        euTaxonomyDataControllerApi.getCompanyAssociatedData(testDataId)
    }
}*/

/*fun `post the teaser company and a dummy data set for it and test if access is possible even without token`() {
    assertEquals(
        "a",
        "a",
        "The posted and the received eu taxonomy data sets and their company IDs are not equal."
    )
}

*/
