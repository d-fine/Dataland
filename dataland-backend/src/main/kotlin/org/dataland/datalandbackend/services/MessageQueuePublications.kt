package org.dataland.datalandbackend.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.model.StoredCompany
import org.dataland.datalandbackend.model.metainformation.DataPointMetaInformation
import org.dataland.datalandbackendutils.utils.QaBypass
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
import org.dataland.datalandmessagequeueutils.messages.data.DataIdPayload
import org.dataland.datalandmessagequeueutils.messages.data.DataMetaInfoPatchPayload
import org.dataland.datalandmessagequeueutils.messages.data.DataPointUploadedPayload
import org.dataland.datalandmessagequeueutils.messages.data.DataUploadedPayload
import org.dataland.datalandmessagequeueutils.messages.data.InitialQaStatus
import org.dataland.datalandmessagequeueutils.messages.data.PresetQaStatus
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

/**
 * Component that bundles the message queue interactions of the data point manager
 * @param cloudEventMessageHandler cloud event message handler used for sending messages to the message queue
 * @param objectMapper object mapper used for converting data classes to strings and vice versa
*/

@Service
class MessageQueuePublications(
    @Autowired private val cloudEventMessageHandler: CloudEventMessageHandler,
    @Autowired private val objectMapper: ObjectMapper,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Method to publish a message that a data point has been uploaded
     * @param dataPointMetaInformation Information about the uploaded datapoint
     * @param companyInformation Information about the company the data point belongs to
     * @param bypassQa Whether the QA process should be bypassed
     * @param correlationId The correlation ID of the request initiating the event
     */
    fun publishDataPointUploadedMessageWithBypassQa(
        dataPointMetaInformation: DataPointMetaInformation,
        companyInformation: StoredCompany,
        bypassQa: Boolean,
        correlationId: String,
    ) {
        val (qaStatus, comment) = QaBypass.getCommentAndStatusForBypass(bypassQa)

        publishDataPointUploadedMessage(
            dataPointMetaInformation = dataPointMetaInformation,
            companyInformation = companyInformation,
            initialQa =
                PresetQaStatus(
                    qaStatus = qaStatus,
                    qaComment = comment,
                ),
            correlationId = correlationId,
        )
    }

    /**
     * Method to publish a message that a data point has been uploaded
     * @param dataPointMetaInformation The meta information of the uploaded data point
     * @param companyInformation The company information of the company the data point belongs to
     * @param initialQa The initial QA status of the data point
     * @param correlationId The correlation ID of the request initiating the event
     */
    fun publishDataPointUploadedMessage(
        dataPointMetaInformation: DataPointMetaInformation,
        companyInformation: StoredCompany,
        initialQa: InitialQaStatus,
        correlationId: String,
    ) {
        logger
            .info(
                "Publish message that data point with ID '${dataPointMetaInformation.dataPointId}' " +
                    "has been uploaded. Correlation ID: '$correlationId'.",
            )
        cloudEventMessageHandler.buildCEMessageAndSendToQueue(
            body =
                objectMapper.writeValueAsString(
                    DataPointUploadedPayload(
                        dataPointId = dataPointMetaInformation.dataPointId,
                        companyId = companyInformation.companyId,
                        companyName = companyInformation.companyInformation.companyName,
                        dataPointType = dataPointMetaInformation.dataPointType,
                        reportingPeriod = dataPointMetaInformation.reportingPeriod,
                        uploadTime = dataPointMetaInformation.uploadTime,
                        uploaderUserId =
                            dataPointMetaInformation.uploaderUserId,
                        initialQa = initialQa,
                    ),
                ),
            type = MessageType.PUBLIC_DATA_RECEIVED,
            correlationId = correlationId,
            exchange = ExchangeName.BACKEND_DATA_POINT_EVENTS,
            routingKey = RoutingKeyNames.DATA_POINT_UPLOAD,
        )
    }

    /**
     * Method to publish a message that a dataset has been uploaded
     * @param dataId The ID of the uploaded dataset
     * @param bypassQa Whether the QA process should be bypassed
     * @param correlationId The correlation ID of the request initiating the event
     */
    fun publishDatasetUploadedMessage(
        dataId: String,
        bypassQa: Boolean,
        correlationId: String,
    ) {
        publishDatasetUploadMessage(
            dataId = dataId,
            bypassQa = bypassQa,
            correlationId = correlationId,
            routingKey = RoutingKeyNames.DATASET_UPLOAD,
        )
    }

    /**
     * Method to publish a message that the meta info of a data set has been updated
     * @param dataId The ID of the uploaded data set
     * @param uploaderUserId new uploaderUserId of storable dataset
     * @param correlationId The correlation ID of the request initiating the event
     */
    fun publishDatasetMetaInfoPatchMessage(
        dataId: String,
        uploaderUserId: String,
        correlationId: String,
    ) {
        logger.info(
            "Publish message that metaInfo for data set with ID '$dataId' has been updated. " +
                "Correlation ID: '$correlationId'.",
        )
        cloudEventMessageHandler.buildCEMessageAndSendToQueue(
            body = objectMapper.writeValueAsString(DataMetaInfoPatchPayload(dataId = dataId, uploaderUserId = uploaderUserId)),
            type = MessageType.METAINFO_UPDATED,
            correlationId = correlationId,
            exchange = ExchangeName.BACKEND_DATASET_EVENTS,
            routingKey = RoutingKeyNames.METAINFORMATION_PATCH,
        )
    }

    /**
     * Method to publish a message that a dataset requires QA
     * @param dataId The ID of the dataset
     * @param bypassQa Whether the QA process should be bypassed
     * @param correlationId The correlation ID of the request initiating the event
     */
    fun publishDatasetQaRequiredMessage(
        dataId: String,
        bypassQa: Boolean,
        correlationId: String,
    ) {
        logger.info("Publish message that dataset with ID '$dataId' needs to undergo QA. Correlation ID: '$correlationId'.")
        publishDatasetUploadMessage(
            dataId = dataId,
            bypassQa = bypassQa,
            correlationId = correlationId,
            routingKey = RoutingKeyNames.DATASET_QA,
        )
    }

    /**
     * Method to publish a message that a dataset has to be deleted
     * @param dataId The ID of the dataset to be deleted
     * @param correlationId The correlation ID of the request initiating the event
     */
    fun publishDatasetDeletionMessage(
        dataId: String,
        correlationId: String,
    ) {
        logger.info("Publish message that dataset with ID '$dataId' has to be deleted. Correlation ID: '$correlationId'.")
        cloudEventMessageHandler.buildCEMessageAndSendToQueue(
            body = objectMapper.writeValueAsString(DataIdPayload(dataId = dataId)),
            type = MessageType.DELETE_DATA,
            correlationId = correlationId,
            exchange = ExchangeName.BACKEND_DATASET_EVENTS,
            routingKey = RoutingKeyNames.DATASET_DELETION,
        )
    }

    /**
     * Method to publish a message that a dataset has been migrated to an assembled dataset
     */
    fun publishDatasetMigratedMessage(
        dataId: String,
        correlationId: String,
    ) {
        logger.info(
            "Publish message that dataset with ID '$dataId' was migrated to an assembled dataset. " +
                "Correlation ID: '$correlationId'.",
        )
        cloudEventMessageHandler.buildCEMessageAndSendToQueue(
            body = objectMapper.writeValueAsString(DataIdPayload(dataId = dataId)),
            type = MessageType.DATA_MIGRATED,
            correlationId = correlationId,
            exchange = ExchangeName.BACKEND_DATASET_EVENTS,
            routingKey = RoutingKeyNames.DATASET_STORED_TO_ASSEMBLED_MIGRATION,
        )
    }

    /**
     * Method to publish a message that a dataset has been uploaded and either needs to be stored or just undergo QA
     * @param dataId The ID of the uploaded dataset
     * @param bypassQa Whether the QA process should be bypassed
     * @param correlationId The correlation ID of the request initiating the event
     * @param routingKey The routing key to steer which consumers pick up on the event
     */
    private fun publishDatasetUploadMessage(
        dataId: String,
        bypassQa: Boolean,
        correlationId: String,
        routingKey: String,
    ) {
        logger.info("Publish message that dataset with ID '$dataId' has been uploaded. Correlation ID: '$correlationId'.")
        cloudEventMessageHandler.buildCEMessageAndSendToQueue(
            body = objectMapper.writeValueAsString(DataUploadedPayload(dataId = dataId, bypassQa = bypassQa)),
            type = MessageType.PUBLIC_DATA_RECEIVED,
            correlationId = correlationId,
            exchange = ExchangeName.BACKEND_DATASET_EVENTS,
            routingKey = routingKey,
        )
    }
}
