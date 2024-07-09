package org.dataland.e2etests.tests

import org.dataland.datalandbackend.openApiClient.infrastructure.ClientException
import org.dataland.e2etests.auth.TechnicalUser
import org.dataland.e2etests.utils.ApiAccessor
import org.dataland.e2etests.utils.DocumentManagerAccessor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GeneralDataManipulationControllerTest {
    // TODO test name should be changed if we also change the AdminDataManipulationApi in productive code

    private val apiAccessor = ApiAccessor()
    private val documentManagerAccessor = DocumentManagerAccessor()

    private val testDataEuTaxonomyNonFinancials = apiAccessor.testDataProviderForEuTaxonomyDataForNonFinancials
        .getTData(1).first()

    private val testCompanyInformation = apiAccessor.testDataProviderForEuTaxonomyDataForNonFinancials
        .getCompanyInformationWithoutIdentifiers(1).first()

    @BeforeAll
    fun postTestDocuments() {
        documentManagerAccessor.uploadAllTestDocumentsAndAssurePersistence()
    }

    @Test
    fun `post a dummy company and a data set for it and check if that dummy data set can be deleted`() {
        val mapOfIds = apiAccessor.uploadOneCompanyAndEuTaxonomyDataForNonFinancials(
            testCompanyInformation,
            testDataEuTaxonomyNonFinancials,
        )
        val response = apiAccessor.generalDataManipulationControllerApi.deleteCompanyAssociatedDataWithHttpInfo(
            mapOfIds.getValue("dataId"),
        )
        assertEquals("200", response.statusCode.toString())
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
                    apiAccessor.generalDataManipulationControllerApi.deleteCompanyAssociatedData(

                        mapOfIds.getValue("dataId"),

                    )
                }
            assertEquals("Client error : 403 ", exception.message)
        }
    }
}
