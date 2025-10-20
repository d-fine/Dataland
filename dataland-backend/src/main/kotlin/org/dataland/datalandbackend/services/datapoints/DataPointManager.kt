package org.dataland.datalandbackend.services.datapoints

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.dataland.datalandbackend.model.datapoints.UploadedDataPoint
import org.dataland.datalandbackend.model.metainformation.DataPointMetaInformation
import org.dataland.datalandbackend.services.CompanyQueryManager
import org.dataland.datalandbackend.services.CompanyRoleChecker
import org.dataland.datalandbackend.services.DataManager
import org.dataland.datalandbackend.services.LogMessageBuilder
import org.dataland.datalandbackend.services.MessageQueuePublications
import org.dataland.datalandbackend.utils.DataPointValidator
import org.dataland.datalandbackend.utils.IdUtils
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandinternalstorage.openApiClient.api.StorageControllerApi
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

/**
 * Class for managing data points and associated validations
 * @param dataManager service for handling data storage
 * @param metaDataManager service for handling data meta information
 */
@Suppress("LongParameterList")
@Service("DataPointManager")
class DataPointManager
    @Autowired
    constructor(
        private val dataManager: DataManager,
        private val metaDataManager: DataPointMetaInformationManager,
        private val storageClient: StorageControllerApi,
        private val messageQueuePublications: MessageQueuePublications,
        private val dataPointValidator: DataPointValidator,
        private val companyQueryManager: CompanyQueryManager,
        private val companyRoleChecker: CompanyRoleChecker,
        private val objectMapper: ObjectMapper,
        private val logMessageBuilder: LogMessageBuilder,
    ) {
        private val logger = LoggerFactory.getLogger(javaClass)

        /**
         * Processes a single data point by validating it and storing it in the internal storage
         * @param uploadedDataPoint the data point to process
         * @param uploaderUserId the user id of the user who uploaded the data point
         * @param bypassQa whether to bypass the QA process
         * @param correlationId the correlation id for the operation
         * @return the meta information of the stored data point
         */
        @Transactional
        fun processDataPoint(
            uploadedDataPoint: UploadedDataPoint,
            uploaderUserId: String,
            bypassQa: Boolean,
            correlationId: String,
        ): DataPointMetaInformation {
            val castedDataPointObject =
                dataPointValidator
                    .validateDataPoint(uploadedDataPoint.dataPointType, uploadedDataPoint.dataPoint, correlationId)
            logger.info("Storing '${uploadedDataPoint.dataPointType}' data point with bypassQa set to: $bypassQa.")
            val dataPointId = IdUtils.generateUUID()

            if (bypassQa && !companyRoleChecker.canUserBypassQa(uploadedDataPoint.companyId)) {
                throw AccessDeniedException(logMessageBuilder.bypassQaDeniedExceptionMessage)
            }

            val companyInformation =
                companyQueryManager
                    .getCompanyById(uploadedDataPoint.companyId)
                    .toApiModel()

            val dataPointMetaInformation =
                storeDataPoint(
                    uploadedDataPoint = uploadedDataPoint.copy(dataPoint = objectMapper.writeValueAsString(castedDataPointObject)),
                    dataPointId = dataPointId,
                    uploaderUserId = uploaderUserId,
                    correlationId = correlationId,
                    uploadTime = Instant.now().toEpochMilli(),
                )
            messageQueuePublications.publishDataPointUploadedMessageWithBypassQa(
                dataPointMetaInformation = dataPointMetaInformation,
                companyInformation = companyInformation,
                bypassQa = bypassQa,
                correlationId = correlationId,
            )
            return dataPointMetaInformation
        }

        /**
         * Stores a single data point in the internal storage
         * @param dataPointId the ID of the data point
         * @param uploadedDataPoint the data point to store
         * @param uploaderUserId the user id of the user who uploaded the data point
         * @param correlationId the correlation id for the operation
         * @return the id of the stored data point
         */
        @Transactional
        fun storeDataPoint(
            uploadedDataPoint: UploadedDataPoint,
            dataPointId: String,
            uploaderUserId: String,
            uploadTime: Long,
            correlationId: String,
        ): DataPointMetaInformation {
            val dataPointMetaInformationEntity = uploadedDataPoint.toDataPointMetaInformationEntity(dataPointId, uploaderUserId, uploadTime)
            metaDataManager.storeDataPointMetaInformation(dataPointMetaInformationEntity)
            dataManager.storeDataInTemporaryStorage(dataPointId, objectMapper.writeValueAsString(uploadedDataPoint), correlationId)

            return dataPointMetaInformationEntity.toApiModel()
        }

        /**
         * Checks if a company is associated with a data point marked for public access.
         * @param dataId the id of the data point
         * @return true if the company is associated with the data point, false otherwise
         */
        @Transactional(readOnly = true)
        fun isCompanyAssociatedWithDataPointMarkedForPublicAccess(dataId: String): Boolean {
            val metaInfo = metaDataManager.getDataPointMetaInformationById(dataId)
            return companyQueryManager.isCompanyPublic(metaInfo.companyId)
        }

        /**
         * Retrieve a batch of data points by their Ids
         * Data is retrieved from the internal cache or the internal storage
         */
        @Transactional(readOnly = true)
        fun retrieveDataPoints(
            dataPointIds: Collection<String>,
            correlationId: String,
        ): Map<String, UploadedDataPoint> {
            logger.info("Retrieving ${dataPointIds.size} data points: $dataPointIds (correlation ID: $correlationId).")
            val dataPointMap = mutableMapOf<String, UploadedDataPoint>()
            val dataIdsToRequestFromInternalStorage = mutableListOf<String>()
            val allMetaInfo =
                metaDataManager
                    .getDataPointMetaInformationByIds(dataPointIds)
                    .associateBy { it.dataPointId }

            for (dataPointId in dataPointIds) {
                val metaInfo =
                    allMetaInfo[dataPointId] ?: throw ResourceNotFoundApiException(
                        "Data point not found",
                        "No data point with the id: $dataPointId could be found in the data store.",
                    )
                if (!metaInfo.isDataPointViewableByUser(DatalandAuthentication.fromContextOrNull())) {
                    throw AccessDeniedException(logMessageBuilder.generateAccessDeniedExceptionMessage(metaInfo.qaStatus))
                }
                val dataPointType = metaInfo.dataPointType
                dataPointValidator.validateDataPointTypeExists(dataPointType)

                val dataFromCache = dataManager.getDataFromCache(dataPointId)
                if (dataFromCache != null) {
                    dataPointMap[dataPointId] = objectMapper.readValue(dataFromCache)
                } else {
                    dataIdsToRequestFromInternalStorage.add(dataPointId)
                }
            }

            if (dataIdsToRequestFromInternalStorage.isNotEmpty()) {
                val dataPointsFromInternalStorage =
                    storageClient
                        .selectBatchDataPointsByIds(correlationId, dataIdsToRequestFromInternalStorage)
                dataPointsFromInternalStorage.forEach { (dataPointId, storedDataPoint) ->
                    dataPointMap[dataPointId] =
                        UploadedDataPoint(
                            dataPoint = storedDataPoint.dataPoint,
                            dataPointType = storedDataPoint.dataPointType,
                            companyId = storedDataPoint.companyId,
                            reportingPeriod = storedDataPoint.reportingPeriod,
                        )
                }
            }

            return dataPointMap
        }

        /**
         * Retrieves a single data point from the internal storage
         * @param dataPointId the id of the data point
         * @param correlationId the correlation id for the operation
         * @return the data point in form of a StorableDataset
         */
        @Transactional(readOnly = true)
        fun retrieveDataPoint(
            dataPointId: String,
            correlationId: String,
        ): UploadedDataPoint {
            val uploadedDataPoint = retrieveDataPoints(listOf(dataPointId), correlationId).values.first()
            return uploadedDataPoint.copy(
                dataPoint =
                    objectMapper.writeValueAsString(
                        dataPointValidator.validateDataPoint(
                            uploadedDataPoint.dataPointType,
                            uploadedDataPoint.dataPoint,
                            correlationId,
                        ),
                    ),
            )
        }
    }
