package org.dataland.e2etests.tests

import org.awaitility.core.ConditionTimeoutException
import org.dataland.communitymanager.openApiClient.model.CompanyRole
import org.dataland.datalandbackend.openApiClient.infrastructure.ClientException
import org.dataland.datalandbackend.openApiClient.model.CompanyAssociatedDataEutaxonomyNonFinancialsData
import org.dataland.datalandbackend.openApiClient.model.DataAndMetaInformationSfdrData
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackend.openApiClient.model.ExportFileType
import org.dataland.datalandbackendutils.utils.JsonComparator
import org.dataland.e2etests.auth.JwtAuthenticationHelper
import org.dataland.e2etests.auth.TechnicalUser
import org.dataland.e2etests.utils.ApiAccessor
import org.dataland.e2etests.utils.DocumentControllerApiAccessor
import org.dataland.e2etests.utils.ExceptionUtils.assertAccessDeniedWrapper
import org.dataland.e2etests.utils.api.ApiAwait
import org.dataland.e2etests.utils.assertEqualsByJsonComparator
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
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

        val ignoredKeys = setOf("publicationDate")
        assertEqualsByJsonComparator(
            CompanyAssociatedDataEutaxonomyNonFinancialsData(
                mapOfIds.getValue("companyId"),
                "",
                testDataEuTaxonomyNonFinancials,
            ),
            companyAssociatedDataEuTaxonomyDataForNonFinancials,
            JsonComparator.JsonComparisonOptions(ignoredKeys = ignoredKeys),
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

        val ignoredKeys = setOf("publicationDate")
        assertEqualsByJsonComparator(
            expectedCompanyAssociatedData, getDataByIdResponse,
            JsonComparator
                .JsonComparisonOptions(ignoredKeys),
        )
    }

    @Test
    fun `post a dummy company and a data set for it and test if unauthorized access is denied`() {
        val mapOfIds =
            apiAccessor.uploadOneCompanyAndEuTaxonomyDataForNonFinancials(
                testCompanyInformationNonTeaser,
                testDataEuTaxonomyNonFinancials,
            )
        // The API call is made using a timeout and a retry on 403 errors. Thus, if access is consistently denied,
        // a timeout exception will be thrown.
        val exception =
            assertThrows<ConditionTimeoutException> {
                apiAccessor.unauthorizedEuTaxonomyDataNonFinancialsControllerApi
                    .getCompanyAssociatedDataEuTaxonomyDataForNonFinancials(mapOfIds.getValue("dataId"))
            }

        assertTrue(exception.message!!.contains("code=403"))
    }

    @ParameterizedTest
    @EnumSource(CompanyRole::class)
    fun `check that keycloak reader role can only upload data as company owner or company data uploader`(role: CompanyRole) {
        val companyId = UUID.fromString(apiAccessor.uploadOneCompanyWithRandomIdentifier().actualStoredCompany.companyId)
        val rolesThatCanUploadPublicData = listOf(CompanyRole.CompanyOwner, CompanyRole.DataUploader)

        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reader)
        assertAccessDeniedWrapper { uploadEuTaxoDataset(companyId) }

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

    @Test
    fun `check that requesting data for a company without data successfully returns an empty list`() {
        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Admin)
        val companyId = apiAccessor.uploadOneCompanyWithRandomIdentifier().actualStoredCompany.companyId

        jwtHelper.authenticateApiCallsWithJwtForTechnicalUser(TechnicalUser.Reader)
        assertDoesNotThrow {
            assertEquals(
                emptyList<DataAndMetaInformationSfdrData>(),
                apiAccessor.dataControllerApiForSfdrData.getAllCompanySfdrData(companyId),
            )
        }
    }

    @Test
    fun `post a dummy company and a dataset and check that no other datasets are available`() {
        val reportingPeriod = "2024"
        val uploadInfo =
            apiAccessor.uploadCompanyAndFrameworkDataForOneFramework(
                listOf(testCompanyInformation),
                listOf(testDataEuTaxonomyNonFinancials),
                apiAccessor::euTaxonomyNonFinancialsUploaderFunction,
                reportingPeriod = reportingPeriod,
                ensureQaPassed = true,
            )
        val companyId = uploadInfo[0].actualStoredCompany.companyId
        val dataId = uploadInfo[0].actualStoredDataMetaInfo!!.dataId

        ApiAwait.waitForData(
            supplier = {
                apiAccessor.dataControllerApiForEuTaxonomyNonFinancials
                    .getAllCompanyEutaxonomyNonFinancialsData(
                        companyId = companyId,
                        showOnlyActive = false,
                    ).size
            },
            condition = { it == 1 },
        )

        assertDoesNotThrow {
            apiAccessor.dataControllerApiForEuTaxonomyFinancials.getCompanyAssociatedEutaxonomyFinancialsData(dataId)
        }

        assertThrows<ClientException> {
            apiAccessor.dataControllerApiForP2pData.getCompanyAssociatedP2pData(dataId)
        }.let { assertEquals(400, it.statusCode) }

        listOf(
            apiAccessor.dataControllerApiForEuTaxonomyFinancials::getAllCompanyEutaxonomyFinancialsData,
            apiAccessor.dataControllerApiForSfdrData::getAllCompanySfdrData,
        ).forEach {
            assertEquals(0, it(companyId, false, null).size)
        }

        apiAccessor.dataControllerApiForEuTaxonomyFinancials
            .exportCompanyAssociatedEutaxonomyFinancialsDataByDimensions(
                companyIds = listOf(companyId),
                reportingPeriods = listOf(reportingPeriod),
                fileFormat = ExportFileType.CSV,
                keepValueFieldsOnly = false,
            ).let { assert(it.readBytes().isEmpty()) }

        apiAccessor.companyDataControllerApi
            .getAggregatedFrameworkDataSummary(
                companyId = companyId,
            ).forEach { framework, aggregatedFrameworkDataSummary ->
                if (framework == DataTypeEnum.eutaxonomyMinusNonMinusFinancials.toString()) {
                    assertEquals(1, aggregatedFrameworkDataSummary.numberOfProvidedReportingPeriods)
                } else {
                    assertEquals(0, aggregatedFrameworkDataSummary.numberOfProvidedReportingPeriods)
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
