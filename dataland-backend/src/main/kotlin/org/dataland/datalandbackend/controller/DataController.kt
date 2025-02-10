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
        val dataAsString =
            datasetStorageService.getDatasetData(dataDimensions, correlationId)
                ?: throw ResourceNotFoundApiException(
                    summary = logMessageBuilder.dynamicDatasetNotFoundSummary,
                    message = logMessageBuilder.getDynamicDatasetNotFoundMessage(dataDimensions),
                )

        val result =
            CompanyAssociatedData(
                companyId = companyId,
                reportingPeriod = reportingPeriod,
                data = objectMapper.readValue(dataAsString, clazz),
            )

        return ResponseEntity.ok(result)
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
        val metaInfos =
            dataMetaInformationManager.searchDataMetaInfo(
                DataMetaInformationSearchFilter(
                    companyId = companyId,
                    dataType = dataType,
                    onlyActive = showOnlyActive,
                    reportingPeriod = reportingPeriod,
                ),
            )
        val authentication = DatalandAuthentication.fromContextOrNull()
        val listOfFrameworkDataAndMetaInfo = mutableListOf<DataAndMetaInformation<T>>()
        metaInfos.filter { it.isDatasetViewableByUser(authentication) }.forEach {
            val correlationId = IdUtils.generateCorrelationId(companyId = companyId, dataId = null)
            listOfFrameworkDataAndMetaInfo.add(
                DataAndMetaInformation(
                    it.toApiModel(), getData(it.dataId, correlationId),
                ),
            )
        }
        return ResponseEntity.ok(listOfFrameworkDataAndMetaInfo)
    }

    override fun exportCompanyAssociatedDataToJson(dataId: String): ResponseEntity<InputStreamResource> {
        val metaInfo = dataMetaInformationManager.getDataMetaInformationByDataId(dataId)
        this.verifyAccess(metaInfo)
        val companyId = metaInfo.company.companyId
        val correlationId = IdUtils.generateCorrelationId(companyId = companyId, dataId = dataId)
        val companyAssociatedData =
            this.buildCompanyAssociatedData(dataId, companyId, metaInfo.reportingPeriod, correlationId)

        logger.info(logMessageBuilder.getCompanyAssociatedDataSuccessMessage(dataId, companyId, correlationId))

        val companyAssociatedDataJson =
            dataExportService.buildJsonStreamFromCompanyAssociatedData(companyAssociatedData)

        logger.info("Creation of JSON for export successful.")

        return ResponseEntity
            .ok()
            .headers(buildHttpHeadersForExport(dataId, ExportFileType.JSON))
            .body(companyAssociatedDataJson)
    }

    override fun exportCompanyAssociatedDataToCsv(dataId: String): ResponseEntity<InputStreamResource> {
        val metaInfo = dataMetaInformationManager.getDataMetaInformationByDataId(dataId)
        this.verifyAccess(metaInfo)
        val companyId = metaInfo.company.companyId
        val correlationId = IdUtils.generateCorrelationId(companyId = companyId, dataId = dataId)
        val companyAssociatedData =
            this.buildCompanyAssociatedData(dataId, companyId, metaInfo.reportingPeriod, correlationId)

        logger.info(logMessageBuilder.getCompanyAssociatedDataSuccessMessage(dataId, companyId, correlationId))

        val companyAssociatedDataCsv = dataExportService.buildCsvStreamFromCompanyAssociatedData(companyAssociatedData)

        logger.info("Creation of CSV for export successful.")

        return ResponseEntity
            .ok()
            .headers(buildHttpHeadersForExport(dataId, ExportFileType.CSV))
            .body(companyAssociatedDataCsv)
    }

    override fun exportCompanyAssociatedDataToExcel(dataId: String): ResponseEntity<InputStreamResource> {
        val metaInfo = dataMetaInformationManager.getDataMetaInformationByDataId(dataId)
        this.verifyAccess(metaInfo)
        val companyId = metaInfo.company.companyId
        val correlationId = IdUtils.generateCorrelationId(companyId = companyId, dataId = dataId)
        val companyAssociatedData =
            this.buildCompanyAssociatedData(dataId, companyId, metaInfo.reportingPeriod, correlationId)

        logger.info(logMessageBuilder.getCompanyAssociatedDataSuccessMessage(dataId, companyId, correlationId))

        val companyAssociatedDataExcel = dataExportService.buildExcelStreamFromCompanyAssociatedData(companyAssociatedData)

        logger.info("Creation of Excel for export successful.")

        return ResponseEntity
            .ok()
            .headers(buildHttpHeadersForExport(dataId, ExportFileType.EXCEL))
            .body(companyAssociatedDataExcel)
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

    @Throws(AccessDeniedException::class)
    private fun verifyAccess(metaInfo: DataMetaInformationEntity) {
        if (!metaInfo.isDatasetViewableByUser(DatalandAuthentication.fromContextOrNull())) {
            throw AccessDeniedException(logMessageBuilder.generateAccessDeniedExceptionMessage(metaInfo.qaStatus))
        }
    }
}
