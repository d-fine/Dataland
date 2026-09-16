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
import org.dataland.datalandbackendutils.model.ListDataDimensions
import org.dataland.datalandbackendutils.utils.JsonUtils
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.UUID

/**
 * Tests for the availability / non-sourceability behavior of [DataExportService], i.e. how the export
 * handles data dimensions for which no dataset is available but which are confirmed as non-sourceable.
 */
class DataExportServiceAvailabilityTest {
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
    fun `check that a missing dimension with an active non-sourceability entry produces a synthetic row`() {
        mockCompanyInformationLookup()
        whenever(mockDatasetStorageService.getDatasetData(any(), any())).doReturn(emptyMap())
        whenever(mockNonSourceabilityInformationManager.searchActiveNonSourceableDimensions(any()))
            .doReturn(setOf(BasicDataDimensions(nonSourceableTestCompanyId, "lksg", TEST_REPORTING_PERIOD)))

        val exportJob = newExportJob()
        dataExportService.startExportJob(
            ListDataDimensions(
                companyIds = listOf(nonSourceableTestCompanyId),
                reportingPeriods = listOf(TEST_REPORTING_PERIOD),
                dataTypes = listOf("lksg"),
            ),
            exportJob,
            LksgData::class.java,
            ExportOptions(DataType.valueOf("lksg"), ExportFileType.JSON, keepValueFieldsOnly = true, includeAliases = false),
        )

        val exportedRows = readExportedRows(exportJob)

        Assertions.assertEquals(1, exportedRows.size)
        val row = exportedRows.first()
        Assertions.assertEquals(ExportAvailability.NON_SOURCEABLE, row.availability)
        Assertions.assertNull(row.data)
        Assertions.assertEquals(TEST_COMPANY_NAME, row.companyName)
        Assertions.assertEquals(TEST_COMPANY_LEI, row.companyLei)
        Assertions.assertEquals(TEST_REPORTING_PERIOD, row.reportingPeriod)
    }

    @Test
    fun `check that a missing dimension without a non-sourceability entry is still omitted`() {
        mockCompanyInformationLookup()
        whenever(mockDatasetStorageService.getDatasetData(any(), any())).doReturn(emptyMap())
        whenever(mockNonSourceabilityInformationManager.searchActiveNonSourceableDimensions(any()))
            .doReturn(emptySet())

        val exportJob = newExportJob()

        dataExportService.startExportJob(
            ListDataDimensions(
                companyIds = listOf(nonSourceableTestCompanyId),
                reportingPeriods = listOf(TEST_REPORTING_PERIOD),
                dataTypes = listOf("lksg"),
            ),
            exportJob,
            LksgData::class.java,
            ExportOptions(DataType.valueOf("lksg"), ExportFileType.JSON, keepValueFieldsOnly = true, includeAliases = false),
        )

        Assertions.assertEquals(ExportJobProgressState.Failure, exportJob.progressState)
        Assertions.assertNull(exportJob.fileToExport)
    }

    @Test
    fun `check that availability is available for present dimensions even with zero non-sourceability entries`() {
        mockCompanyInformationLookup()
        val dimensions = BasicDatasetDimensions(nonSourceableTestCompanyId, "lksg", TEST_REPORTING_PERIOD)
        whenever(mockDatasetStorageService.getDatasetData(any(), any()))
            .doReturn(mapOf(dimensions to objectMapper.writeValueAsString(testDataProvider.getLksgDataset())))
        whenever(mockNonSourceabilityInformationManager.searchActiveNonSourceableDimensions(any()))
            .doReturn(emptySet())

        val exportJob = newExportJob()
        dataExportService.startExportJob(
            ListDataDimensions(
                companyIds = listOf(nonSourceableTestCompanyId),
                reportingPeriods = listOf(TEST_REPORTING_PERIOD),
                dataTypes = listOf("lksg"),
            ),
            exportJob,
            LksgData::class.java,
            ExportOptions(DataType.valueOf("lksg"), ExportFileType.JSON, keepValueFieldsOnly = true, includeAliases = false),
        )

        val exportedRows = readExportedRows(exportJob)

        Assertions.assertEquals(1, exportedRows.size)
        Assertions.assertEquals(ExportAvailability.AVAILABLE, exportedRows.first().availability)
        Assertions.assertNotNull(exportedRows.first().data)
    }

    @Test
    fun `check that non-sourceability lookup is never invoked when no dimensions are missing`() {
        mockCompanyInformationLookup()
        val dimensions = BasicDatasetDimensions(nonSourceableTestCompanyId, "lksg", TEST_REPORTING_PERIOD)
        whenever(mockDatasetStorageService.getDatasetData(any(), any()))
            .doReturn(mapOf(dimensions to objectMapper.writeValueAsString(testDataProvider.getLksgDataset())))

        val exportJob = newExportJob()
        dataExportService.startExportJob(
            ListDataDimensions(
                companyIds = listOf(nonSourceableTestCompanyId),
                reportingPeriods = listOf(TEST_REPORTING_PERIOD),
                dataTypes = listOf("lksg"),
            ),
            exportJob,
            LksgData::class.java,
            ExportOptions(DataType.valueOf("lksg"), ExportFileType.JSON, keepValueFieldsOnly = true, includeAliases = false),
        )

        verify(mockNonSourceabilityInformationManager, never()).searchActiveNonSourceableDimensions(any())
    }

    @Test
    fun `check mixed available and non-sourceable periods for the same company are exported correctly`() {
        mockCompanyInformationLookup()
        val availablePeriod = "2024"
        val nonSourceablePeriod = "2023"
        val availableDimensions = BasicDatasetDimensions(nonSourceableTestCompanyId, "lksg", availablePeriod)

        whenever(mockDatasetStorageService.getDatasetData(any(), any()))
            .doReturn(mapOf(availableDimensions to objectMapper.writeValueAsString(testDataProvider.getLksgDataset())))
        whenever(mockNonSourceabilityInformationManager.searchActiveNonSourceableDimensions(any()))
            .doReturn(setOf(BasicDataDimensions(nonSourceableTestCompanyId, "lksg", nonSourceablePeriod)))

        val exportJob = newExportJob()
        dataExportService.startExportJob(
            ListDataDimensions(
                companyIds = listOf(nonSourceableTestCompanyId),
                reportingPeriods = listOf(availablePeriod, nonSourceablePeriod),
                dataTypes = listOf("lksg"),
            ),
            exportJob,
            LksgData::class.java,
            ExportOptions(DataType.valueOf("lksg"), ExportFileType.JSON, keepValueFieldsOnly = true, includeAliases = false),
        )

        val exportedRows = readExportedRows(exportJob)

        Assertions.assertEquals(2, exportedRows.size)
        val availableRow = exportedRows.first { it.reportingPeriod == availablePeriod }
        val nonSourceableRow = exportedRows.first { it.reportingPeriod == nonSourceablePeriod }

        Assertions.assertEquals(ExportAvailability.AVAILABLE, availableRow.availability)
        Assertions.assertNotNull(availableRow.data)
        Assertions.assertEquals(ExportAvailability.NON_SOURCEABLE, nonSourceableRow.availability)
        Assertions.assertNull(nonSourceableRow.data)
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
}
