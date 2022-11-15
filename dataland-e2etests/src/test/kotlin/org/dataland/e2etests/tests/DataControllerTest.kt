package org.dataland.e2etests.tests

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.api.EuTaxonomyDataForNonFinancialsControllerApi
import org.dataland.datalandbackend.openApiClient.infrastructure.ClientException
import org.dataland.datalandbackend.openApiClient.model.CompanyAssociatedDataEuTaxonomyDataForNonFinancials
import org.dataland.datalandbackend.openApiClient.model.CompanyInformation
import org.dataland.datalandbackend.openApiClient.model.EuTaxonomyDataForNonFinancials
import org.dataland.e2etests.BASE_PATH_TO_DATALAND_BACKEND
import org.dataland.e2etests.accessmanagement.TokenHandler
import org.dataland.e2etests.accessmanagement.UnauthorizedEuTaxonomyDataControllerApi
import org.dataland.e2etests.utils.FrameworkTestDataProvider
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.lang.IllegalArgumentException

class DataControllerTest {

    private val companyDataControllerApi = CompanyDataControllerApi(BASE_PATH_TO_DATALAND_BACKEND)
    private val euTaxonomyDataForNonFinancialsControllerApi =
        EuTaxonomyDataForNonFinancialsControllerApi(BASE_PATH_TO_DATALAND_BACKEND)

    private val tokenHandler = TokenHandler()
    private val unauthorizedEuTaxonomyDataControllerApi = UnauthorizedEuTaxonomyDataControllerApi()

    private val testDataProviderForEuTaxonomyDataForNonFinancials =
        FrameworkTestDataProvider(EuTaxonomyDataForNonFinancials::class.java)

    private val testData = testDataProviderForEuTaxonomyDataForNonFinancials.getTData(1).first()

    private fun postOneCompanyAndEuTaxonomyDataForNonFinancials(
        companyInformation: CompanyInformation,
        euTaxonomyDataForNonFinancials: EuTaxonomyDataForNonFinancials
    ):
        Map<String, String> {
        tokenHandler.obtainTokenForUserType(TokenHandler.UserType.Uploader)
        val companyId = companyDataControllerApi.postCompany((companyInformation)).companyId
        val dataId = euTaxonomyDataForNonFinancialsControllerApi.postCompanyAssociatedEuTaxonomyDataForNonFinancials(
            CompanyAssociatedDataEuTaxonomyDataForNonFinancials(companyId, euTaxonomyDataForNonFinancials)
        ).dataId
        return mapOf("companyId" to companyId, "dataId" to dataId)
    }

    @Test
    fun `post a dummy company and a data set for it and check if that dummy data set can be retrieved`() {
        val testCompanyInformation = testDataProviderForEuTaxonomyDataForNonFinancials
            .getCompanyInformationWithoutIdentifiers(1).first()
        val mapOfIds = postOneCompanyAndEuTaxonomyDataForNonFinancials(testCompanyInformation, testData)
        val companyAssociatedDataEuTaxonomyDataForNonFinancials =
            euTaxonomyDataForNonFinancialsControllerApi
                .getCompanyAssociatedEuTaxonomyDataForNonFinancials(mapOfIds["dataId"]!!)
        assertEquals(
            CompanyAssociatedDataEuTaxonomyDataForNonFinancials(mapOfIds["companyId"], testData),
            companyAssociatedDataEuTaxonomyDataForNonFinancials,
            "The posted and the received eu taxonomy data sets and their company IDs are not equal."
        )
    }

    @Test
    fun `post a dummy company as teaser company and a data set for it and test if unauthorized access is possible`() {
        val testCompanyInformation = testDataProviderForEuTaxonomyDataForNonFinancials
            .getCompanyInformationWithoutIdentifiers(1).first()
            .copy(isTeaserCompany = true)
        val mapOfIds = postOneCompanyAndEuTaxonomyDataForNonFinancials(testCompanyInformation, testData)
        val getDataByIdResponse = unauthorizedEuTaxonomyDataControllerApi
            .getCompanyAssociatedDataEuTaxonomyDataForNonFinancials(mapOfIds["dataId"]!!)
        val expectedCompanyAssociatedData = CompanyAssociatedDataEuTaxonomyDataForNonFinancials(
            mapOfIds["companyId"]!!, testData
        )
        assertEquals(
            expectedCompanyAssociatedData, getDataByIdResponse,
            "The posted data does not equal the expected test data."
        )
    }

    @Test
    fun `post a dummy company and a data set for it and test if unauthorized access is denied`() {
        val testCompanyInformation = testDataProviderForEuTaxonomyDataForNonFinancials
            .getCompanyInformationWithoutIdentifiers(1).first()
            .copy(isTeaserCompany = false)
        val mapOfIds = postOneCompanyAndEuTaxonomyDataForNonFinancials(testCompanyInformation, testData)
        val exception = assertThrows<IllegalArgumentException> {
            unauthorizedEuTaxonomyDataControllerApi
                .getCompanyAssociatedDataEuTaxonomyDataForNonFinancials(mapOfIds["dataId"]!!)
        }
        assertTrue(exception.message!!.contains("Unauthorized access failed"))
    }

    @Test
    fun `post data as a user type which does not have the rights to do so and receive an error code 403`() {
        tokenHandler.obtainTokenForUserType(TokenHandler.UserType.Uploader)
        val testCompanyInformation = testDataProviderForEuTaxonomyDataForNonFinancials
            .getCompanyInformationWithoutIdentifiers(1).first()
        val testCompanyId = companyDataControllerApi.postCompany(testCompanyInformation).companyId
        tokenHandler.obtainTokenForUserType(TokenHandler.UserType.Reader)
        val exception =
            assertThrows<ClientException> {
                euTaxonomyDataForNonFinancialsControllerApi.postCompanyAssociatedEuTaxonomyDataForNonFinancials(
                    CompanyAssociatedDataEuTaxonomyDataForNonFinancials(testCompanyId, testData)
                )
            }
        assertEquals("Client error : 403 ", exception.message)
    }
}
