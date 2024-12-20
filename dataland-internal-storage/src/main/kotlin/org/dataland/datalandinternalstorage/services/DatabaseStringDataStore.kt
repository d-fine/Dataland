package org.dataland.datalandinternalstorage.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.openApiClient.api.TemporarilyCachedDataControllerApi
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandinternalstorage.entities.DataItem
import org.dataland.datalandinternalstorage.entities.DataPointItem
import org.dataland.datalandinternalstorage.model.StorableDataPoint
import org.dataland.datalandinternalstorage.repositories.DataItemRepository
import org.dataland.datalandinternalstorage.repositories.DataPointItemRepository
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.datalandmessagequeueutils.constants.ActionType
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageHeaderKey
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.constants.QueueNames
import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
import org.dataland.datalandmessagequeueutils.exceptions.MessageQueueRejectException
import org.dataland.datalandmessagequeueutils.messages.ManualQaRequestedMessage
import org.dataland.datalandmessagequeueutils.messages.data.DataUploadPayload
import org.dataland.datalandmessagequeueutils.utils.MessageQueueUtils
import org.json.JSONObject
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.annotation.Argument
import org.springframework.amqp.rabbit.annotation.Exchange
import org.springframework.amqp.rabbit.annotation.Queue
import org.springframework.amqp.rabbit.annotation.QueueBinding
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

/**
 * Simple implementation of a data store using a postgres database
 * @param dataItemRepository the repository for data items
 * @param cloudEventMessageHandler service for managing CloudEvents messages
 * @param temporarilyCachedDataClient the service for retrieving data from the temporary storage
 * @param objectMapper object mapper used for converting data classes to strings and vice versa
 */
@Component
class DatabaseStringDataStore(
    @Autowired private var dataItemRepository: DataItemRepository,
    @Autowired private var dataPointItemRepository: DataPointItemRepository,
    @Autowired var cloudEventMessageHandler: CloudEventMessageHandler,
    @Autowired var temporarilyCachedDataClient: TemporarilyCachedDataControllerApi,
    @Autowired var objectMapper: ObjectMapper,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Method that listens to the storage_queue and stores data into the database in case there is a message on the
     * storage_queue
     * @param payload the content of the message
     * @param correlationId the correlation ID of the current user process
     * @param type the type of the message
     */
    @RabbitListener(
        bindings = [
            QueueBinding(
                value =
                    Queue(
                        "requestReceivedInternalStorageDatabaseDataStore",
                        arguments = [
                            Argument(name = "x-dead-letter-exchange", value = ExchangeName.DEAD_LETTER),
                            Argument(name = "x-dead-letter-routing-key", value = "deadLetterKey"),
                            Argument(name = "defaultRequeueRejected", value = "false"),
                        ],
                    ),
                exchange = Exchange(ExchangeName.REQUEST_RECEIVED, declare = "false"),
                key = [""],
            ),
        ],
    )
    fun distributeIncomingRequests(
        @Payload payload: String,
        @Header(MessageHeaderKey.CORRELATION_ID) correlationId: String,
        @Header(MessageHeaderKey.TYPE) type: String,
    ) {
        MessageQueueUtils.validateMessageType(type, MessageType.PUBLIC_DATA_RECEIVED)
        val dataId = JSONObject(payload).getString("dataId")
        val actionType = JSONObject(payload).getString("actionType")
        if (dataId.isEmpty()) {
            throw MessageQueueRejectException("Provided data ID is empty.")
        }
        MessageQueueUtils.rejectMessageOnException {
            if (actionType == ActionType.STORE_PUBLIC_DATA) {
                persistentlyStoreDataSetAndSendMessage(dataId, correlationId, payload)
            }
            if (actionType == ActionType.DELETE_DATA) {
                deleteDataItemWithoutTransactionAndSendMessage(dataId, correlationId)
            }
        }
    }

    /**
     * Method that listens to the data point storage queue and stores data points into the database
     * @param payload the content of the message
     * @param correlationId the correlation ID of the current user process
     * @param type the type of the message
     */
    @RabbitListener(
        bindings = [
            QueueBinding(
                value =
                    Queue(
                        QueueNames.DATA_POINT_STORAGE,
                        arguments = [
                            Argument(name = "x-dead-letter-exchange", value = ExchangeName.DEAD_LETTER),
                            Argument(name = "x-dead-letter-routing-key", value = "deadLetterKey"),
                            Argument(name = "defaultRequeueRejected", value = "false"),
                        ],
                    ),
                exchange = Exchange(ExchangeName.BACKEND_DATA_POINT_EVENTS, declare = "false"),
                key = [RoutingKeyNames.DATA_POINT_UPLOAD],
            ),
        ],
    )
    fun storeDataPoint(
        @Payload payload: String,
        @Header(MessageHeaderKey.CORRELATION_ID) correlationId: String,
        @Header(MessageHeaderKey.TYPE) type: String,
    ) {
        MessageQueueUtils.validateMessageType(type, MessageType.PUBLIC_DATA_RECEIVED)
        MessageQueueUtils.rejectMessageOnException {
            val dataUploadPayload = MessageQueueUtils.readMessagePayload<DataUploadPayload>(payload, objectMapper)
            val dataId = dataUploadPayload.dataId
            val dataPointString = retrieveData(dataId, correlationId)

            val storableDataPoint = objectMapper.readValue(dataPointString, StorableDataPoint::class.java)
            logger.info("Storing data point with data ID: $dataId and correlation ID: $correlationId.")
            storeDataPointItemWithoutTransaction(
                DataPointItem(
                    dataId = dataId,
                    companyId = storableDataPoint.companyId,
                    reportingPeriod = storableDataPoint.reportingPeriod,
                    dataPointIdentifier = storableDataPoint.dataPointIdentifier,
                    dataPointContent = objectMapper.writeValueAsString(storableDataPoint.dataPointContent),
                ),
            )
            publishStorageEvent(payload, correlationId)
        }
    }

    private fun retrieveData(
        dataId: String,
        correlationId: String,
    ): String {
        logger.info("Retrieving data for DataID $dataId from the backend. CorrelationId: $correlationId")
        return temporarilyCachedDataClient.getReceivedPublicData(dataId)
    }

    private fun publishStorageEvent(
        payload: String,
        correlationId: String,
    ) {
        logger.info("Publishing storage event to the message queue. CorrelationId: $correlationId")
        cloudEventMessageHandler.buildCEMessageAndSendToQueue(
            payload, MessageType.DATA_STORED, correlationId, ExchangeName.ITEM_STORED, RoutingKeyNames.DATA,
        )
    }

    /**
     * Method that stores data into the database in case there is a message on the storage_queue and sends a message to
     * the message queue
     * @param payload the content of the message
     * @param correlationId the correlation ID of the current user process
     * @param dataId the dataId of the dataset to be stored
     */
    fun persistentlyStoreDataSetAndSendMessage(
        dataId: String,
        correlationId: String,
        payload: String,
    ) {
        val data = retrieveData(dataId, correlationId)
        logger.info("Inserting data into database with data ID: $dataId and correlation ID: $correlationId.")
        storeDataItemWithoutTransaction(DataItem(dataId, objectMapper.writeValueAsString(data)))
        publishStorageEvent(payload, correlationId)

        val bypassQa = JSONObject(payload).getBoolean("bypassQa")
        val body =
            objectMapper.writeValueAsString(
                ManualQaRequestedMessage(
                    resourceId = dataId,
                    bypassQa = bypassQa,
                ),
            )
        cloudEventMessageHandler.buildCEMessageAndSendToQueue(
            body, MessageType.MANUAL_QA_REQUESTED, correlationId, ExchangeName.ITEM_STORED, RoutingKeyNames.DATA_QA,
        )
    }

    /**
     * Stores a Data Point Item while ensuring that there is no active transaction. This will guarantee that the data
     * Stores a Data Item while ensuring that there is no active transaction. This will guarantee that the write
     * is commited after exit of this method.
     * @param dataPointItem the DataItem to be stored
     */
    @Transactional(propagation = Propagation.NEVER)
    fun storeDataPointItemWithoutTransaction(dataPointItem: DataPointItem) {
        dataPointItemRepository.save(dataPointItem)
    }

    /**
     * Stores a Data Item while ensuring that there is no active transaction. This will guarantee that the write
     * point is commited after exit of this method.
     * @param dataItem the DatapointItem to be stored
     */
    @Transactional(propagation = Propagation.NEVER)
    fun storeDataItemWithoutTransaction(dataItem: DataItem) {
        dataItemRepository.save(dataItem)
    }

    /**
     * Reads data point from a database
     * @param dataId the ID of the data to be retrieved
     * @return the data as json string with ID dataId
     */
    fun selectDataPoint(
        dataId: String,
        correlationId: String,
    ): StorableDataPoint {
        val entry =
            dataPointItemRepository
                .findById(dataId)
                .orElseThrow {
                    logger.info("Data point with data ID: $dataId could not be found. Correlation ID: $correlationId.")
                    ResourceNotFoundApiException(
                        "Data point not found",
                        "No data point with the ID: $dataId could be found in the data store.",
                    )
                }

        return entry.toStorableDataPoint(objectMapper)
    }

    /**
     * Reads data from a database
     * @param dataId the ID of the data to be retrieved
     * @return the data as json string with ID dataId
     */
    fun selectDataSet(
        dataId: String,
        correlationId: String,
    ): String =
        dataItemRepository
            .findById(dataId)
            .orElseThrow {
                logger.info("Dataset with data ID: $dataId could not be found. Correlation ID: $correlationId.")
                ResourceNotFoundApiException(
                    "Dataset not found",
                    "No dataset with the ID: $dataId could be found in the data store.",
                )
            }.data

    /**
     * Deletes a Data Item while ensuring that there is no active transaction. This will guarantee that the write
     * is commited after exit of this method.
     * @param dataId the DataItem to be removed from the storage
     * @param correlationId the correlationId ot the current user process
     */
    @Transactional(propagation = Propagation.NEVER)
    fun deleteDataItemWithoutTransactionAndSendMessage(
        dataId: String,
        correlationId: String,
    ) {
        logger.info("Deleting data from database with data ID: $dataId and correlation ID: $correlationId.")
        dataItemRepository.deleteById(dataId)

        val body =
            objectMapper.writeValueAsString(
                ManualQaRequestedMessage(
                    resourceId = dataId,
                    bypassQa = null,
                ),
            )
        logger.info("Sending message to QA service to delete qa information on data ID $dataId (correlationID: $correlationId).")
        cloudEventMessageHandler.buildCEMessageAndSendToQueue(
            body, MessageType.MANUAL_QA_REQUESTED, correlationId, ExchangeName.ITEM_STORED, RoutingKeyNames.DELETE_QA_INFO,
        )
    }
}
