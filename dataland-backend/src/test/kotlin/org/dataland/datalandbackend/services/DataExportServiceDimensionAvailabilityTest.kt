package org.dataland.datalandbackend.services

import org.dataland.datalandbackend.frameworks.lksg.model.LksgData
import org.dataland.datalandbackend.model.enums.export.ExportJobProgressState
import org.dataland.datalandbackend.model.export.ExportAvailability
import org.dataland.datalandbackendutils.model.BasicDataDimensions
import org.dataland.datalandbackendutils.model.BasicDatasetDimensions
import org.dataland.datalandbackendutils.model.ListDataDimensions
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * Tests for the availability / non-sourceability behavior of [DataExportService] for the dimension-based
 * export path ([DataExportService.startExportJob]), i.e. how the export handles data dimensions for which no
 * dataset is available but which are confirmed as non-sourceable.
 *
 * See [DataExportServiceLatestAvailabilityTest] for the equivalent tests for the "latest" export path.
 */
class DataExportServiceDimensionAvailabilityTest : DataExportServiceAvailabilityTestBase() {
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
            lksgExportOptions,
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
            lksgExportOptions,
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
            lksgExportOptions,
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
            lksgExportOptions,
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
            lksgExportOptions,
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
    fun `check that a dimension-based portfolio consisting entirely of non-sourceable companies can be exported successfully`() {
        mockCompanyInformationLookupForTwoCompanies()
        whenever(mockDatasetStorageService.getDatasetData(any(), any())).doReturn(emptyMap())
        whenever(mockNonSourceabilityInformationManager.searchActiveNonSourceableDimensions(any()))
            .doReturn(
                setOf(
                    BasicDataDimensions(nonSourceableTestCompanyId, "lksg", TEST_REPORTING_PERIOD),
                    BasicDataDimensions(secondTestCompanyId, "lksg", TEST_REPORTING_PERIOD),
                ),
            )

        val exportJob = newExportJob()
        dataExportService.startExportJob(
            ListDataDimensions(
                companyIds = listOf(nonSourceableTestCompanyId, secondTestCompanyId),
                reportingPeriods = listOf(TEST_REPORTING_PERIOD),
                dataTypes = listOf("lksg"),
            ),
            exportJob,
            LksgData::class.java,
            lksgExportOptions,
        )

        Assertions.assertEquals(ExportJobProgressState.Success, exportJob.progressState)
        val exportedRows = readExportedRows(exportJob)
        Assertions.assertEquals(2, exportedRows.size)
        Assertions.assertTrue(exportedRows.all { it.availability == ExportAvailability.NON_SOURCEABLE })
        Assertions.assertTrue(exportedRows.all { it.data == null })
    }

    @Test
    fun `check that a mixed dimension-based portfolio with one available and one non-sourceable company is exported correctly`() {
        mockCompanyInformationLookupForTwoCompanies()
        val availableDimensions = BasicDatasetDimensions(nonSourceableTestCompanyId, "lksg", TEST_REPORTING_PERIOD)
        whenever(mockDatasetStorageService.getDatasetData(any(), any()))
            .doReturn(mapOf(availableDimensions to objectMapper.writeValueAsString(testDataProvider.getLksgDataset())))
        whenever(mockNonSourceabilityInformationManager.searchActiveNonSourceableDimensions(any()))
            .doReturn(setOf(BasicDataDimensions(secondTestCompanyId, "lksg", TEST_REPORTING_PERIOD)))

        val exportJob = newExportJob()
        dataExportService.startExportJob(
            ListDataDimensions(
                companyIds = listOf(nonSourceableTestCompanyId, secondTestCompanyId),
                reportingPeriods = listOf(TEST_REPORTING_PERIOD),
                dataTypes = listOf("lksg"),
            ),
            exportJob,
            LksgData::class.java,
            lksgExportOptions,
        )

        Assertions.assertEquals(ExportJobProgressState.Success, exportJob.progressState)
        val exportedRows = readExportedRows(exportJob)
        Assertions.assertEquals(2, exportedRows.size)

        val availableRow = exportedRows.first { it.companyLei == TEST_COMPANY_LEI }
        val nonSourceableRow = exportedRows.first { it.companyLei == secondTestCompanyInfo.lei }

        Assertions.assertEquals(ExportAvailability.AVAILABLE, availableRow.availability)
        Assertions.assertNotNull(availableRow.data)
        Assertions.assertEquals(ExportAvailability.NON_SOURCEABLE, nonSourceableRow.availability)
        Assertions.assertNull(nonSourceableRow.data)
    }
}
