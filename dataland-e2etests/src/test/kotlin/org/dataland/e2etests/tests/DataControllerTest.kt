package org.dataland.e2etests.tests

import okhttp3.OkHttpClient
import org.awaitility.core.ConditionTimeoutException
import org.dataland.communitymanager.openApiClient.model.CompanyRole
import org.dataland.datalandbackend.openApiClient.api.EutaxonomyNonFinancialsDataControllerApi
import org.dataland.datalandbackend.openApiClient.infrastructure.ClientException
import org.dataland.datalandbackend.openApiClient.model.CompanyAssociatedDataEutaxonomyNonFinancialsData
import org.dataland.datalandbackend.openApiClient.model.CurrencyDataPoint
import org.dataland.datalandbackend.openApiClient.model.DataAndMetaInformationSfdrData
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackend.openApiClient.model.EutaxonomyNonFinancialsData
import org.dataland.datalandbackend.openApiClient.model.EutaxonomyNonFinancialsRevenue
import org.dataland.datalandbackend.openApiClient.model.ExportFileType
import org.dataland.datalandbackend.openApiClient.model.ExportRequestData
import org.dataland.datalandbackend.openApiClient.model.LksgSocial
import org.dataland.datalandbackend.openApiClient.model.LksgSocialChildLabor
import org.dataland.datalandbackendutils.utils.JsonComparator
import org.dataland.e2etests.BASE_PATH_TO_DATALAND_BACKEND
import org.dataland.e2etests.auth.JwtAuthenticationHelper
import org.dataland.e2etests.auth.TechnicalUser
import org.dataland.e2etests.utils.ApiAccessor
import org.dataland.e2etests.utils.DocumentControllerApiAccessor
import org.dataland.e2etests.utils.ExceptionUtils.assertAccessDeniedWrapper
import org.dataland.e2etests.utils.api.ApiAwait
import org.dataland.e2etests.utils.assertEqualsByJsonComparator
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.springframework.http.HttpStatus
import java.math.BigDecimal
import java.util.UUID
import java.util.concurrent.TimeUnit

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class DataControllerTest {
    private val apiAccessor = ApiAccessor()
    private val documentManagerAccessor = DocumentControllerApiAccessor()
    private val dataReaderUserId = UUID.fromString(TechnicalUser.Reader.technicalUserId)

    val jwtHelper = JwtAuthenticationHelper()

    private val testDataEuTaxonomyNonFinancials =
        apiAccessor.testDataProviderForEuTaxonomyDataForNonFinancials.getTData(1).first()

    private val testDataLksg = apiAccessor.testDataProviderForLksgData.getTData(1).first()

    private val testCompanyInformation =
        apiAccessor.testDataProviderForEuTaxonomyDataForNonFinancials.getCompanyInformationWithoutIdentifiers(1).first()

    private val testCompanyInformationNonTeaser = testCompanyInformation.copy(isTeaserCompany = false)
    private val testCompanyInformationTeaser = testCompanyInformation.copy(isTeaserCompany = true)

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
            apiAccessor.dataControllerApiForEuTaxonomyNonFinancials.getCompanyAssociatedEutaxonomyNonFinancialsData(
                mapOfIds.getValue("dataId"),
            )

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
            apiAccessor.unauthorizedEuTaxonomyDataNonFinancialsControllerApi.getCompanyAssociatedDataEuTaxonomyDataForNonFinancials(
                mapOfIds.getValue("dataId"),
            )
        val expectedCompanyAssociatedData =
            CompanyAssociatedDataEutaxonomyNonFinancialsData(
                mapOfIds.getValue("companyId"),
                "",
                testDataEuTaxonomyNonFinancials,
            )

        val ignoredKeys = setOf("publicationDate")
        assertEqualsByJsonComparator(
            expectedCompanyAssociatedData, getDataByIdResponse,
            JsonComparator.JsonComparisonOptions(ignoredKeys),
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
                apiAccessor.unauthorizedEuTaxonomyDataNonFinancialsControllerApi.getCompanyAssociatedDataEuTaxonomyDataForNonFinancials(
                    mapOfIds.getValue("dataId"),
                )
            }

        assertTrue(exception.message!!.contains("code=403"))
    }

    @ParameterizedTest
    @EnumSource(CompanyRole::class)
    fun `check that keycloak reader role can only upload data as company owner or company data uploader`(role: CompanyRole) {
        val companyId =
            UUID.fromString(apiAccessor.uploadOneCompanyWithRandomIdentifier().actualStoredCompany.companyId)
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
            apiAccessor.dataControllerApiForLksgData.getCompanyAssociatedLksgData(dataId)
        }.let { assertEquals(400, it.statusCode) }

        listOf(
            apiAccessor.dataControllerApiForEuTaxonomyFinancials::getAllCompanyEutaxonomyFinancialsData,
            apiAccessor.dataControllerApiForSfdrData::getAllCompanySfdrData,
        ).forEach { getAllCompanyData -> assertEquals(0, getAllCompanyData(companyId, false, null).size) }

        apiAccessor.dataControllerApiForEuTaxonomyFinancials
            .exportCompanyAssociatedEutaxonomyFinancialsDataByDimensions(
                ExportRequestData(listOf(reportingPeriod), listOf(companyId), ExportFileType.CSV),
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

    @Test
    fun `test fetching of dataset of the latest available reporting period`() {
        val companyId = apiAccessor.uploadOneCompanyWithRandomIdentifier().actualStoredCompany.companyId

        mapOf(
            "2023" to "2023",
            "2024" to "2024",
            "2022" to "2024",
        ).forEach { (reportingPeriod, latestAvailableReportingPeriod) ->
            uploadLksgDataset(companyId, reportingPeriod)
            assertEquals(
                "Test Description $latestAvailableReportingPeriod",
                ApiAwait.waitForData {
                    apiAccessor.dataControllerApiForLksgData.getLatestAvailableCompanyAssociatedLksgData(companyId).let {
                        it.data.social
                            ?.childLabor
                            ?.additionalChildLaborOtherMeasuresDescription
                    }
                },
            )
        }
    }

    @Test
    fun `test fetching of assembled dataset of the latest available reporting period`() {
        val storedCompany = apiAccessor.uploadOneCompanyWithRandomIdentifier().actualStoredCompany
        val companyIdentifier =
            storedCompany.companyInformation.identifiers.values
                .first { it.isNotEmpty() }
                .first()

        mapOf(
            "2023" to "2023",
            "2024" to "2024",
            "2022" to "2024",
        ).forEach { (reportingPeriod, latestAvailableReportingPeriod) ->
            uploadEuTaxoDataset(
                UUID.fromString(storedCompany.companyId),
                reportingPeriod,
                testDataEuTaxonomyNonFinancials.copy(
                    revenue =
                        EutaxonomyNonFinancialsRevenue(
                            totalAmount = CurrencyDataPoint(value = BigDecimal(reportingPeriod)),
                        ),
                ),
                bypassQa = true,
            )
            ApiAwait.untilAsserted(retryOnHttpErrors = setOf(HttpStatus.NOT_FOUND)) {
                val latestResponse =
                    apiAccessor.dataControllerApiForEuTaxonomyNonFinancials.getLatestAvailableCompanyAssociatedEutaxonomyNonFinancialsData(
                        companyIdentifier,
                    )
                Assertions.assertNotNull(latestResponse, "Controller should not return null")
                assertEquals(
                    latestAvailableReportingPeriod,
                    latestResponse.data.revenue
                        ?.totalAmount
                        ?.value
                        ?.toPlainString(),
                )
            }
        }
    }

    @Test
    fun `verify that export supports a huge number of companies`() {
        val increasedTimeoutDataControllerApiForEuTaxonomyNonFinancials =
            EutaxonomyNonFinancialsDataControllerApi(
                BASE_PATH_TO_DATALAND_BACKEND,
                OkHttpClient
                    .Builder()
                    .connectTimeout(60, TimeUnit.SECONDS)
                    .readTimeout(60, TimeUnit.SECONDS)
                    .writeTimeout(60, TimeUnit.SECONDS)
                    .build(),
            )
        val dummyCompanyIds = List(3000) { UUID.randomUUID().toString() }
        assertThrows<ClientException> {
            increasedTimeoutDataControllerApiForEuTaxonomyNonFinancials
                .exportCompanyAssociatedEutaxonomyNonFinancialsDataByDimensions(
                    ExportRequestData(
                        companyIds = dummyCompanyIds,
                        reportingPeriods = listOf("2022"),
                        fileFormat = ExportFileType.CSV,
                    ),
                )
        }.also {
            assertEquals(404, it.statusCode)
        }
    }

    private fun uploadEuTaxoDataset(
        companyId: UUID,
        reportingPeriod: String = "2022",
        data: EutaxonomyNonFinancialsData = testDataEuTaxonomyNonFinancials,
        bypassQa: Boolean = false,
    ) {
        apiAccessor.euTaxonomyNonFinancialsUploaderFunction(
            companyId.toString(),
            data,
            reportingPeriod,
            bypassQa,
        )
    }

    private fun uploadLksgDataset(
        companyId: String,
        reportingPeriod: String,
    ) = apiAccessor
        .lksgUploaderFunction(
            companyId,
            testDataLksg.copy(
                social =
                    LksgSocial(
                        childLabor =
                            LksgSocialChildLabor(
                                additionalChildLaborOtherMeasuresDescription = "Test Description $reportingPeriod",
                            ),
                    ),
            ),
            reportingPeriod,
            bypassQa = true,
        ).dataId
}
