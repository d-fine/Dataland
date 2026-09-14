package org.dataland.datalandbackend.services

import com.fasterxml.jackson.dataformat.csv.CsvSchema
import org.dataland.datalandbackend.model.enums.export.ExportJobProgressState
import org.dataland.datalandbackend.model.export.ExportJob
import org.dataland.datalandbackend.model.export.ExportOptions
import org.dataland.datalandbackend.model.export.SingleCompanyExportData
import org.dataland.datalandbackend.services.datapoints.DatasetAssembler
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
     * @param newExportJob export job in which the stream will be stored
     * @param clazz the class type of the data to be exported
     * @param exportOptions the export options specifying the export format
     */
    private fun buildStream(
        dataDimensionsWithDataStrings: Map<BasicDatasetDimensions, String>,
        newExportJob: ExportJob,
        clazz: Class<out T>,
        exportOptions: ExportOptions,
    ) {
        val portfolioData = buildCompanyExportData(dataDimensionsWithDataStrings, clazz)

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
        buildStream(
            getPlainData(listDataDimensions, newExportJob.id.toString()),
            newExportJob,
            clazz,
            exportOptions,
        )
    }

    /**
     * Create a ByteStream of the latest available data per company to be used for export from a list of SingleCompanyExportData.
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
        buildStream(
            getLatestPlainData(companyIds, exportOptions.dataType.toString(), newExportJob.id.toString()),
            newExportJob,
            clazz,
            exportOptions,
        )
    }

    private fun getPlainData(
        listDataDimensions: ListDataDimensions,
        correlationId: String,
    ) = datasetStorageService.getDatasetData(
        listDataDimensions.companyIds
            .flatMap { companyId ->
                listDataDimensions.reportingPeriods.flatMap { reportingPeriod ->
                    listDataDimensions.dataTypes.map { dataType ->
                        BasicDatasetDimensions(companyId, dataType, reportingPeriod)
                    }
                }
            }.toSet(),
        correlationId,
    )

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
        clazz: Class<out T>,
    ): List<SingleCompanyExportData<T>> {
        val basicCompanyInformation =
            companyQueryManager.getBasicCompanyInformationByIds(
                dataDimensionsWithDataStrings.map { it.key.companyId },
            )

        return dataDimensionsWithDataStrings
            .asSequence()
            .map {
                SingleCompanyExportData(
                    companyName = basicCompanyInformation[it.key.companyId]?.companyName ?: "",
                    companyLei = basicCompanyInformation[it.key.companyId]?.lei ?: "",
                    reportingPeriod = it.key.reportingPeriod,
                    data = defaultObjectMapper.readValue(it.value, clazz),
                )
            }.sortedBy { it.companyName }
            .toList()
    }
}
