package org.dataland.e2etests.tests

import org.dataland.communitymanager.openApiClient.model.CompanyRole
import org.dataland.datalandbackend.openApiClient.model.CompanyAssociatedDataEutaxonomyNonFinancialsData
import org.dataland.e2etests.auth.JwtAuthenticationHelper
import org.dataland.e2etests.auth.TechnicalUser
import org.dataland.e2etests.utils.ApiAccessor
import org.dataland.e2etests.utils.DocumentControllerApiAccessor
import org.dataland.e2etests.utils.ExceptionUtils.assertAccessDeniedWrapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import java.lang.IllegalArgumentException
import java.util.UUID

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DataControllerTest {
    private val apiAccessor = ApiAccessor()
    private val documentManagerAccessor = DocumentControllerApiAccessor()
    private val dataReaderUserId = UUID.fromString(TechnicalUser.Reader.technicalUserId)

    val jwtHelper = JwtAuthenticationHelper()

    private val testDataEuTaxonomyNonFinancials =
        apiAccessor.testDataProviderForEuTaxonomyDataForNonFinancials
            .getTData(1)
            .first()

    private val testCompanyInformation =
        apiAccessor.testDataProviderForEuTaxonomyDataForNonFinancials
            .getCompanyInformationWithoutIdentifiers(1)
            .first()

    private val testCompanyInformationNonTeaser =
        testCompanyInformation.copy(isTeaserCompany = false)
    private val testCompanyInformationTeaser =
        testCompanyInformation.copy(isTeaserCompany = true)

    @BeforeAll
    fun postTestDocuments() {
        documentManagerAccessor.uploadAllTestDocumentsAndAssurePersistence()
    }

    @Test
    fun `post a dummy company and a data set for it and check if that dummy data set can be retrieved`() {
        val mapOfIds =
            apiAccessor.uploadOneCompanyAndEuTaxonomyDataForNonFinancials(
                testCompanyInformation,
                testDataEuTaxonomyNonFinancials,
            )
        val companyAssociatedDataEuTaxonomyDataForNonFinancials =
            apiAccessor.dataControllerApiForEuTaxonomyNonFinancials
                .getCompanyAssociatedEutaxonomyNonFinancialsData(mapOfIds.getValue("dataId"))

        assertEquals(
            CompanyAssociatedDataEutaxonomyNonFinancialsData(
                mapOfIds.getValue("companyId"),
                "",
                testDataEuTaxonomyNonFinancials,
            ),
            companyAssociatedDataEuTaxonomyDataForNonFinancials,
            "The posted and the received eu taxonomy data sets and/or their company IDs are not equal.",
        )
    }

    @Test
    fun `post a dummy company as teaser company and a data set for it and test if unauthorized access is possible`() {
        val mapOfIds =
            apiAccessor.uploadOneCompanyAndEuTaxonomyDataForNonFinancials(
                testCompanyInformationTeaser,
                testDataEuTaxonomyNonFinancials,
            )
        val getDataByIdResponse =
            apiAccessor.unauthorizedEuTaxonomyDataNonFinancialsControllerApi
                .getCompanyAssociatedDataEuTaxonomyDataForNonFinancials(mapOfIds.getValue("dataId"))
        val expectedCompanyAssociatedData =
            CompanyAssociatedDataEutaxonomyNonFinancialsData(
                mapOfIds.getValue("companyId"),
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
        val mapOfIds =
            apiAccessor.uploadOneCompanyAndEuTaxonomyDataForNonFinancials(
                testCompanyInformationNonTeaser,
                testDataEuTaxonomyNonFinancials,
            )
        val exception =
            assertThrows<IllegalArgumentException> {
                apiAccessor.unauthorizedEuTaxonomyDataNonFinancialsControllerApi
                    .getCompanyAssociatedDataEuTaxonomyDataForNonFinancials(mapOfIds.getValue("dataId"))
            }
        assertTrue(exception.message!!.contains("Unauthorized access failed"))
    }

    @Test
    fun `check that keycloak reader role can only upload data as company owner or company data uploader`() {
        val companyId =
            UUID.fromString(
                apiAccessor.uploadOneCompanyWithRandomIdentifier().actualStoredCompany.companyId,
            )
        val rolesThatCanUploadPublicData = listOf(CompanyRole.CompanyOwner, CompanyRole.DataUploader)

        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reader)
        assertAccessDeniedWrapper { uploadEuTaxoDataset(companyId) }

        for (role in CompanyRole.values()) {
            jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
            apiAccessor.companyRolesControllerApi.assignCompanyRole(role, companyId = companyId, dataReaderUserId)

            jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reader)
            if (rolesThatCanUploadPublicData.contains(role)) {
                assertDoesNotThrow {
                    apiAccessor.companyRolesControllerApi.hasUserCompanyRole(role, companyId, dataReaderUserId)
                }
                assertDoesNotThrow { uploadEuTaxoDataset(companyId) }
            } else {
                assertAccessDeniedWrapper { uploadEuTaxoDataset(companyId) }
            }
        }
    }

    private fun uploadEuTaxoDataset(companyId: UUID) {
        apiAccessor.euTaxonomyNonFinancialsUploaderFunction(
            companyId.toString(),
            testDataEuTaxonomyNonFinancials,
            "2022",
            false,
        )
    }
}
