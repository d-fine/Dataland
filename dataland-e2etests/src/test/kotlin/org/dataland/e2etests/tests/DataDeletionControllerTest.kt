package org.dataland.e2etests.tests

import org.dataland.communitymanager.openApiClient.model.CompanyRole
import org.dataland.datalandbackend.openApiClient.infrastructure.ClientException
import org.dataland.e2etests.READER_USER_ID
import org.dataland.e2etests.auth.TechnicalUser
import org.dataland.e2etests.utils.ApiAccessor
import org.dataland.e2etests.utils.DocumentManagerAccessor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DataDeletionControllerTest {

    private val apiAccessor = ApiAccessor()
    private val documentManagerAccessor = DocumentManagerAccessor()

    private val testDataEuTaxonomyNonFinancials = apiAccessor.testDataProviderForEuTaxonomyDataForNonFinancials
        .getTData(1).first()

    private val testCompanyInformation = apiAccessor.testDataProviderForEuTaxonomyDataForNonFinancials
        .getCompanyInformationWithoutIdentifiers(1).first()

    private val dataReaderUserId = UUID.fromString(READER_USER_ID)

    @BeforeAll
    fun postTestDocuments() {
        documentManagerAccessor.uploadAllTestDocumentsAndAssurePersistence()
    }

    @Test
    fun `post a dummy company and a data set for it and check if that dummy data set can be deleted`() {
        val dataId = apiAccessor.uploadOneCompanyAndEuTaxonomyDataForNonFinancials(
            testCompanyInformation,
            testDataEuTaxonomyNonFinancials,
        ).getValue("dataId")
        assertDoesNotThrow { apiAccessor.dataDeletionControllerApi.deleteCompanyAssociatedData(dataId) }
    }

    @Test
    fun `delete data as a user type which does not have the rights to do so and receive an error code 403`() {
        val dataId = apiAccessor.uploadOneCompanyAndEuTaxonomyDataForNonFinancials(
            testCompanyInformation,
            testDataEuTaxonomyNonFinancials,
        ).getValue("dataId")
        for (role in arrayOf(TechnicalUser.Reader, TechnicalUser.Reviewer, TechnicalUser.Uploader)) {
            apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(role)
            assertAccessDeniedWrapper { apiAccessor.dataDeletionControllerApi.deleteCompanyAssociatedData(dataId) }
        }
    }

    @Test
    fun `delete data as a company owner and company data uploader`() {
        val companyRolesAllowedToDelete = listOf(CompanyRole.CompanyOwner, CompanyRole.DataUploader)
        for (companyRole in CompanyRole.values()) {
            val mapOfIds = apiAccessor.uploadOneCompanyAndEuTaxonomyDataForNonFinancials(
                testCompanyInformation,
                testDataEuTaxonomyNonFinancials,
            )
            val companyId = UUID.fromString(mapOfIds.getValue("companyId"))
            val dataId = mapOfIds.getValue("dataId")

            apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reader)
            assertAccessDeniedWrapper { apiAccessor.dataDeletionControllerApi.deleteCompanyAssociatedData(dataId) }

            apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
            apiAccessor.companyRolesControllerApi.assignCompanyRole(companyRole, companyId, dataReaderUserId)

            apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reader)
            if (companyRole in companyRolesAllowedToDelete) {
                assertDoesNotThrow { apiAccessor.dataDeletionControllerApi.deleteCompanyAssociatedData(dataId) }
            } else { assertAccessDeniedWrapper {
                apiAccessor.dataDeletionControllerApi.deleteCompanyAssociatedData(dataId)
            }
            }
        }
    }

    private fun assertAccessDeniedWrapper(
        operation: () -> Unit,
    ) {
        val expectedAccessDeniedClientException = assertThrows<ClientException> {
            operation()
        }
        assertEquals("Client error : 403 ", expectedAccessDeniedClientException.message)
    }
}
