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
import org.dataland.datalandbackendutils.model.BasicDataPointDimensions
import org.dataland.datalandinternalstorage.openApiClient.api.StorageControllerApi
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service
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
        fun processDataPoint(
            uploadedDataPoint: UploadedDataPoint,
            uploaderUserId: String,
            bypassQa: Boolean,
            correlationId: String,
        ): DataPointMetaInformation {
            dataPointValidator.validateDataPoint(uploadedDataPoint.dataPointType, uploadedDataPoint.dataPoint, correlationId)
            logger.info("Storing '${uploadedDataPoint.dataPointType}' data point with bypassQa set to: $bypassQa.")
            val dataPointId = IdUtils.generateUUID()

            if (bypassQa && !companyRoleChecker.canUserBypassQa(uploadedDataPoint.companyId)) {
                throw AccessDeniedException(logMessageBuilder.bypassQaDeniedExceptionMessage)
            }

            val dataPointMetaInformation =
                storeDataPoint(
                    uploadedDataPoint = uploadedDataPoint,
                    dataPointId = dataPointId,
                    uploaderUserId = uploaderUserId,
                    correlationId = correlationId,
                    uploadTime = Instant.now().toEpochMilli(),
                )
            messageQueuePublications.publishDataPointUploadedMessageWithBypassQa(dataPointId, bypassQa, correlationId)
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

            return dataPointMetaInformationEntity.toApiModel(DatalandAuthentication.fromContextOrNull())
        }

        /**
         * Checks if a company is associated with a data point marked for public access.
         * @param dataId the id of the data point
         * @return true if the company is associated with the data point, false otherwise
         */
        fun isCompanyAssociatedWithDataPointMarkedForPublicAccess(dataId: String): Boolean {
            val metaInfo = metaDataManager.getDataPointMetaInformationByDataId(dataId)
            return companyQueryManager.isCompanyPublic(metaInfo.companyId)
        }

        /**
         * Retrieves a single data point from the internal storage
         * @param dataId the id of the data point
         * @param correlationId the correlation id for the operation
         * @return the data point in form of a StorableDataset
         */
        fun retrieveDataPoint(
            dataId: String,
            correlationId: String,
        ): UploadedDataPoint {
            val metaInfo = metaDataManager.getDataPointMetaInformationByDataId(dataId)
            if (!metaInfo.isDatasetViewableByUser(DatalandAuthentication.fromContextOrNull())) {
                throw AccessDeniedException(logMessageBuilder.generateAccessDeniedExceptionMessage(metaInfo.qaStatus))
            }
            val dataPointType = metaInfo.dataPointType
            logger.info("Retrieving $dataPointType data point with id $dataId (correlation ID: $correlationId).")
            dataPointValidator.validateDataPointTypeExists(dataPointType)

            val dataFromCache = dataManager.getDataFromCache(dataId)
            if (dataFromCache != null) {
                return objectMapper.readValue(dataFromCache)
            }

            val storedDataPoint = storageClient.selectDataPointById(dataId, correlationId)
            return UploadedDataPoint(
                dataPoint = storedDataPoint.dataPoint,
                dataPointType = storedDataPoint.dataPointType,
                companyId = storedDataPoint.companyId,
                reportingPeriod = storedDataPoint.reportingPeriod,
            )
        }

        /**
         * Update the currently active data point for specific data point dimensions
         * @param dataPointDimensions the data point dimension to update the currently active data point for
         * @param newActiveDataId the id of the new active data point
         * @param correlationId the correlation id for the operation
         */
        fun updateCurrentlyActiveDataPoint(
            dataPointDimensions: BasicDataPointDimensions,
            newActiveDataId: String?,
            correlationId: String,
        ) {
            logger.info("Updating currently active data point for $dataPointDimensions (correlation ID: $correlationId).")
            val currentlyActiveDataId = metaDataManager.getCurrentlyActiveDataId(dataPointDimensions)
            logger.info("Currently and newly active IDs are $currentlyActiveDataId and $newActiveDataId (correlation ID: $correlationId).")
            if (newActiveDataId.isNullOrEmpty() && !currentlyActiveDataId.isNullOrEmpty()) {
                logger.info("Setting data point with dataId $currentlyActiveDataId to inactive (correlation ID: $correlationId).")
                metaDataManager.updateCurrentlyActiveFlagOfDataPoint(currentlyActiveDataId, null)
            } else if (newActiveDataId != currentlyActiveDataId) {
                logger.info("Setting $newActiveDataId to active and $currentlyActiveDataId to inactive (correlation ID: $correlationId).")
                metaDataManager.updateCurrentlyActiveFlagOfDataPoint(currentlyActiveDataId, null)
                metaDataManager.updateCurrentlyActiveFlagOfDataPoint(newActiveDataId, true)
            } else {
                logger.info("No update of the currently active flag required (correlation ID: $correlationId).")
            }
        }
    }
