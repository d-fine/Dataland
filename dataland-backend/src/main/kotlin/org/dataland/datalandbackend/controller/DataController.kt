package org.dataland.datalandbackend.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.api.DataApi
import org.dataland.datalandbackend.entities.DataMetaInformationEntity
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.model.StorableDataSet
import org.dataland.datalandbackend.model.companies.CompanyAssociatedData
import org.dataland.datalandbackend.model.metainformation.DataAndMetaInformation
import org.dataland.datalandbackend.model.metainformation.DataMetaInformation
import org.dataland.datalandbackend.services.DataExportService
import org.dataland.datalandbackend.services.DataManager
import org.dataland.datalandbackend.services.DataMetaInformationManager
import org.dataland.datalandbackend.services.LogMessageBuilder
import org.dataland.datalandbackend.utils.IdUtils
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
 * @param dataManager service to handle data
 * @param dataMetaInformationManager service for handling data meta information
 * @param objectMapper the mapper to transform strings into classes and vice versa
 */

abstract class DataController<T>(
    var dataManager: DataManager,
    var dataMetaInformationManager: DataMetaInformationManager,
    var dataExportService: DataExportService,
    var objectMapper: ObjectMapper,
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
        val dataIdOfPostedData = dataManager.processDataStorageRequest(datasetToStore, bypassQa, correlationId)
        logger.info(logMessageBuilder.postCompanyAssociatedDataSuccessMessage(companyId, correlationId))

        return ResponseEntity.ok(
            DataMetaInformation(
                dataId = dataIdOfPostedData, companyId = companyId, dataType = dataType,
                uploaderUserId = userId, uploadTime = uploadTime, reportingPeriod = reportingPeriod,
                currentlyActive = false, qaStatus = QaStatus.Pending,
            ),
        )
    }

    override fun getCompanyAssociatedData(dataId: String): ResponseEntity<CompanyAssociatedData<T>> {
        val metaInfo = dataMetaInformationManager.getDataMetaInformationByDataId(dataId)
        this.verifyAccess(metaInfo)
        val companyId = metaInfo.company.companyId
        val correlationId = IdUtils.generateCorrelationId(companyId = companyId, dataId = dataId)
        val companyAssociatedData =
            this.buildCompanyAssociatedData(dataId, companyId, metaInfo.reportingPeriod, correlationId)
        logger.info(logMessageBuilder.getCompanyAssociatedDataSuccessMessage(dataId, companyId, correlationId))
        return ResponseEntity.ok(companyAssociatedData)
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
                companyId, dataType, showOnlyActive, reportingPeriod, null, null,
            )
        val authentication = DatalandAuthentication.fromContextOrNull()
        val listOfFrameworkDataAndMetaInfo = mutableListOf<DataAndMetaInformation<T>>()
        metaInfos.filter { it.isDatasetViewableByUser(authentication) }.forEach {
            val correlationId = IdUtils.generateCorrelationId(companyId = companyId, dataId = null)
            val dataAsString =
                dataManager
                    .getPublicDataSet(
                        it.dataId, DataType.valueOf(it.dataType),
                        correlationId,
                    ).data
            listOfFrameworkDataAndMetaInfo.add(
                DataAndMetaInformation(
                    it.toApiModel(DatalandAuthentication.fromContext()), objectMapper.readValue(dataAsString, clazz),
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
    ): StorableDataSet =
        StorableDataSet(
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
            data = objectMapper.readValue(dataManager.getPublicDataSet(dataId, dataType, correlationId).data, clazz),
        )
    }

    @Throws(AccessDeniedException::class)
    private fun verifyAccess(metaInfo: DataMetaInformationEntity) {
        if (!metaInfo.isDatasetViewableByUser(DatalandAuthentication.fromContextOrNull())) {
            throw AccessDeniedException(logMessageBuilder.generateAccessDeniedExceptionMessage(metaInfo.qaStatus))
        }
    }
}
