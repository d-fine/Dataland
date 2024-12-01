package org.dataland.datalandbackend.services.datapoints

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.model.datapoints.UploadedDataPoint
import org.dataland.datalandbackend.model.metainformation.DataPointMetaInformation
import org.dataland.datalandbackend.services.CompanyQueryManager
import org.dataland.datalandbackend.services.CompanyRoleChecker
import org.dataland.datalandbackend.services.DataManager
import org.dataland.datalandbackend.services.LogMessageBuilder
import org.dataland.datalandbackend.utils.DataPointValidator
import org.dataland.datalandbackend.utils.IdUtils
import org.dataland.datalandbackendutils.model.DataPointDimensions
import org.dataland.datalandinternalstorage.openApiClient.api.StorageControllerApi
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service

/**
 * Class for managing data points and associated validations
 * @param dataManager service for handling data storage
 * @param metaDataManager service for handling data meta information
 */
@Suppress("LongParameterList")
@Service
class DataPointManager(
    @Autowired private val dataManager: DataManager,
    @Autowired private val metaDataManager: DataPointMetaInformationManager,
    @Autowired private val storageClient: StorageControllerApi,
    @Autowired private val messageQueueInteractionForDataPoints: MessageQueueInteractionForDataPoints,
    @Autowired private val dataPointValidator: DataPointValidator,
    @Autowired private val companyQueryManager: CompanyQueryManager,
    @Autowired private val companyRoleChecker: CompanyRoleChecker,
    @Autowired private val objectMapper: ObjectMapper,
    @Autowired private val logMessageBuilder: LogMessageBuilder,
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
        dataPointValidator.validateDataPoint(uploadedDataPoint.dataPointIdentifier, uploadedDataPoint.dataPointContent, correlationId)
        logger.info("Storing '${uploadedDataPoint.dataPointIdentifier}' data point with bypassQa set to: $bypassQa.")
        val dataId = IdUtils.generateUUID()

        if (bypassQa && !companyRoleChecker.canUserBypassQa(uploadedDataPoint.companyId)) {
            throw AccessDeniedException(logMessageBuilder.bypassQaDeniedExceptionMessage)
        }

        val dataPointMetaInformation = storeDataPoint(uploadedDataPoint, dataId, uploaderUserId, correlationId)
        messageQueueInteractionForDataPoints.publishDataPointUploadedMessage(dataId, correlationId)
        messageQueueInteractionForDataPoints.publishDataPointQaRequestedMessage(dataId, bypassQa, correlationId)
        return dataPointMetaInformation
    }

    /**
     * Stores a single data point in the internal storage
     * @param dataId the ID of the data point
     * @param uploadedDataPoint the data point to store
     * @param uploaderUserId the user id of the user who uploaded the data point
     * @param correlationId the correlation id for the operation
     * @return the id of the stored data point
     */
    fun storeDataPoint(
        uploadedDataPoint: UploadedDataPoint,
        dataId: String,
        uploaderUserId: String,
        correlationId: String,
    ): DataPointMetaInformation {
        val dataPointMetaInformationEntity = uploadedDataPoint.toDataPointMetaInformationEntity(dataId, uploaderUserId)
        metaDataManager.storeDataPointMetaInformation(dataPointMetaInformationEntity)
        dataManager.storeDataInTemporaryStorage(dataId, objectMapper.writeValueAsString(uploadedDataPoint), correlationId)

        return dataPointMetaInformationEntity.toApiModel(DatalandAuthentication.fromContextOrNull())
    }

    /**
     * Checks if a company is associated with a data point marked for public access (the function is used as part of the authorization
     * and wrongly flagged as unused by the IDE).
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
     * @return the data point in form of a StorableDataSet
     */
    fun retrieveDataPoint(
        dataId: String,
        correlationId: String,
    ): UploadedDataPoint {
        val metaInfo = metaDataManager.getDataPointMetaInformationByDataId(dataId)
        if (!metaInfo.isDatasetViewableByUser(DatalandAuthentication.fromContextOrNull())) {
            throw AccessDeniedException(logMessageBuilder.generateAccessDeniedExceptionMessage(metaInfo.qaStatus))
        }
        val dataPointIdentifier = metaInfo.dataPointIdentifier
        logger.info("Retrieving $dataPointIdentifier data point with id $dataId (correlation ID: $correlationId).")
        dataPointValidator.validateDataPointIdentifierExists(dataPointIdentifier)

        val storedDataPoint = storageClient.selectDataPointById(dataId, correlationId)
        return UploadedDataPoint(
            dataPointContent = storedDataPoint.dataPointContent,
            dataPointIdentifier = storedDataPoint.dataPointIdentifier,
            companyId = storedDataPoint.companyId,
            reportingPeriod = storedDataPoint.reportingPeriod,
        )
    }

    /**
     * Method to update the currently active data point for a specific data point dimension
     * @param dataPointDimensions the data point dimension to update the currently active data point for
     * @param newActiveDataId the id of the new active data point
     * @param correlationId the correlation id for the operation
     */
    fun updateCurrentlyActiveDataPoint(
        dataPointDimensions: DataPointDimensions,
        newActiveDataId: String?,
        correlationId: String,
    ) {
        logger.info("Updating currently active data point for $dataPointDimensions (correlation ID: $correlationId).")
        val currentlyActiveDataId = metaDataManager.getCurrentlyActiveDataId(dataPointDimensions)
        logger.info("Currently and newly active IDs are $currentlyActiveDataId and $newActiveDataId (correlation ID: $correlationId).")
        if (newActiveDataId.isNullOrEmpty() && !currentlyActiveDataId.isNullOrEmpty()) {
            logger.info("Setting data point with dataId $currentlyActiveDataId to inactive (correlation ID: $correlationId).")
            metaDataManager.updateCurrentlyActiveFlagOfDataPoint(currentlyActiveDataId, false)
        } else if (newActiveDataId != currentlyActiveDataId) {
            logger.info("Setting $newActiveDataId to active and $currentlyActiveDataId to inactive (correlation ID: $correlationId).")
            metaDataManager.updateCurrentlyActiveFlagOfDataPoint(currentlyActiveDataId, false)
            metaDataManager.updateCurrentlyActiveFlagOfDataPoint(newActiveDataId, true)
        } else {
            logger.info("No update of the currently active flag required (correlation ID: $correlationId).")
        }
    }
}
