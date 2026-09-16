package org.dataland.datalandbackend.services

import com.fasterxml.jackson.dataformat.csv.CsvSchema
import org.dataland.datalandbackend.entities.BasicCompanyInformation
import org.dataland.datalandbackend.model.DataDimensionQuery
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.model.enums.export.ExportJobProgressState
import org.dataland.datalandbackend.model.export.ExportAvailability
import org.dataland.datalandbackend.model.export.ExportJob
import org.dataland.datalandbackend.model.export.ExportOptions
import org.dataland.datalandbackend.model.export.SingleCompanyExportData
import org.dataland.datalandbackend.services.datapoints.DatasetAssembler
import org.dataland.datalandbackendutils.model.BasicDataDimensions
import org.dataland.datalandbackendutils.model.BasicDatasetDimensions
import org.dataland.datalandbackendutils.model.ListDataDimensions
import org.dataland.datalandbackendutils.utils.JsonUtils.defaultObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.core.io.InputStreamResource
import org.springframework.scheduling.annotation.Async
import java.io.OutputStream
import kotlin.collections.associate

/**
 * Base class for export service used for managing the logic behind the dataset export controller
 */
open class DataExportService<T>(
    private val datasetAssembler: DatasetAssembler,
    private val specificationService: SpecificationService,
    private val companyQueryManager: CompanyQueryManager,
    private val datasetStorageService: DatasetStorageService,
    private val nonSourceabilityInformationManager: NonSourceabilityInformationManager,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    private val exportStreamBuilder = ExportStreamBuilder(datasetAssembler, specificationService)

    /**
     * Create a ByteStream to be used for export from a list of SingleCompanyExportData.
     *
     * Note that swagger only supports InputStreamResources and not OutputStreams
     */
    internal fun <T> buildStreamFromPortfolioExportData(
        portfolioData: Collection<SingleCompanyExportData<T>>,
        exportOptions: ExportOptions,
    ): InputStreamResource = exportStreamBuilder.buildStreamFromPortfolioExportData(portfolioData, exportOptions)

    /**
     * Transform the data to an Excel file with human-readable headers. See [ExportStreamBuilder.transformDataToExcelWithReadableHeaders].
     */
    fun transformDataToExcelWithReadableHeaders(
        csvDataWithReadableHeaders: List<Map<String, String?>>,
        csvSchema: CsvSchema,
        outputStream: OutputStream,
        shortHeaderNamesAndColumns: Boolean = false,
    ) = exportStreamBuilder.transformDataToExcelWithReadableHeaders(
        csvDataWithReadableHeaders,
        csvSchema,
        outputStream,
        shortHeaderNamesAndColumns,
    )

    /**
     * Create a ByteStream to be used for export from a list of SingleCompanyExportData.
     *
     * Note that swagger only supports InputStreamResources and not OutputStreams
     *
     * @param dataDimensionsWithDataStrings the plain data to be exported
     * @param nonSourceableGapDimensions data dimensions for which no dataset was found but which are confirmed
     *   non-sourceable and should therefore still produce a synthetic row in the export
     * @param newExportJob export job in which the stream will be stored
     * @param clazz the class type of the data to be exported
     * @param exportOptions the export options specifying the export format
     */
    private fun buildStream(
        dataDimensionsWithDataStrings: Map<BasicDatasetDimensions, String>,
        nonSourceableGapDimensions: Set<BasicDataDimensions>,
        newExportJob: ExportJob,
        clazz: Class<out T>,
        exportOptions: ExportOptions,
    ) {
        val portfolioData = buildCompanyExportData(dataDimensionsWithDataStrings, nonSourceableGapDimensions, clazz)

        newExportJob.fileToExport = buildStreamFromPortfolioExportData(portfolioData, exportOptions)
        newExportJob.progressState = ExportJobProgressState.Success
    }

    /**
     * Runs [block] and, if it throws, marks [newExportJob] as failed instead of letting the exception propagate.
     *
     * [startExportJob] and [startLatestExportJob] run on an `@Async` thread, so an uncaught exception would only be
     * logged by Spring's default `AsyncUncaughtExceptionHandler` and never reach the caller - the export job would
     * otherwise be left stuck in [ExportJobProgressState.Pending] forever from the user's perspective.
     *
     * @param newExportJob the export job to mark as failed if [block] throws
     * @param block the export job logic to run, including any data retrieval that may fail
     */
    private fun runExportJob(
        newExportJob: ExportJob,
        block: () -> Unit,
    ) {
        @Suppress("TooGenericExceptionCaught")
        try {
            block()
        } catch (exception: Exception) {
            logger.error("Export job with id ${newExportJob.id} failed.", exception)
            newExportJob.progressState = ExportJobProgressState.Failure
        }
    }

    /**
     * Create a ByteStream to be used for export from a list of SingleCompanyExportData.
     *
     * Note that swagger only supports InputStreamResources and not OutputStreams
     *
     * @param listDataDimensions the passed list of SingleCompanyExportData to be exported
     * @param newExportJob export job in which the stream will be stored
     * @param clazz the class type of the data to be exported
     * @param exportOptions the export options specifying the export format
     */
    @Async
    open fun startExportJob(
        listDataDimensions: ListDataDimensions,
        newExportJob: ExportJob,
        clazz: Class<out T>,
        exportOptions: ExportOptions,
    ) = runExportJob(newExportJob) {
        val correlationId = newExportJob.id.toString()
        val requestedDimensions = buildRequestedDimensions(listDataDimensions)
        val dataDimensionsWithDataStrings = datasetStorageService.getDatasetData(requestedDimensions, correlationId)

        val missingDimensions =
            (requestedDimensions - dataDimensionsWithDataStrings.keys)
                .map { it.toBasicDataDimensions() }
                .toSet()
        val nonSourceableGapDimensions = resolveNonSourceableGaps(missingDimensions, exportOptions.dataType)

        buildStream(dataDimensionsWithDataStrings, nonSourceableGapDimensions, newExportJob, clazz, exportOptions)
    }

    /**
     * Create a ByteStream of the latest available data per company to be used for export from a list of SingleCompanyExportData.
     *
     * For companies without a latest dataset, any currently-active non-sourceability entry for the requested framework
     * (across any reporting period) is used to produce a synthetic non-sourceable row instead of silently omitting
     * the company from the export.
     *
     * @param companyIds the companies for which the latest data is to be exported
     * @param newExportJob correlationId for unique identification
     * @param clazz the class type of the data to be exported
     * @param exportOptions the export options specifying the export format
     */
    @Async
    open fun startLatestExportJob(
        companyIds: Collection<String>,
        newExportJob: ExportJob,
        clazz: Class<out T>,
        exportOptions: ExportOptions,
    ) = runExportJob(newExportJob) {
        val correlationId = newExportJob.id.toString()
        val dataDimensionsWithDataStrings = getLatestPlainData(companyIds, exportOptions.dataType.toString(), correlationId)

        val missingCompanyIds = companyIds.toSet() - dataDimensionsWithDataStrings.keys.map { it.companyId }.toSet()
        val nonSourceableGapDimensions =
            if (missingCompanyIds.isEmpty()) {
                emptySet()
            } else {
                selectLatestNonSourceableDimensionPerCompany(
                    nonSourceabilityInformationManager.searchActiveNonSourceableDimensions(
                        DataDimensionQuery(
                            companyIds = missingCompanyIds.toList(),
                            dataTypes = listOf(exportOptions.dataType.toString()),
                        ),
                    ),
                )
            }

        buildStream(dataDimensionsWithDataStrings, nonSourceableGapDimensions, newExportJob, clazz, exportOptions)
    }

    /**
     * Reduces a set of non-sourceable data dimensions to at most one entry per company - the one with the
     * lexicographically latest reporting period - mirroring the "latest" semantics used for datasets.
     * This ensures the "latest" export never produces more than one synthetic
     * non-sourceable row per company, even if multiple reporting periods are currently marked non-sourceable.
     *
     * @param nonSourceableDimensions the non-sourceable data dimensions to reduce
     * @return at most one data dimension per company, namely the one with the latest reporting period
     */
    private fun selectLatestNonSourceableDimensionPerCompany(nonSourceableDimensions: Set<BasicDataDimensions>): Set<BasicDataDimensions> =
        nonSourceableDimensions
            .groupBy { it.companyId }
            .mapNotNull { (_, dimensionsForCompany) -> dimensionsForCompany.maxByOrNull { it.reportingPeriod } }
            .toSet()

    private fun buildRequestedDimensions(listDataDimensions: ListDataDimensions): Set<BasicDatasetDimensions> =
        listDataDimensions.companyIds
            .flatMap { companyId ->
                listDataDimensions.reportingPeriods.flatMap { reportingPeriod ->
                    listDataDimensions.dataTypes.map { dataType ->
                        BasicDatasetDimensions(companyId, dataType, reportingPeriod)
                    }
                }
            }.toSet()

    /**
     * For the given set of data dimensions that have no dataset available, determines which of them are
     * currently confirmed as non-sourceable via a single bulk lookup.
     *
     * @param missingDimensions the data dimensions for which no dataset was found
     * @param dataType the framework/data type being exported
     * @return the subset of [missingDimensions] that are currently confirmed as non-sourceable
     */
    private fun resolveNonSourceableGaps(
        missingDimensions: Set<BasicDataDimensions>,
        dataType: DataType,
    ): Set<BasicDataDimensions> {
        if (missingDimensions.isEmpty()) return emptySet()

        val query =
            DataDimensionQuery(
                companyIds = missingDimensions.map { it.companyId }.distinct(),
                dataTypes = listOf(dataType.toString()),
                reportingPeriods = missingDimensions.map { it.reportingPeriod }.distinct(),
            )

        return nonSourceabilityInformationManager
            .searchActiveNonSourceableDimensions(query)
            .filter { it in missingDimensions }
            .toSet()
    }

    private fun getLatestPlainData(
        companyIds: Collection<String>,
        framework: String,
        correlationId: String,
    ) = datasetStorageService
        .getLatestAvailableData(
            companyIds,
            framework,
            correlationId,
        ).associate { it.dimensions to it.data }

    private fun buildCompanyExportData(
        dataDimensionsWithDataStrings: Map<BasicDatasetDimensions, String>,
        nonSourceableGapDimensions: Set<BasicDataDimensions>,
        clazz: Class<out T>,
    ): List<SingleCompanyExportData<T>> {
        val allCompanyIds =
            (
                dataDimensionsWithDataStrings.keys.map { it.companyId } +
                    nonSourceableGapDimensions.map { it.companyId }
            ).distinct()
        val basicCompanyInformation = companyQueryManager.getBasicCompanyInformationByIds(allCompanyIds)

        val availableRows = buildRowsForAvailableData(dataDimensionsWithDataStrings, clazz, basicCompanyInformation)
        val nonSourceableRows = buildRowsForNonSourceableGaps(nonSourceableGapDimensions, basicCompanyInformation)

        return (availableRows + nonSourceableRows).sortedBy { it.companyName }
    }

    /**
     * Builds one export row per data dimension for which an actual dataset was found.
     */
    private fun buildRowsForAvailableData(
        dataDimensionsWithDataStrings: Map<BasicDatasetDimensions, String>,
        clazz: Class<out T>,
        basicCompanyInformation: Map<String, BasicCompanyInformation?>,
    ): List<SingleCompanyExportData<T>> =
        dataDimensionsWithDataStrings.map {
            SingleCompanyExportData(
                companyName = basicCompanyInformation[it.key.companyId]?.companyName ?: "",
                companyLei = basicCompanyInformation[it.key.companyId]?.lei ?: "",
                reportingPeriod = it.key.reportingPeriod,
                availability = ExportAvailability.AVAILABLE,
                data = defaultObjectMapper.readValue(it.value, clazz),
            )
        }

    /**
     * Builds one synthetic export row per data dimension that has no dataset but is confirmed non-sourceable.
     * The framework-specific data is intentionally left absent (null).
     */
    private fun buildRowsForNonSourceableGaps(
        nonSourceableGapDimensions: Set<BasicDataDimensions>,
        basicCompanyInformation: Map<String, BasicCompanyInformation?>,
    ): List<SingleCompanyExportData<T>> =
        nonSourceableGapDimensions.map {
            SingleCompanyExportData(
                companyName = basicCompanyInformation[it.companyId]?.companyName ?: "",
                companyLei = basicCompanyInformation[it.companyId]?.lei ?: "",
                reportingPeriod = it.reportingPeriod,
                availability = ExportAvailability.NON_SOURCEABLE,
                data = null,
            )
        }
}
