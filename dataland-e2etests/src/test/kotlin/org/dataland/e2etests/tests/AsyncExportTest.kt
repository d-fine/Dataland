package org.dataland.e2etests.tests

import org.awaitility.Awaitility
import org.dataland.datalandbackend.openApiClient.infrastructure.ClientException
import org.dataland.datalandbackend.openApiClient.model.ExportFileType
import org.dataland.datalandbackend.openApiClient.model.ExportJobProgressState
import org.dataland.datalandbackend.openApiClient.model.ExportRequestData
import org.dataland.e2etests.utils.ApiAccessor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import java.util.UUID
import java.util.concurrent.TimeUnit

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AsyncExportTest {
    private val apiAccessor = ApiAccessor()

    private val testDataEuTaxonomyNonFinancials =
        apiAccessor.testDataProviderForEuTaxonomyDataForNonFinancials.getTData(1).first()

    private lateinit var testCompanyId1: String
    private lateinit var testCompanyId2: String
    private lateinit var testCompanyId3: String
    private val testReportingPeriod = "2024"

    @BeforeAll
    fun setup() {
        val companies = apiAccessor.uploadNCompaniesWithoutIdentifiers(3)

        testCompanyId1 = companies[0].actualStoredCompany.companyId
        testCompanyId2 = companies[1].actualStoredCompany.companyId
        testCompanyId3 = companies[2].actualStoredCompany.companyId

        val dataId1 =
            apiAccessor
                .euTaxonomyNonFinancialsUploaderFunction(
                    testCompanyId1,
                    testDataEuTaxonomyNonFinancials,
                    testReportingPeriod,
                ).dataId

        val dataId2 =
            apiAccessor
                .euTaxonomyNonFinancialsUploaderFunction(
                    testCompanyId2,
                    testDataEuTaxonomyNonFinancials,
                    testReportingPeriod,
                ).dataId

        val dataId3 =
            apiAccessor
                .euTaxonomyNonFinancialsUploaderFunction(
                    testCompanyId3,
                    testDataEuTaxonomyNonFinancials,
                    testReportingPeriod,
                ).dataId

        Awaitility
            .await()
            .atMost(10, TimeUnit.SECONDS)
            .pollInterval(500, TimeUnit.MILLISECONDS)
            .untilAsserted {
                apiAccessor.dataControllerApiForEuTaxonomyNonFinancials.getCompanyAssociatedEutaxonomyNonFinancialsData(
                    dataId1,
                )
                apiAccessor.dataControllerApiForEuTaxonomyNonFinancials.getCompanyAssociatedEutaxonomyNonFinancialsData(
                    dataId2,
                )
                apiAccessor.dataControllerApiForEuTaxonomyNonFinancials.getCompanyAssociatedEutaxonomyNonFinancialsData(
                    dataId3,
                )
            }
    }

    @Test
    fun `export job lifecycle completes successfully with multiple companies`() {
        val jobResponse =
            apiAccessor.dataControllerApiForEuTaxonomyNonFinancials.postExportJobCompanyAssociatedEutaxonomyNonFinancialsDataByDimensions(
                ExportRequestData(
                    companyIds = listOf(testCompanyId1, testCompanyId2, testCompanyId3),
                    reportingPeriods = listOf(testReportingPeriod),
                    fileFormat = ExportFileType.CSV,
                ),
            )

        assertNotNull(jobResponse.id)
        val exportJobId = jobResponse.id.toString()

        Awaitility
            .await()
            .atMost(30, TimeUnit.SECONDS)
            .pollInterval(500, TimeUnit.MILLISECONDS)
            .untilAsserted {
                val state =
                    apiAccessor.dataControllerApiForEuTaxonomyNonFinancials.getExportJobState(
                        exportJobId = exportJobId,
                    )
                assertEquals(ExportJobProgressState.Success, state)
            }

        val exportResponse =
            apiAccessor.dataControllerApiForEuTaxonomyNonFinancials.exportCompanyAssociatedEutaxonomyNonFinancialsDataById(
                exportJobId = exportJobId,
            )

        assertNotNull(exportResponse)
    }

    @Test
    fun `export job state returns 404 for non-existent job ID`() {
        val nonExistentJobId = UUID.randomUUID().toString()

        assertThrows<ClientException> {
            apiAccessor.dataControllerApiForEuTaxonomyNonFinancials.getExportJobState(
                exportJobId = nonExistentJobId,
            )
        }.also {
            assertEquals(404, it.statusCode)
        }
    }

    @Test
    fun `export download returns 404 for non-existent job ID`() {
        val nonExistentJobId = UUID.randomUUID().toString()

        assertThrows<ClientException> {
            apiAccessor.dataControllerApiForEuTaxonomyNonFinancials.exportCompanyAssociatedEutaxonomyNonFinancialsDataById(
                exportJobId = nonExistentJobId,
            )
        }.also {
            assertEquals(404, it.statusCode)
        }
    }

    @Test
    fun `user cannot access export job from different user`() {
        val jobResponse =
            apiAccessor.dataControllerApiForEuTaxonomyNonFinancials.postExportJobCompanyAssociatedEutaxonomyNonFinancialsDataByDimensions(
                ExportRequestData(
                    companyIds = listOf(testCompanyId1),
                    reportingPeriods = listOf(testReportingPeriod),
                    fileFormat = ExportFileType.CSV,
                ),
            )

        val exportJobId = jobResponse.id.toString()

        Awaitility
            .await()
            .atMost(30, TimeUnit.SECONDS)
            .pollInterval(500, TimeUnit.MILLISECONDS)
            .untilAsserted {
                val state =
                    apiAccessor.dataControllerApiForEuTaxonomyNonFinancials.getExportJobState(
                        exportJobId = exportJobId,
                    )
                assertEquals(ExportJobProgressState.Success, state)
            }

        assertThrows<ClientException> {
            apiAccessor.unauthorizedEuTaxonomyDataNonFinancialsControllerApi.getExportJobState(
                exportJobId = exportJobId,
            )
        }.also {
            assertEquals(403, it.statusCode)
        }

        assertThrows<ClientException> {
            apiAccessor.unauthorizedEuTaxonomyDataNonFinancialsControllerApi.exportCompanyAssociatedEutaxonomyNonFinancialsDataById(
                exportJobId = exportJobId,
            )
        }.also {
            assertEquals(403, it.statusCode)
        }
    }
}
