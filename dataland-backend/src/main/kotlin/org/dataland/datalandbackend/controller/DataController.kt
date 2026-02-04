package org.dataland.datalandbackend.controller

import org.apache.poi.ss.formula.functions.T
import org.dataland.datalandbackend.api.DataApi
import org.dataland.datalandbackend.entities.DataMetaInformationEntity
import org.dataland.datalandbackend.exceptions.DownloadDataNotFoundApiException
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.model.StorableDataset
import org.dataland.datalandbackend.model.companies.CompanyAssociatedData
import org.dataland.datalandbackend.model.enums.export.ExportJobProgressState
import org.dataland.datalandbackend.model.export.ExportJob
import org.dataland.datalandbackend.model.export.ExportJobInfo
import org.dataland.datalandbackend.model.export.ExportLatestRequestData
import org.dataland.datalandbackend.model.export.ExportRequestData
import org.dataland.datalandbackend.model.metainformation.DataAndMetaInformation
import org.dataland.datalandbackend.model.metainformation.DataMetaInformation
import org.dataland.datalandbackend.repositories.utils.DataMetaInformationSearchFilter
import org.dataland.datalandbackend.services.CompanyQueryManager
import org.dataland.datalandbackend.services.DataExportService
import org.dataland.datalandbackend.services.DataExportStore
import org.dataland.datalandbackend.services.DataMetaInformationManager
import org.dataland.datalandbackend.services.DatasetStorageService
import org.dataland.datalandbackend.services.LogMessageBuilder
import org.dataland.datalandbackend.utils.DataTypeNameMapper
import org.dataland.datalandbackend.utils.IdUtils
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandbackendutils.model.BasicDataDimensions
import org.dataland.datalandbackendutils.model.BasicDatasetDimensions
import org.dataland.datalandbackendutils.model.ExportFileType
import org.dataland.datalandbackendutils.model.ListDataDimensions
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandbackendutils.utils.JsonUtils.defaultObjectMapper
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import java.time.Instant
import java.util.UUID

/**
 * Abstract implementation of the controller for data exchange of an abstract type T
 * @param datasetStorageService service to handle data storage
 * @param dataMetaInformationManager service for retrieving data meta-information
 * @param dataExportService service for handling data export to CSV and JSON
 * @param companyQueryManager service to retrieve company information
 */
open class DataController<T>(
    private val datasetStorageService: DatasetStorageService,
    private val dataMetaInformationManager: DataMetaInformationManager,
    private val dataExportService: DataExportService<T>,
    private val dataExportStorage: DataExportStore,
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
                data = defaultObjectMapper.readValue(frameworkData.values.first(), clazz),
            ),
        )
    }

    private fun getData(
        dataId: String,
        correlationId: String,
    ): T {
        val dataAsString = datasetStorageService.getDatasetData(dataId, dataType.toString(), correlationId)
        return defaultObjectMapper.readValue(dataAsString, clazz)
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
                    defaultObjectMapper.readValue(it.data, clazz),
                )
            },
        )
    }

    override fun postExportJobCompanyAssociatedDataByDimensions(
        exportRequestData: ExportRequestData,
        keepValueFieldsOnly: Boolean,
        includeAliases: Boolean,
    ): ResponseEntity<ExportJobInfo> =
        createAndStartExportJob(
            companyIds = exportRequestData.companyIds,
            fileFormat = exportRequestData.fileFormat,
            startExport = { exportJobEntity ->
                dataExportService.startExportJob(
                    ListDataDimensions(
                        exportRequestData.companyIds,
                        exportRequestData.reportingPeriods,
                        listOf(dataType.toString()),
                    ),
                    exportRequestData.fileFormat,
                    exportJobEntity,
                    clazz,
                    keepValueFieldsOnly,
                    includeAliases,
                )
            },
        )

    override fun postExportLatestJobCompanyAssociatedDataByDimensions(
        exportRequestData: ExportLatestRequestData,
        keepValueFieldsOnly: Boolean,
        includeAliases: Boolean,
    ): ResponseEntity<ExportJobInfo> =
        createAndStartExportJob(
            companyIds = exportRequestData.companyIds,
            fileFormat = exportRequestData.fileFormat,
            startExport = { exportJobEntity ->
                dataExportService.startLatestExportJob(
                    exportRequestData.companyIds,
                    exportRequestData.fileFormat,
                    exportJobEntity,
                    clazz,
                    keepValueFieldsOnly,
                    includeAliases,
                )
            },
        )

    private fun createAndStartExportJob(
        companyIds: List<String>,
        fileFormat: ExportFileType,
        startExport: (exportJobEntity: ExportJob) -> Unit,
    ): ResponseEntity<ExportJobInfo> {
        if (companyQueryManager.validateCompanyIdentifiers(companyIds).all {
                it.companyInformation == null
            }
        ) {
            throw ResourceNotFoundApiException(
                summary = "CompanyIds $companyIds not found.",
                message = "All provided companyIds are invalid. Please provide at least one valid companyId.",
            )
        }

        val exportJobId = UUID.randomUUID()
        logger.info("Received a request to export portfolio data. ID of new export Job: $exportJobId")

        val newExportJobEntity =
            dataExportStorage
                .createAndSaveExportJob(
                    exportJobId,
                    fileFormat,
                    DataTypeNameMapper.getDisplayName(dataType.name) ?: "",
                )

        try {
            // Async function
            startExport(newExportJobEntity)
        } catch (_: DownloadDataNotFoundApiException) {
            newExportJobEntity.progressState = ExportJobProgressState.Failure
            return ResponseEntity.noContent().build()
        }
        return ResponseEntity
            .ok(ExportJobInfo(id = exportJobId))
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
            data = defaultObjectMapper.writeValueAsString(companyAssociatedData.data),
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
    ): Map<BasicDatasetDimensions, String> =
        datasetStorageService.getDatasetData(
            companyAndReportingPeriodPairs.mapTo(mutableSetOf()) {
                BasicDatasetDimensions(
                    it.first,
                    framework,
                    it.second,
                )
            },
            correlationId,
        )

    @Throws(AccessDeniedException::class)
    private fun verifyAccess(metaInfo: DataMetaInformationEntity) {
        if (!metaInfo.isDatasetViewableByUser(DatalandAuthentication.fromContextOrNull())) {
            throw AccessDeniedException(logMessageBuilder.generateAccessDeniedExceptionMessage(metaInfo.qaStatus))
        }
    }

    override fun getLatestAvailableCompanyAssociatedData(identifier: String): ResponseEntity<CompanyAssociatedData<T>> {
        val correlationId = IdUtils.generateUUID()
        logger.info(logMessageBuilder.getLatestCompanyAssociatedDataMessage(identifier, correlationId))
        val companyIds =
            companyQueryManager.validateCompanyIdentifiers(listOf(identifier)).mapNotNull {
                it.companyInformation?.companyId
            }
        if (companyIds.size > 1) {
            throw InvalidInputApiException(
                summary = "Multiple companies found for identifier $identifier.",
                message = "The provided identifier: $identifier is ambiguous and matches multiple companies.",
            )
        }
        val companyId =
            companyIds.firstOrNull() ?: throw ResourceNotFoundApiException(
                summary = "Company with identifier $identifier not found.",
                message = "No company matches the provided identifier: $identifier.",
            )

        val latestData =
            datasetStorageService
                .getLatestAvailableData(
                    listOf(companyId),
                    dataType.toString(),
                    correlationId,
                ).firstOrNull() ?: throw ResourceNotFoundApiException(
                summary = "No available data found for company $companyId and data type $dataType.",
                message = "The company with ID $companyId has no available data for the requested data type: $dataType.",
            )

        return ResponseEntity.ok(
            CompanyAssociatedData(
                companyId = companyId,
                reportingPeriod = latestData.dimensions.reportingPeriod,
                data = defaultObjectMapper.readValue(latestData.data, clazz),
            ),
        )
    }
}
