package org.dataland.datalandbackend.services

import com.fasterxml.jackson.module.kotlin.readValue
import org.dataland.datalandbackend.entities.BasicCompanyInformation
import org.dataland.datalandbackend.frameworks.lksg.model.LksgData
import org.dataland.datalandbackend.model.DataDimensionQuery
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.model.PlainDataAndDimensions
import org.dataland.datalandbackend.model.enums.export.ExportJobProgressState
import org.dataland.datalandbackend.model.export.ExportAvailability
import org.dataland.datalandbackend.model.export.ExportJob
import org.dataland.datalandbackend.model.export.ExportOptions
import org.dataland.datalandbackend.model.export.SingleCompanyExportData
import org.dataland.datalandbackend.services.datapoints.DatasetAssembler
import org.dataland.datalandbackend.utils.TestDataProvider
import org.dataland.datalandbackendutils.model.BasicDataDimensions
import org.dataland.datalandbackendutils.model.BasicDatasetDimensions
import org.dataland.datalandbackendutils.model.ExportFileType
import org.dataland.datalandbackendutils.utils.JsonUtils
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.UUID

/**
 * Tests for the availability / non-sourceability behavior of [DataExportService] for the "latest" export path
 * ([DataExportService.startLatestExportJob]), i.e. how the "latest" data dimension per company is determined by
 * reconciling the latest real dataset against the latest active non-sourceability entry.
 *
 * See [DataExportServiceDimensionAvailabilityTest] for the equivalent tests for the dimension-based export path.
 */
class DataExportServiceLatestAvailabilityTest {
    private val objectMapper = JsonUtils.defaultObjectMapper
    private val mockDatasetAssembler = mock<DatasetAssembler>()
    private val mockSpecificationService = mock<SpecificationService>()
    private val mockCompanyQueryManager = mock<CompanyQueryManager>()
    private val mockDatasetStorageService = mock<DatasetStorageService>()
    private val mockNonSourceabilityInformationManager = mock<NonSourceabilityInformationManager>()
    private val dataExportService =
        DataExportService<LksgData>(
            mockDatasetAssembler,
            mockSpecificationService,
            mockCompanyQueryManager,
            mockDatasetStorageService,
            mockNonSourceabilityInformationManager,
        )

    private val testDataProvider = TestDataProvider(objectMapper)

    private val nonSourceableTestCompanyId = UUID.randomUUID().toString()
    private val nonSourceableTestCompanyInfo =
        BasicCompanyInformation(
            companyId = nonSourceableTestCompanyId,
            companyName = TEST_COMPANY_NAME,
            headquarters = "Test City",
            countryCode = "DE",
            sector = null,
            lei = TEST_COMPANY_LEI,
        )

    private fun newExportJob(): ExportJob =
        ExportJob(
            id = UUID.randomUUID(),
            fileToExport = null,
            fileType = ExportFileType.JSON,
            frameworkName = "lksg",
            progressState = ExportJobProgressState.Pending,
            creationTime = 0L,
        )

    private fun readExportedRows(exportJob: ExportJob): List<SingleCompanyExportData<LksgData>> =
        objectMapper.readValue<List<SingleCompanyExportData<LksgData>>>(exportJob.fileToExport!!.inputStream)

    private fun mockCompanyInformationLookup() {
        whenever(mockCompanyQueryManager.getBasicCompanyInformationByIds(any()))
            .doReturn(mapOf(nonSourceableTestCompanyId to nonSourceableTestCompanyInfo))
    }

    @Test
    fun `check that latest export marks a company with a latest dataset as available`() {
        mockCompanyInformationLookup()
        val dimensions = BasicDatasetDimensions(nonSourceableTestCompanyId, "lksg", TEST_REPORTING_PERIOD)
        whenever(mockDatasetStorageService.getLatestAvailableData(any(), any(), any()))
            .doReturn(
                listOf(
                    PlainDataAndDimensions(
                        dimensions = dimensions,
                        data = objectMapper.writeValueAsString(testDataProvider.getLksgDataset()),
                    ),
                ),
            )

        val exportJob = newExportJob()
        dataExportService.startLatestExportJob(
            listOf(nonSourceableTestCompanyId),
            exportJob,
            LksgData::class.java,
            ExportOptions(DataType.valueOf("lksg"), ExportFileType.JSON, keepValueFieldsOnly = true, includeAliases = false),
        )

        val exportedRows = readExportedRows(exportJob)

        Assertions.assertEquals(1, exportedRows.size)
        Assertions.assertEquals(ExportAvailability.AVAILABLE, exportedRows.first().availability)
    }

    @Test
    fun `check that latest export marks a company with no dataset but an active non-sourceability entry as non-sourceable`() {
        mockCompanyInformationLookup()
        whenever(mockDatasetStorageService.getLatestAvailableData(any(), any(), any()))
            .doReturn(emptyList())
        whenever(
            mockNonSourceabilityInformationManager.searchActiveNonSourceableDimensions(
                eq(DataDimensionQuery(companyIds = listOf(nonSourceableTestCompanyId), dataTypes = listOf("lksg"))),
            ),
        ).doReturn(setOf(BasicDataDimensions(nonSourceableTestCompanyId, "lksg", TEST_REPORTING_PERIOD)))

        val exportJob = newExportJob()
        dataExportService.startLatestExportJob(
            listOf(nonSourceableTestCompanyId),
            exportJob,
            LksgData::class.java,
            ExportOptions(DataType.valueOf("lksg"), ExportFileType.JSON, keepValueFieldsOnly = true, includeAliases = false),
        )

        val exportedRows = readExportedRows(exportJob)

        Assertions.assertEquals(1, exportedRows.size)
        val row = exportedRows.first()
        Assertions.assertEquals(ExportAvailability.NON_SOURCEABLE, row.availability)
        Assertions.assertNull(row.data)
        Assertions.assertEquals(TEST_REPORTING_PERIOD, row.reportingPeriod)
    }

    @Test
    fun `check that latest export omits a company with neither a dataset nor a non-sourceability entry`() {
        mockCompanyInformationLookup()
        whenever(mockDatasetStorageService.getLatestAvailableData(any(), any(), any()))
            .doReturn(emptyList())
        whenever(mockNonSourceabilityInformationManager.searchActiveNonSourceableDimensions(any()))
            .doReturn(emptySet())

        val exportJob = newExportJob()

        dataExportService.startLatestExportJob(
            listOf(nonSourceableTestCompanyId),
            exportJob,
            LksgData::class.java,
            ExportOptions(DataType.valueOf("lksg"), ExportFileType.JSON, keepValueFieldsOnly = true, includeAliases = false),
        )

        Assertions.assertEquals(ExportJobProgressState.Failure, exportJob.progressState)
        Assertions.assertNull(exportJob.fileToExport)
    }

    @Test
    fun `check that latest export selects only the latest non-sourceable period when multiple exist for the same company`() {
        mockCompanyInformationLookup()
        whenever(mockDatasetStorageService.getLatestAvailableData(any(), any(), any()))
            .doReturn(emptyList())
        whenever(
            mockNonSourceabilityInformationManager.searchActiveNonSourceableDimensions(
                eq(DataDimensionQuery(companyIds = listOf(nonSourceableTestCompanyId), dataTypes = listOf("lksg"))),
            ),
        ).doReturn(
            setOf(
                BasicDataDimensions(nonSourceableTestCompanyId, "lksg", "2021"),
                BasicDataDimensions(nonSourceableTestCompanyId, "lksg", "2023"),
                BasicDataDimensions(nonSourceableTestCompanyId, "lksg", "2022"),
            ),
        )

        val exportJob = newExportJob()
        dataExportService.startLatestExportJob(
            listOf(nonSourceableTestCompanyId),
            exportJob,
            LksgData::class.java,
            ExportOptions(DataType.valueOf("lksg"), ExportFileType.JSON, keepValueFieldsOnly = true, includeAliases = false),
        )

        val exportedRows = readExportedRows(exportJob)

        Assertions.assertEquals(1, exportedRows.size)
        val row = exportedRows.first()
        Assertions.assertEquals("2023", row.reportingPeriod)
        Assertions.assertEquals(ExportAvailability.NON_SOURCEABLE, row.availability)
        Assertions.assertNull(row.data)
    }

    @Test
    fun `check that latest export prefers a newer non-sourceable period over older real data`() {
        mockCompanyInformationLookup()
        val olderRealDataPeriod = "2022"
        val newerNonSourceablePeriod = "2023"
        val realDataDimensions = BasicDatasetDimensions(nonSourceableTestCompanyId, "lksg", olderRealDataPeriod)

        whenever(mockDatasetStorageService.getLatestAvailableData(any(), any(), any()))
            .doReturn(
                listOf(
                    PlainDataAndDimensions(
                        dimensions = realDataDimensions,
                        data = objectMapper.writeValueAsString(testDataProvider.getLksgDataset()),
                    ),
                ),
            )
        whenever(
            mockNonSourceabilityInformationManager.searchActiveNonSourceableDimensions(
                eq(DataDimensionQuery(companyIds = listOf(nonSourceableTestCompanyId), dataTypes = listOf("lksg"))),
            ),
        ).doReturn(setOf(BasicDataDimensions(nonSourceableTestCompanyId, "lksg", newerNonSourceablePeriod)))

        val exportJob = newExportJob()
        dataExportService.startLatestExportJob(
            listOf(nonSourceableTestCompanyId),
            exportJob,
            LksgData::class.java,
            ExportOptions(DataType.valueOf("lksg"), ExportFileType.JSON, keepValueFieldsOnly = true, includeAliases = false),
        )

        val exportedRows = readExportedRows(exportJob)

        Assertions.assertEquals(1, exportedRows.size)
        val row = exportedRows.first()
        Assertions.assertEquals(newerNonSourceablePeriod, row.reportingPeriod)
        Assertions.assertEquals(ExportAvailability.NON_SOURCEABLE, row.availability)
        Assertions.assertNull(row.data)
    }

    @Test
    fun `check that latest export prefers real data over a non-sourceable period that is not newer`() {
        mockCompanyInformationLookup()
        val realDataPeriod = "2023"
        val olderNonSourceablePeriod = "2022"
        val realDataDimensions = BasicDatasetDimensions(nonSourceableTestCompanyId, "lksg", realDataPeriod)

        whenever(mockDatasetStorageService.getLatestAvailableData(any(), any(), any()))
            .doReturn(
                listOf(
                    PlainDataAndDimensions(
                        dimensions = realDataDimensions,
                        data = objectMapper.writeValueAsString(testDataProvider.getLksgDataset()),
                    ),
                ),
            )
        whenever(
            mockNonSourceabilityInformationManager.searchActiveNonSourceableDimensions(
                eq(DataDimensionQuery(companyIds = listOf(nonSourceableTestCompanyId), dataTypes = listOf("lksg"))),
            ),
        ).doReturn(
            setOf(
                BasicDataDimensions(nonSourceableTestCompanyId, "lksg", olderNonSourceablePeriod),
                BasicDataDimensions(nonSourceableTestCompanyId, "lksg", realDataPeriod),
            ),
        )

        val exportJob = newExportJob()
        dataExportService.startLatestExportJob(
            listOf(nonSourceableTestCompanyId),
            exportJob,
            LksgData::class.java,
            ExportOptions(DataType.valueOf("lksg"), ExportFileType.JSON, keepValueFieldsOnly = true, includeAliases = false),
        )

        val exportedRows = readExportedRows(exportJob)

        Assertions.assertEquals(1, exportedRows.size)
        val row = exportedRows.first()
        Assertions.assertEquals(realDataPeriod, row.reportingPeriod)
        Assertions.assertEquals(ExportAvailability.AVAILABLE, row.availability)
        Assertions.assertNotNull(row.data)
    }

    @Test
    fun `check that non-sourceability lookup is invoked in latest mode even when a company already has real data`() {
        mockCompanyInformationLookup()
        val dimensions = BasicDatasetDimensions(nonSourceableTestCompanyId, "lksg", TEST_REPORTING_PERIOD)
        whenever(mockDatasetStorageService.getLatestAvailableData(any(), any(), any()))
            .doReturn(
                listOf(
                    PlainDataAndDimensions(
                        dimensions = dimensions,
                        data = objectMapper.writeValueAsString(testDataProvider.getLksgDataset()),
                    ),
                ),
            )
        whenever(mockNonSourceabilityInformationManager.searchActiveNonSourceableDimensions(any()))
            .doReturn(emptySet())

        val exportJob = newExportJob()
        dataExportService.startLatestExportJob(
            listOf(nonSourceableTestCompanyId),
            exportJob,
            LksgData::class.java,
            ExportOptions(DataType.valueOf("lksg"), ExportFileType.JSON, keepValueFieldsOnly = true, includeAliases = false),
        )

        verify(mockNonSourceabilityInformationManager).searchActiveNonSourceableDimensions(any())
    }

    @Test
    fun `check that a latest-mode portfolio consisting entirely of non-sourceable companies can be exported successfully`() {
        val secondCompanyId = UUID.randomUUID().toString()
        val secondCompanyInfo =
            BasicCompanyInformation(
                companyId = secondCompanyId,
                companyName = "second test company",
                headquarters = "Test City",
                countryCode = "DE",
                sector = null,
                lei = "second-test-lei",
            )
        whenever(mockCompanyQueryManager.getBasicCompanyInformationByIds(any()))
            .doReturn(
                mapOf(
                    nonSourceableTestCompanyId to nonSourceableTestCompanyInfo,
                    secondCompanyId to secondCompanyInfo,
                ),
            )
        whenever(mockDatasetStorageService.getLatestAvailableData(any(), any(), any()))
            .doReturn(emptyList())
        whenever(mockNonSourceabilityInformationManager.searchActiveNonSourceableDimensions(any()))
            .doReturn(
                setOf(
                    BasicDataDimensions(nonSourceableTestCompanyId, "lksg", TEST_REPORTING_PERIOD),
                    BasicDataDimensions(secondCompanyId, "lksg", TEST_REPORTING_PERIOD),
                ),
            )

        val exportJob = newExportJob()
        dataExportService.startLatestExportJob(
            listOf(nonSourceableTestCompanyId, secondCompanyId),
            exportJob,
            LksgData::class.java,
            ExportOptions(DataType.valueOf("lksg"), ExportFileType.JSON, keepValueFieldsOnly = true, includeAliases = false),
        )

        Assertions.assertEquals(ExportJobProgressState.Success, exportJob.progressState)
        val exportedRows = readExportedRows(exportJob)
        Assertions.assertEquals(2, exportedRows.size)
        Assertions.assertTrue(exportedRows.all { it.availability == ExportAvailability.NON_SOURCEABLE })
        Assertions.assertTrue(exportedRows.all { it.data == null })
    }
}
