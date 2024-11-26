package org.dataland.datalandbackend.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.model.datapoints.UploadedDataPoint
import org.dataland.datalandbackend.model.metainformation.DataPointMetaInformation
import org.dataland.datalandbackend.utils.DataPointValidator
import org.dataland.datalandbackend.utils.IdUtils
import org.dataland.datalandinternalstorage.openApiClient.api.StorageControllerApi
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 * Class for managing data points and associated validations
 * @param dataManager service for handling data storage
 * @param metaDataManager service for handling data meta information
 */
@Component("DataPointManager")
class DataPointManager(
    @Autowired private val dataManager: DataManager,
    @Autowired private val metaDataManager: DataMetaInformationManager,
    @Autowired private val storageClient: StorageControllerApi,
    @Autowired private val messageQueueInteractionForDataPoints: MessageQueueInteractionForDataPoints,
    @Autowired private val dataPointValidator: DataPointValidator,
    @Autowired private val objectMapper: ObjectMapper,
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
        return storeDataPoint(uploadedDataPoint, uploaderUserId, bypassQa, correlationId)
    }

    /**
     * Stores a single data point in the internal storage
     * @param uploadedDataPoint the data point to store
     * @param uploaderUserId the user id of the user who uploaded the data point
     * @param bypassQa whether to bypass the QA process
     * @param correlationId the correlation id for the operation
     * @return the id of the stored data point
     */
    fun storeDataPoint(
        uploadedDataPoint: UploadedDataPoint,
        uploaderUserId: String,
        bypassQa: Boolean,
        correlationId: String,
    ): DataPointMetaInformation {
        logger.info("Storing '${uploadedDataPoint.dataPointIdentifier}' data point with bypassQa set to: $bypassQa.")
        val dataId = IdUtils.generateUUID()
        val dataPointMetaInformationEntity = uploadedDataPoint.toDataPointMetaInformationEntity(dataId, uploaderUserId)
        metaDataManager.storeDataPointMetaInformation(dataPointMetaInformationEntity)
        dataManager.storeDataInTemporaryStorage(dataId, objectMapper.writeValueAsString(uploadedDataPoint), correlationId)
        messageQueueInteractionForDataPoints.publishDataPointUploadedMessage(dataId, correlationId)

        return dataPointMetaInformationEntity.toApiModel(DatalandAuthentication.fromContextOrNull())
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
}
