package org.dataland.e2etests.tests

import org.awaitility.Awaitility
import org.dataland.communitymanager.openApiClient.model.CompanyRole
import org.dataland.datalandqaservice.openApiClient.infrastructure.ClientException
import org.dataland.datalandqaservice.openApiClient.model.QaStatus
import org.dataland.e2etests.auth.TechnicalUser
import org.dataland.e2etests.utils.ApiAccessor
import org.dataland.e2etests.utils.DocumentManagerAccessor
import org.dataland.e2etests.utils.ExceptionUtils.assertAccessDeniedWrapper
import org.dataland.e2etests.utils.ExceptionUtils.assertResourceNotFoundWrapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import java.util.UUID
import java.util.concurrent.TimeUnit

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

    private fun awaitUntil(operation: () -> Boolean) =
        Awaitility.await().atMost(2000, TimeUnit.MILLISECONDS).pollDelay(500, TimeUnit.MILLISECONDS).until {
            operation()
        }

    private fun awaitUntilAsserted(operation: () -> Any) =
        Awaitility.await().atMost(2000, TimeUnit.MILLISECONDS).pollDelay(500, TimeUnit.MILLISECONDS).untilAsserted {
            operation()
        }

    private fun assertNoQaReviewFound(dataId: String) {
        val errorMessage =
            assertThrows<ClientException> {
                apiAccessor.qaServiceControllerApi.getQaReviewResponseByDataId(UUID.fromString(dataId))
            }.message
        assertEquals("Client error : 404 ", errorMessage)
    }

    private fun performAndVerifyDeletion(dataId: String) {
        assertDoesNotThrow { apiAccessor.dataDeletionControllerApi.deleteCompanyAssociatedData(dataId) }
        assertResourceNotFoundWrapper { apiAccessor.metaDataControllerApi.getDataMetaInfo(dataId) }
        awaitUntilAsserted { assertNoQaReviewFound(dataId) }
    }

    private fun tryDeletionAndVerifyDenial(dataId: String) {
        val dataMetaInfoBeforeDeletion = apiAccessor.metaDataControllerApi.getDataMetaInfo(dataId)
        assertAccessDeniedWrapper { apiAccessor.dataDeletionControllerApi.deleteCompanyAssociatedData(dataId) }
        val dataMetaInfoAfterDeletion = apiAccessor.metaDataControllerApi.getDataMetaInfo(dataId)
        assert(dataMetaInfoAfterDeletion == dataMetaInfoBeforeDeletion)
    }

    private fun isDataSetActive(dataId: String): Boolean = apiAccessor.metaDataControllerApi.getDataMetaInfo(dataId).currentlyActive

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

    @Test
    fun `delete accepted data set and check if previously superseeded data set becomes active`() {
        val firstUploadInfo =
            apiAccessor.uploadOneCompanyAndEuTaxonomyDataForNonFinancials(
                testCompanyInformation,
                testDataEuTaxonomyNonFinancials,
                ensureQaPassed = false,
            )
        val dataIdFirstUpload = firstUploadInfo.get("dataId")!!

        awaitUntilAsserted { apiAccessor.qaServiceControllerApi.changeQaStatus(dataIdFirstUpload, QaStatus.Accepted) }
        assert(isDataSetActive(dataIdFirstUpload))

        val dataIdSecondUpload =
            apiAccessor
                .euTaxonomyNonFinancialsUploaderFunction(
                    firstUploadInfo.get("companyId")!!, testDataEuTaxonomyNonFinancials, "", false,
                ).dataId
        awaitUntilAsserted { apiAccessor.qaServiceControllerApi.changeQaStatus(dataIdSecondUpload, QaStatus.Accepted) }
        awaitUntil { isDataSetActive(dataIdSecondUpload) }
        assert(!isDataSetActive(dataIdFirstUpload))

        performAndVerifyDeletion(dataIdSecondUpload)
        awaitUntil { isDataSetActive(dataIdFirstUpload) }
    }
}
