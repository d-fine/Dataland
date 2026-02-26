package org.dataland.e2etests.utils

import org.awaitility.Awaitility
import org.dataland.datalandbackend.openApiClient.model.ExportFileType
import org.dataland.datalandbackend.openApiClient.model.ExportJobInfo
import org.dataland.datalandbackend.openApiClient.model.ExportJobProgressState
import org.dataland.datalandbackend.openApiClient.model.ExportLatestRequestData
import org.dataland.datalandbackend.openApiClient.model.ExportRequestData
import org.junit.jupiter.api.assertDoesNotThrow
import java.io.File
import java.util.UUID
import java.util.concurrent.TimeUnit

abstract class BaseExportTestSetup<T> {
    protected val apiAccessor = ApiAccessor()

    // Common properties that all export tests need
    protected lateinit var companyWithNullFieldId: String
    protected lateinit var companyWithNullFieldLei: String
    protected lateinit var companyWithNonNullFieldId: String
    protected lateinit var companyWithNonNullFieldLei: String
    protected val reportingPeriod = "2024"

    // Abstract methods that subclasses must implement
    protected abstract fun getTestDataWithNullField(): T

    protected abstract fun getTestDataWithNonNullField(): T

    protected abstract fun getFullTestData(): T

    protected abstract fun getNullFieldName(): String

    protected abstract fun retrieveData(
        companyId: String,
        reportingPeriod: String,
    ): Any

    protected abstract fun uploadData(
        companyId: String,
        data: T,
        reportingPeriod: String,
    )

    protected abstract fun getExportJobPostingFunction(): (ExportRequestData, Boolean?, Boolean?) -> ExportJobInfo

    protected abstract fun getExportLatestJobPostingFunction(): (ExportLatestRequestData, Boolean?, Boolean?) -> ExportJobInfo

    protected fun exportDataAsCsv(
        companyIds: List<String>,
        reportingPeriods: List<String>,
        keepValueFieldsOnly: Boolean = true,
        includeAliases: Boolean = false,
    ): File =
        exportData(
            ExportRequestData(
                reportingPeriods = reportingPeriods,
                companyIds = companyIds,
                fileFormat = ExportFileType.CSV,
            ),
            keepValueFieldsOnly,
            includeAliases,
        )

    protected fun exportDataAsExcel(
        companyIds: List<String>,
        reportingPeriods: List<String>,
        keepValueFieldsOnly: Boolean = true,
        includeAliases: Boolean = false,
    ): File =
        exportData(
            ExportRequestData(
                reportingPeriods = reportingPeriods,
                companyIds = companyIds,
                fileFormat = ExportFileType.EXCEL,
            ),
            keepValueFieldsOnly = keepValueFieldsOnly,
            includeAliases = includeAliases,
        )

    protected fun getExportedData(exportJobInfo: ExportJobInfo): File {
        val exportJobId = exportJobInfo.id.toString()
        Awaitility.await().atMost(10000, TimeUnit.MILLISECONDS).pollDelay(500, TimeUnit.MILLISECONDS).until {
            apiAccessor.exportControllerApi.getExportJobState(exportJobId) == ExportJobProgressState.Success
        }
        return apiAccessor.exportControllerApi.exportCompanyAssociatedDataById(exportJobId)
    }

    protected fun exportData(
        exportRequestData: ExportRequestData,
        keepValueFieldsOnly: Boolean = true,
        includeAliases: Boolean = false,
    ): File = getExportedData(getExportJobPostingFunction()(exportRequestData, keepValueFieldsOnly, includeAliases))

    protected fun exportLatestData(
        exportLatestRequestData: ExportLatestRequestData,
        keepValueFieldsOnly: Boolean = true,
        includeAliases: Boolean = false,
    ): File = getExportedData(getExportLatestJobPostingFunction()(exportLatestRequestData, keepValueFieldsOnly, includeAliases))

    /**
     * Uploads a company with a randomly generated Legal Entity Identifier (LEI) and associates it with the
     * API accessor.
     *
     * @return A pair where the first element is the ID of the uploaded company and the second element is the generated LEI.
     */
    protected fun uploadCompanyWithRandomLei(): Pair<String, String> {
        val lei = UUID.randomUUID().toString()
        return Pair(apiAccessor.uploadOneCompanyWithIdentifiers(lei = lei)!!.actualStoredCompany.companyId, lei)
    }

    /**
     * Sets up test companies and their associated data for use in export tests.
     */
    protected fun setupCompaniesAndData() {
        // Create two test companies with randomly generated LEIs
        val companyWithNullFieldIdAndLei = uploadCompanyWithRandomLei()
        val companyWithNonNullFieldIdAndLei = uploadCompanyWithRandomLei()

        companyWithNullFieldId = companyWithNullFieldIdAndLei.first
        companyWithNullFieldLei = companyWithNullFieldIdAndLei.second
        companyWithNonNullFieldId = companyWithNonNullFieldIdAndLei.first
        companyWithNonNullFieldLei = companyWithNonNullFieldIdAndLei.second

        // Upload test data with null field for first company
        uploadData(
            companyId = companyWithNullFieldId,
            data = getTestDataWithNullField(),
            reportingPeriod = reportingPeriod,
        )

        // Upload test data with non-null field for second company
        uploadData(
            companyId = companyWithNonNullFieldId,
            data = getTestDataWithNonNullField(),
            reportingPeriod = reportingPeriod,
        )

        // Upload outdated data (with null field) for second company
        uploadData(
            companyId = companyWithNonNullFieldId,
            data = getTestDataWithNonNullField(),
            reportingPeriod = (reportingPeriod.toInt() - 1).toString(),
        )

        waitForDataAvailability()
    }

    /**
     * Waits for the data associated with specific test companies to become available.
     *
     * This method uses polling to repeatedly check the availability of data for two test companies
     * identified by their respective IDs (`companyWithNullFieldId` and `companyWithNonNullFieldId`).
     * It ensures that data retrieval for both companies does not throw any exceptions.
     *
     * The method will attempt to verify data availability within a maximum time limit of 10 seconds.
     * Each polling attempt is performed at intervals of 500 milliseconds until the conditions are met.
     *
     * The method is intended to be used as part of the data setup or verification process in export tests,
     * ensuring that the required data is accessible before proceeding with further test operations.
     */
    protected fun waitForDataAvailability() {
        Awaitility
            .await()
            .atMost(10, TimeUnit.SECONDS)
            .pollInterval(500, TimeUnit.MILLISECONDS)
            .untilAsserted {
                assertDoesNotThrow {
                    retrieveData(companyWithNullFieldId, reportingPeriod)
                    retrieveData(companyWithNonNullFieldId, reportingPeriod)
                    retrieveData(companyWithNonNullFieldId, (reportingPeriod.toInt() - 1).toString())
                }
            }
    }

    // Common test implementations that can be called from subclasses
}
