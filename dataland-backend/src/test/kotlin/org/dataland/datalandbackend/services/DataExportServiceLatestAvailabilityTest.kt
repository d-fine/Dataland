package org.dataland.datalandbackend.services

import org.dataland.datalandbackend.frameworks.lksg.model.LksgData
import org.dataland.datalandbackend.model.DataDimensionQuery
import org.dataland.datalandbackend.model.PlainDataAndDimensions
import org.dataland.datalandbackend.model.enums.export.ExportJobProgressState
import org.dataland.datalandbackend.model.export.ExportAvailability
import org.dataland.datalandbackendutils.model.BasicDataDimensions
import org.dataland.datalandbackendutils.model.BasicDatasetDimensions
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.eq
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

/**
 * Tests for the availability / non-sourceability behavior of [DataExportService] for the "latest" export path
 * ([DataExportService.startLatestExportJob]), i.e. how the "latest" data dimension per company is determined by
 * reconciling the latest real dataset against the latest active non-sourceability entry.
 *
 * See [DataExportServiceDimensionAvailabilityTest] for the equivalent tests for the dimension-based export path.
 */
class DataExportServiceLatestAvailabilityTest : DataExportServiceAvailabilityTestBase() {
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
            lksgExportOptions,
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
            lksgExportOptions,
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
            lksgExportOptions,
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
            lksgExportOptions,
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
            lksgExportOptions,
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
            lksgExportOptions,
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
            lksgExportOptions,
        )

        verify(mockNonSourceabilityInformationManager).searchActiveNonSourceableDimensions(any())
    }

    @Test
    fun `check that a latest-mode portfolio consisting entirely of non-sourceable companies can be exported successfully`() {
        mockCompanyInformationLookupForTwoCompanies()
        whenever(mockDatasetStorageService.getLatestAvailableData(any(), any(), any()))
            .doReturn(emptyList())
        whenever(mockNonSourceabilityInformationManager.searchActiveNonSourceableDimensions(any()))
            .doReturn(
                setOf(
                    BasicDataDimensions(nonSourceableTestCompanyId, "lksg", TEST_REPORTING_PERIOD),
                    BasicDataDimensions(secondTestCompanyId, "lksg", TEST_REPORTING_PERIOD),
                ),
            )

        val exportJob = newExportJob()
        dataExportService.startLatestExportJob(
            listOf(nonSourceableTestCompanyId, secondTestCompanyId),
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
    fun `check that a mixed latest-mode portfolio with one available and one non-sourceable company is exported correctly`() {
        mockCompanyInformationLookupForTwoCompanies()
        val availableDimensions = BasicDatasetDimensions(nonSourceableTestCompanyId, "lksg", TEST_REPORTING_PERIOD)
        whenever(mockDatasetStorageService.getLatestAvailableData(any(), any(), any()))
            .doReturn(
                listOf(
                    PlainDataAndDimensions(
                        dimensions = availableDimensions,
                        data = objectMapper.writeValueAsString(testDataProvider.getLksgDataset()),
                    ),
                ),
            )
        whenever(mockNonSourceabilityInformationManager.searchActiveNonSourceableDimensions(any()))
            .doReturn(setOf(BasicDataDimensions(secondTestCompanyId, "lksg", TEST_REPORTING_PERIOD)))

        val exportJob = newExportJob()
        dataExportService.startLatestExportJob(
            listOf(nonSourceableTestCompanyId, secondTestCompanyId),
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
