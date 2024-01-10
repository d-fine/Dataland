package org.dataland.e2etests.tests

import org.dataland.datalandbackend.openApiClient.infrastructure.ClientException
import org.dataland.e2etests.auth.TechnicalUser
import org.dataland.e2etests.utils.ApiAccessor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class AdminDataManipulationControllerTest {

    private val apiAccessor = ApiAccessor()

    private val testDataEuTaxonomyNonFinancials = apiAccessor.testDataProviderForEuTaxonomyDataForNonFinancials
        .getTData(1).first()

    private val testCompanyInformation = apiAccessor.testDataProviderForEuTaxonomyDataForNonFinancials
        .getCompanyInformationWithoutIdentifiers(1).first()

    @Test
    fun `post a dummy company and a data set for it and check if that dummy data set can be deleted`() {
        val mapOfIds = apiAccessor.uploadOneCompanyAndEuTaxonomyDataForNonFinancials(
            testCompanyInformation,
            testDataEuTaxonomyNonFinancials,
        )
        apiAccessor.adminDataManipulationControllerApi.deleteCompanyAssociatedData(
            mapOfIds.getValue("dataId"),
        )

        val exception =
            assertThrows<ClientException> {
                apiAccessor.dataControllerApiForEuTaxonomyNonFinancials
                    .getCompanyAssociatedEuTaxonomyDataForNonFinancials(mapOfIds.getValue("dataId"))
            }
        assertEquals("Client error : 404 ", exception.message)
    }

    @Test
    fun `delete data as a user type which does not have the rights to do so and receive an error code 403`() {
        val mapOfIds = apiAccessor.uploadOneCompanyAndEuTaxonomyDataForNonFinancials(
            testCompanyInformation,
            testDataEuTaxonomyNonFinancials,
        )
        for (role in arrayOf(TechnicalUser.Reader, TechnicalUser.Reviewer, TechnicalUser.Uploader)) {
            apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(role)

            val exception =
                assertThrows<ClientException> {
                    apiAccessor.adminDataManipulationControllerApi.deleteCompanyAssociatedData(

                        mapOfIds.getValue("dataId"),

                    )
                }
            assertEquals("Client error : 403 ", exception.message)
        }
    }
}
