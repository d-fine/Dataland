package org.dataland.e2etests.tests

import org.dataland.communitymanager.openApiClient.model.CompanyRole
import org.dataland.e2etests.auth.TechnicalUser
import org.dataland.e2etests.utils.ApiAccessor
import org.dataland.e2etests.utils.DocumentManagerAccessor
import org.dataland.e2etests.utils.ExceptionUtils.assertAccessDeniedWrapper
import org.dataland.e2etests.utils.ExceptionUtils.assertResourceNotFoundWrapper
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertDoesNotThrow
import java.util.UUID

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DataDeletionControllerTest {
    private val apiAccessor = ApiAccessor()
    private val documentManagerAccessor = DocumentManagerAccessor()

    private val testDataEuTaxonomyNonFinancials =
        apiAccessor.testDataProviderForEuTaxonomyDataForNonFinancials
            .getTData(1)
            .first()

    private val testCompanyInformation =
        apiAccessor.testDataProviderForEuTaxonomyDataForNonFinancials
            .getCompanyInformationWithoutIdentifiers(1)
            .first()

    private val dataReaderUserId = UUID.fromString(TechnicalUser.Reader.technicalUserId)

    private fun performAndVerifyDeletion(dataId: String) {
        assertDoesNotThrow { apiAccessor.dataDeletionControllerApi.deleteCompanyAssociatedData(dataId) }
        assertResourceNotFoundWrapper { apiAccessor.metaDataControllerApi.getDataMetaInfo(dataId) }
    }

    private fun tryDeletionAndVerifyDenial(dataId: String) {
        val dataMetaInfoBeforeDeletion = apiAccessor.metaDataControllerApi.getDataMetaInfo(dataId)
        assertAccessDeniedWrapper { apiAccessor.dataDeletionControllerApi.deleteCompanyAssociatedData(dataId) }
        val dataMetaInfoAfterDeletion = apiAccessor.metaDataControllerApi.getDataMetaInfo(dataId)
        assert(dataMetaInfoAfterDeletion == dataMetaInfoBeforeDeletion)
    }

    @BeforeAll
    fun postTestDocuments() {
        documentManagerAccessor.uploadAllTestDocumentsAndAssurePersistence()
    }

    @Test
    fun `post a dummy company and a data set for it and check if that dummy data set can be deleted`() {
        val dataId =
            apiAccessor
                .uploadOneCompanyAndEuTaxonomyDataForNonFinancials(
                    testCompanyInformation,
                    testDataEuTaxonomyNonFinancials,
                ).getValue("dataId")
        performAndVerifyDeletion(dataId)
    }

    @Test
    fun `delete data as a user type which does not have the rights to do so and receive an error code 403`() {
        val dataId =
            apiAccessor
                .uploadOneCompanyAndEuTaxonomyDataForNonFinancials(
                    testCompanyInformation,
                    testDataEuTaxonomyNonFinancials,
                ).getValue("dataId")
        for (role in arrayOf(TechnicalUser.Reader, TechnicalUser.Reviewer, TechnicalUser.Uploader)) {
            apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(role)
            tryDeletionAndVerifyDenial(dataId)
        }
    }

    @Test
    fun `delete data as a company owner and company data uploader`() {
        val companyRolesAllowedToDelete = listOf(CompanyRole.CompanyOwner, CompanyRole.DataUploader)
        for (companyRole in CompanyRole.values()) {
            val mapOfIds =
                apiAccessor.uploadOneCompanyAndEuTaxonomyDataForNonFinancials(
                    testCompanyInformation,
                    testDataEuTaxonomyNonFinancials,
                )
            val companyId = UUID.fromString(mapOfIds.getValue("companyId"))
            val dataId = mapOfIds.getValue("dataId")

            apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reader)
            tryDeletionAndVerifyDenial(dataId)

            apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
            apiAccessor.companyRolesControllerApi.assignCompanyRole(companyRole, companyId, dataReaderUserId)

            apiAccessor.jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reader)
            if (companyRole in companyRolesAllowedToDelete) {
                performAndVerifyDeletion(dataId)
            } else {
                tryDeletionAndVerifyDenial(dataId)
            }
        }
    }
}
