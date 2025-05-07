package org.dataland.datalandbackend.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.api.DataApi
import org.dataland.datalandbackend.entities.DataMetaInformationEntity
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.model.StorableDataset
import org.dataland.datalandbackend.model.companies.CompanyAssociatedData
import org.dataland.datalandbackend.model.metainformation.DataAndMetaInformation
import org.dataland.datalandbackend.model.metainformation.DataMetaInformation
import org.dataland.datalandbackend.repositories.utils.DataMetaInformationSearchFilter
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

/**
 * Abstract implementation of the controller for data exchange of an abstract type T
 * @param datasetStorageService service to handle data storage
 * @param dataMetaInformationManager service for handling data meta information
 * @param objectMapper the mapper to transform strings into classes and vice versa
 */

open class DataController<T>(
    private val datasetStorageService: DatasetStorageService,
    private val dataMetaInformationManager: DataMetaInformationManager,
    private val dataExportService: DataExportService,
    private val objectMapper: ObjectMapper,
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

        val companyAssociatedDataList =
            this.buildCompanyAssociatedData(
                setOf(Pair(companyId, reportingPeriod)),
                dataType.toString(),
                correlationId,
            )
        if (companyAssociatedDataList.isEmpty()) {
            throw ResourceNotFoundApiException(
                summary = logMessageBuilder.dynamicDatasetNotFoundSummary,
                message = logMessageBuilder.getDynamicDatasetNotFoundMessage(dataDimensions),
            )
        }
        return ResponseEntity.ok(companyAssociatedDataList.first())
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
        includeMetaData: Boolean,
    ): ResponseEntity<InputStreamResource> {
        val companyIdAndReportingPeriodPairs = mutableSetOf<Pair<String, String>>()
        companyIds.forEach { companyId ->
            reportingPeriods.forEach { reportingPeriod ->
                companyIdAndReportingPeriodPairs.add(Pair(companyId, reportingPeriod))
            }
        }

        val correlationId = IdUtils.generateUUID()
        logger.info("Received a request to export portfolio data. Correlation ID: $correlationId")

        val companyAssociatedDataForExport =
            dataExportService.buildStreamFromCompanyAssociatedData(
                this.buildCompanyAssociatedData(
                    companyIdAndReportingPeriodPairs,
                    dataType.toString(), correlationId,
                ),
                exportFileType,
                includeMetaData,
            )

        logger.info("Creation of ${exportFileType.name} for export successful. Correlation ID: $correlationId")

        return ResponseEntity
            .ok()
            .headers(buildHttpHeadersForExport("portfolio", exportFileType))
            .body(companyAssociatedDataForExport)
    }

    private fun buildHttpHeadersForExport(
        dataId: String,
        exportFileType: ExportFileType,
    ): HttpHeaders {
        val headers = HttpHeaders()
        headers.contentType = exportFileType.mediaType
        headers.contentDisposition =
            ContentDisposition
                .attachment()
                .filename("$dataId.${exportFileType.fileExtension}")
                .build()
        return headers
    }

    private fun buildHttpHeadersForExport(
        dataDimensions: BasicDataDimensions,
        exportFileType: ExportFileType,
    ): HttpHeaders {
        val headers = HttpHeaders()
        headers.contentType = exportFileType.mediaType
        headers.contentDisposition =
            ContentDisposition
                .attachment()
                .filename(
                    "${dataDimensions.reportingPeriod}-${dataDimensions.dataType}-${dataDimensions.companyId}" +
                        ".${exportFileType.fileExtension}",
                ).build()
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

    private fun buildCompanyAssociatedData(
        companyAndReportingPeriodPairs: Set<Pair<String, String>>,
        framework: String,
        correlationId: String,
    ): List<CompanyAssociatedData<T>> {
        val dataDimensionsWithDataStrings =
            datasetStorageService.getDatasetData(
                companyAndReportingPeriodPairs.mapTo(mutableSetOf()) { BasicDataDimensions(it.first, framework, it.second) },
                correlationId,
            )

        return dataDimensionsWithDataStrings.map {
            CompanyAssociatedData(
                companyId = it.key.companyId,
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
