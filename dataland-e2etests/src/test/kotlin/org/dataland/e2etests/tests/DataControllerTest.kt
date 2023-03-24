package org.dataland.e2etests.tests

import org.dataland.datalandbackend.openApiClient.infrastructure.ClientException
import org.dataland.datalandbackend.openApiClient.model.CompanyAssociatedDataEuTaxonomyDataForNonFinancials
import org.dataland.e2etests.auth.TechnicalUser
import org.dataland.e2etests.utils.ApiAccessor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.lang.IllegalArgumentException

class DataControllerTest {

    private val apiAccessor = ApiAccessor()

    private val testDataEuTaxonomyNonFinancials = apiAccessor.testDataProviderForEuTaxonomyDataForNonFinancials
        .getTData(1).first()

    private val testCompanyInformation = apiAccessor.testDataProviderForEuTaxonomyDataForNonFinancials
        .getCompanyInformationWithoutIdentifiers(1).first()

    private val testCompanyInformationNonTeaser =
        testCompanyInformation.copy(isTeaserCompany = false)
    private val testCompanyInformationTeaser =
        testCompanyInformation.copy(isTeaserCompany = true)

    @Test
    fun `post a dummy company and a data set for it and check if that dummy data set can be retrieved`() {
        val mapOfIds = apiAccessor.uploadOneCompanyAndEuTaxonomyDataForNonFinancials(
            testCompanyInformation,
            testDataEuTaxonomyNonFinancials,
        )
        val companyAssociatedDataEuTaxonomyDataForNonFinancials =
            apiAccessor.dataControllerApiForEuTaxonomyNonFinancials
                .getCompanyAssociatedEuTaxonomyDataForNonFinancials(mapOfIds["dataId"]!!)

        assertEquals(
            CompanyAssociatedDataEuTaxonomyDataForNonFinancials(
                mapOfIds["companyId"],
                "",
                testDataEuTaxonomyNonFinancials,
            ),
            companyAssociatedDataEuTaxonomyDataForNonFinancials,
            "The posted and the received eu taxonomy data sets and/or their company IDs are not equal.",
        )
    }

    @Test
    fun `post a dummy company as teaser company and a data set for it and test if unauthorized access is possible`() {
        val mapOfIds = apiAccessor.uploadOneCompanyAndEuTaxonomyDataForNonFinancials(
            testCompanyInformationTeaser,
            testDataEuTaxonomyNonFinancials,
        )
        val getDataByIdResponse = apiAccessor.unauthorizedEuTaxonomyDataNonFinancialsControllerApi
            .getCompanyAssociatedDataEuTaxonomyDataForNonFinancials(mapOfIds["dataId"]!!)
        val expectedCompanyAssociatedData = CompanyAssociatedDataEuTaxonomyDataForNonFinancials(
            mapOfIds["companyId"]!!,
            "",
            testDataEuTaxonomyNonFinancials,
        )
        assertEquals(
            expectedCompanyAssociatedData,
            getDataByIdResponse,
            "The posted data does not equal the expected test data.",
        )
    }

    @Test
    fun `post a dummy company and a data set for it and test if unauthorized access is denied`() {
        val mapOfIds = apiAccessor.uploadOneCompanyAndEuTaxonomyDataForNonFinancials(
            testCompanyInformationNonTeaser,
            testDataEuTaxonomyNonFinancials,
        )
        val exception = assertThrows<IllegalArgumentException> {
            apiAccessor.unauthorizedEuTaxonomyDataNonFinancialsControllerApi
                .getCompanyAssociatedDataEuTaxonomyDataForNonFinancials(mapOfIds["dataId"]!!)
        }
        assertTrue(exception.message!!.contains("Unauthorized access failed"))
    }

    @Test
    fun `post data as a user type which does not have the rights to do so and receive an error code 403`() {
        val testCompanyId = apiAccessor.uploadOneCompanyWithRandomIdentifier().actualStoredCompany.companyId
        apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reader)
        val exception =
            assertThrows<ClientException> {
                apiAccessor.dataControllerApiForEuTaxonomyNonFinancials
                    .postCompanyAssociatedEuTaxonomyDataForNonFinancials(
                        CompanyAssociatedDataEuTaxonomyDataForNonFinancials(
                            testCompanyId,
                            "",
                            testDataEuTaxonomyNonFinancials,
                        ),
                    )
            }
        assertEquals("Client error : 403 ", exception.message)
    }
}
