package org.dataland.datalandbackend.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.api.DataApi
import org.dataland.datalandbackend.entities.DataMetaInformationEntity
import org.dataland.datalandbackend.exceptions.DownloadDataNotFoundApiException
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.model.StorableDataset
import org.dataland.datalandbackend.model.companies.CompanyAssociatedData
import org.dataland.datalandbackend.model.export.SingleCompanyExportData
import org.dataland.datalandbackend.model.metainformation.DataAndMetaInformation
import org.dataland.datalandbackend.model.metainformation.DataMetaInformation
import org.dataland.datalandbackend.repositories.utils.DataMetaInformationSearchFilter
import org.dataland.datalandbackend.services.CompanyQueryManager
import org.dataland.datalandbackend.services.DataExportService
import org.dataland.datalandbackend.services.DataMetaInformationManager
import org.dataland.datalandbackend.services.DatasetStorageService
import org.dataland.datalandbackend.services.LogMessageBuilder
import org.dataland.datalandbackend.utils.IdUtils
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandbackendutils.model.BasicDataDimensions
import org.dataland.datalandbackendutils.model.ExportFileType
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.slf4j.LoggerFactory
import org.springframework.core.io.InputStreamResource
import org.springframework.http.ContentDisposition
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import java.time.Instant
import java.time.LocalDateTime

/**
 * Abstract implementation of the controller for data exchange of an abstract type T
 * @param datasetStorageService service to handle data storage
 * @param dataMetaInformationManager service for retrieving data meta-information
 * @param dataExportService service for handling data export to CSV and JSON
 * @param objectMapper the mapper to transform strings into classes and vice versa
 * @param companyQueryManager service to retrieve company information
 */
open class DataController<T>(
    private val datasetStorageService: DatasetStorageService,
    private val dataMetaInformationManager: DataMetaInformationManager,
    private val dataExportService: DataExportService,
    private val objectMapper: ObjectMapper,
    private val companyQueryManager: CompanyQueryManager,
    private val clazz: Class<T>,
) : DataApi<T> {
    private val dataType = DataType.of(clazz)
    private val logger = LoggerFactory.getLogger(javaClass)
    private val logMessageBuilder = LogMessageBuilder()

    override fun postCompanyAssociatedData(
        companyAssociatedData: CompanyAssociatedData<T>,
        bypassQa: Boolean,
    ): ResponseEntity<DataMetaInformation> {
        val companyId = companyAssociatedData.companyId
        val reportingPeriod = companyAssociatedData.reportingPeriod
        val userId = DatalandAuthentication.fromContext().userId
        logger.info(logMessageBuilder.postCompanyAssociatedDataMessage(userId, dataType, companyId, reportingPeriod))

        val uploadTime = Instant.now().toEpochMilli()
        val datasetToStore = buildStorableDataset(companyAssociatedData, userId, uploadTime)
        val correlationId = IdUtils.generateCorrelationId(companyId = companyAssociatedData.companyId, dataId = null)
        val dataIdOfPostedData = datasetStorageService.storeDataset(datasetToStore, bypassQa, correlationId)

        logger.info(logMessageBuilder.postCompanyAssociatedDataSuccessMessage(companyId, correlationId))

        return ResponseEntity.ok(
            DataMetaInformation(
                dataId = dataIdOfPostedData, companyId = companyId, dataType = dataType,
                uploaderUserId = userId, uploadTime = uploadTime, reportingPeriod = reportingPeriod,
                currentlyActive = false, qaStatus = QaStatus.Pending,
            ),
        )
    }

    private fun retrieveDataset(dataId: String): CompanyAssociatedData<T> {
        val metaInfo = dataMetaInformationManager.getDataMetaInformationByDataId(dataId)
        this.verifyAccess(metaInfo)
        val companyId = metaInfo.company.companyId
        val correlationId = IdUtils.generateCorrelationId(companyId = companyId, dataId = dataId)
        logger.info(logMessageBuilder.getCompanyAssociatedDataMessage(dataId, companyId))

        val companyAssociatedData =
            this.buildCompanyAssociatedData(dataId, companyId, metaInfo.reportingPeriod, correlationId)
        logger.info(logMessageBuilder.getCompanyAssociatedDataSuccessMessage(dataId, companyId, correlationId))
        return companyAssociatedData
    }

    override fun getCompanyAssociatedData(dataId: String): ResponseEntity<CompanyAssociatedData<T>> =
        ResponseEntity
            .ok(retrieveDataset(dataId))

    override fun getCompanyAssociatedDataByDimensions(
        reportingPeriod: String,
        companyId: String,
    ): ResponseEntity<CompanyAssociatedData<T>> {
        val dataDimensions =
            BasicDataDimensions(
                companyId = companyId,
                dataType = dataType.toString(),
                reportingPeriod = reportingPeriod,
            )
        val correlationId = IdUtils.generateCorrelationId(dataDimensions)

        val frameworkData =
            this.getFrameworkDataStrings(
                setOf(Pair(companyId, reportingPeriod)),
                dataType.toString(),
                correlationId,
            )
        if (frameworkData.isEmpty()) {
            throw ResourceNotFoundApiException(
                summary = logMessageBuilder.dynamicDatasetNotFoundSummary,
                message = logMessageBuilder.getDynamicDatasetNotFoundMessage(dataDimensions),
            )
        }

        return ResponseEntity.ok(
            CompanyAssociatedData(
                companyId = companyId,
                reportingPeriod = reportingPeriod,
                data = objectMapper.readValue(frameworkData.values.first(), clazz),
            ),
        )
    }

    private fun getData(
        dataId: String,
        correlationId: String,
    ): T {
        val dataAsString = datasetStorageService.getDatasetData(dataId, dataType.toString(), correlationId)
        return objectMapper.readValue(dataAsString, clazz)
    }

    override fun getFrameworkDatasetsForCompany(
        companyId: String,
        showOnlyActive: Boolean,
        reportingPeriod: String?,
    ): ResponseEntity<List<DataAndMetaInformation<T>>> {
        val reportingPeriodInLog = reportingPeriod ?: "all reporting periods"
        logger.info(logMessageBuilder.getFrameworkDatasetsForCompanyMessage(dataType, companyId, reportingPeriodInLog))
        val allRelevantData =
            datasetStorageService.getAllDatasetsAndMetaInformation(
                DataMetaInformationSearchFilter(
                    companyId = companyId,
                    dataType = dataType,
                    onlyActive = showOnlyActive,
                    reportingPeriod = reportingPeriod,
                ),
                correlationId = IdUtils.generateCorrelationId(companyId = companyId, dataId = null),
            )
        return ResponseEntity.ok(
            allRelevantData.map {
                DataAndMetaInformation(
                    it.metaInfo,
                    objectMapper.readValue(it.data, clazz),
                )
            },
        )
    }

    override fun exportCompanyAssociatedDataByDimensions(
        reportingPeriods: List<String>,
        companyIds: List<String>,
        exportFileType: ExportFileType,
        keepValueFieldsOnly: Boolean,
    ): ResponseEntity<InputStreamResource> {
        if (companyQueryManager.validateCompanyIdentifiers(companyIds).all {
                it.companyInformation == null
            }
        ) {
            throw ResourceNotFoundApiException(
                summary = "CompanyIds $companyIds not found.",
                message = "All provided companyIds are invalid. Please provide at least one valid companyId.",
            )
        }

        val companyIdAndReportingPeriodPairs = mutableSetOf<Pair<String, String>>()
        companyIds.forEach { companyId ->
            reportingPeriods.forEach { reportingPeriod ->
                companyIdAndReportingPeriodPairs.add(Pair(companyId, reportingPeriod))
            }
        }

        val correlationId = IdUtils.generateUUID()
        logger.info("Received a request to export portfolio data. Correlation ID: $correlationId")

        val companyAssociatedDataForExport =
            try {
                dataExportService.buildStreamFromPortfolioExportData(
                    this.buildCompanyExportData(
                        companyIdAndReportingPeriodPairs,
                        dataType.toString(), correlationId,
                    ),
                    exportFileType,
                    dataType,
                    keepValueFieldsOnly,
                )
            } catch (_: DownloadDataNotFoundApiException) {
                return ResponseEntity.noContent().build()
            }
        logger.info("Creation of ${exportFileType.name} for export successful. Correlation ID: $correlationId")

        return ResponseEntity
            .ok()
            .headers(buildHttpHeadersForExport(exportFileType))
            .body(companyAssociatedDataForExport)
    }

    private fun buildHttpHeadersForExport(exportFileType: ExportFileType): HttpHeaders {
        val headers = HttpHeaders()
        val timestamp = LocalDateTime.now().toString()
        headers.contentType = exportFileType.mediaType
        headers.contentDisposition =
            ContentDisposition
                .attachment()
                .filename("data-export-$timestamp.${exportFileType.fileExtension}")
                .build()
        return headers
    }

    private fun buildStorableDataset(
        companyAssociatedData: CompanyAssociatedData<T>,
        userId: String,
        uploadTime: Long,
    ): StorableDataset =
        StorableDataset(
            companyId = companyAssociatedData.companyId,
            dataType = dataType,
            uploaderUserId = userId,
            uploadTime = uploadTime,
            reportingPeriod = companyAssociatedData.reportingPeriod,
            data = objectMapper.writeValueAsString(companyAssociatedData.data),
        )

    private fun buildCompanyAssociatedData(
        dataId: String,
        companyId: String,
        reportingPeriod: String,
        correlationId: String,
    ): CompanyAssociatedData<T> {
        logger.info(logMessageBuilder.getCompanyAssociatedDataMessage(dataId, companyId))
        return CompanyAssociatedData(
            companyId = companyId,
            reportingPeriod = reportingPeriod,
            data = getData(dataId, correlationId),
        )
    }

    /**
     * Retrieve the active data set as a string for a given framework and each combination of company ID and reporting period.
     */
    private fun getFrameworkDataStrings(
        companyAndReportingPeriodPairs: Set<Pair<String, String>>,
        framework: String,
        correlationId: String,
    ): Map<BasicDataDimensions, String> =
        datasetStorageService.getDatasetData(
            companyAndReportingPeriodPairs.mapTo(mutableSetOf()) {
                BasicDataDimensions(
                    it.first,
                    framework,
                    it.second,
                )
            },
            correlationId,
        )

    private fun buildCompanyExportData(
        companyAndReportingPeriodPairs: Set<Pair<String, String>>,
        framework: String,
        correlationId: String,
    ): List<SingleCompanyExportData<T>> {
        val dataDimensionsWithDataStrings =
            getFrameworkDataStrings(companyAndReportingPeriodPairs, framework, correlationId)

        val basicCompanyInformation =
            companyQueryManager.getBasicCompanyInformationByIds(
                dataDimensionsWithDataStrings.map { it.key.companyId },
            )

        return dataDimensionsWithDataStrings.map {
            SingleCompanyExportData(
                companyName = basicCompanyInformation[it.key.companyId]?.companyName ?: "",
                companyLei = basicCompanyInformation[it.key.companyId]?.lei ?: "",
                reportingPeriod = it.key.reportingPeriod,
                data = objectMapper.readValue(it.value, clazz),
            )
        }
    }

    @Throws(AccessDeniedException::class)
    private fun verifyAccess(metaInfo: DataMetaInformationEntity) {
        if (!metaInfo.isDatasetViewableByUser(DatalandAuthentication.fromContextOrNull())) {
            throw AccessDeniedException(logMessageBuilder.generateAccessDeniedExceptionMessage(metaInfo.qaStatus))
        }
    }
}
