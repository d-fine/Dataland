package org.dataland.datalandbackend.services

import com.fasterxml.jackson.module.kotlin.readValue
import org.dataland.datalandbackend.entities.BasicCompanyInformation
import org.dataland.datalandbackend.frameworks.lksg.model.LksgData
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.model.enums.export.ExportJobProgressState
import org.dataland.datalandbackend.model.export.ExportJob
import org.dataland.datalandbackend.model.export.ExportOptions
import org.dataland.datalandbackend.model.export.SingleCompanyExportData
import org.dataland.datalandbackend.services.datapoints.DatasetAssembler
import org.dataland.datalandbackend.utils.TestDataProvider
import org.dataland.datalandbackendutils.model.ExportFileType
import org.dataland.datalandbackendutils.utils.JsonUtils
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.util.UUID

/**
 * Shared setup for [DataExportService] availability/non-sourceability tests, split across
 * [DataExportServiceDimensionAvailabilityTest] and [DataExportServiceLatestAvailabilityTest].
 */
abstract class DataExportServiceAvailabilityTestBase {
    protected val objectMapper = JsonUtils.defaultObjectMapper
    protected val mockDatasetAssembler = mock<DatasetAssembler>()
    protected val mockSpecificationService = mock<SpecificationService>()
    protected val mockCompanyQueryManager = mock<CompanyQueryManager>()
    protected val mockDatasetStorageService = mock<DatasetStorageService>()
    protected val mockNonSourceabilityInformationManager = mock<NonSourceabilityInformationManager>()
    protected val dataExportService =
        DataExportService<LksgData>(
            mockDatasetAssembler,
            mockSpecificationService,
            mockCompanyQueryManager,
            mockDatasetStorageService,
            mockNonSourceabilityInformationManager,
        )

    protected val testDataProvider = TestDataProvider(objectMapper)

    protected val nonSourceableTestCompanyId = UUID.randomUUID().toString()
    protected val nonSourceableTestCompanyInfo =
        BasicCompanyInformation(
            companyId = nonSourceableTestCompanyId,
            companyName = TEST_COMPANY_NAME,
            headquarters = "Test City",
            countryCode = "DE",
            sector = null,
            lei = TEST_COMPANY_LEI,
        )

    /**
     * The [ExportOptions] used by all lksg-based tests in this hierarchy: JSON export, keeping only value fields
     * and without alias renaming, so tests can deserialize and inspect the exported rows directly.
     */
    protected val lksgExportOptions =
        ExportOptions(DataType.valueOf("lksg"), ExportFileType.JSON, keepValueFieldsOnly = true, includeAliases = false)

    protected fun newExportJob(): ExportJob =
        ExportJob(
            id = UUID.randomUUID(),
            fileToExport = null,
            fileType = ExportFileType.JSON,
            frameworkName = "lksg",
            progressState = ExportJobProgressState.Pending,
            creationTime = 0L,
        )

    protected fun readExportedRows(exportJob: ExportJob): List<SingleCompanyExportData<LksgData>> =
        objectMapper.readValue<List<SingleCompanyExportData<LksgData>>>(exportJob.fileToExport!!.inputStream)

    protected fun mockCompanyInformationLookup() {
        whenever(mockCompanyQueryManager.getBasicCompanyInformationByIds(any()))
            .doReturn(mapOf(nonSourceableTestCompanyId to nonSourceableTestCompanyInfo))
    }
}
