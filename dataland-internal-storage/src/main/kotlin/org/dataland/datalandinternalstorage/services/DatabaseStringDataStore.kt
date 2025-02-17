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
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageHeaderKey
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.constants.QueueNames
import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
import org.dataland.datalandmessagequeueutils.messages.data.DataIdPayload
import org.dataland.datalandmessagequeueutils.messages.data.DataPointUploadedPayload
import org.dataland.datalandmessagequeueutils.messages.data.DataUploadedPayload
import org.dataland.datalandmessagequeueutils.utils.MessageQueueUtils
import org.dataland.datalandmessagequeueutils.utils.getCorrelationId
import org.dataland.datalandmessagequeueutils.utils.getType
import org.dataland.datalandmessagequeueutils.utils.readMessagePayload
import org.slf4j.LoggerFactory
import org.springframework.amqp.core.Message
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
                        QueueNames.INTERNAL_STORAGE_DATASET_STORAGE,
                        arguments = [
                            Argument(name = "x-dead-letter-exchange", value = ExchangeName.DEAD_LETTER),
                            Argument(name = "x-dead-letter-routing-key", value = "deadLetterKey"),
                            Argument(name = "defaultRequeueRejected", value = "false"),
                        ],
                    ),
                exchange = Exchange(ExchangeName.BACKEND_DATASET_EVENTS, declare = "false"),
                key = [RoutingKeyNames.DATASET_UPLOAD],
            ),
        ],
    )
    fun storeDataset(
        @Payload payload: String,
        @Header(MessageHeaderKey.CORRELATION_ID) correlationId: String,
        @Header(MessageHeaderKey.TYPE) type: String,
    ) {
        MessageQueueUtils.validateMessageType(type, MessageType.PUBLIC_DATA_RECEIVED)
        MessageQueueUtils.rejectMessageOnException {
            val dataId = MessageQueueUtils.readMessagePayload<DataUploadedPayload>(payload, objectMapper).dataId
            MessageQueueUtils.validateDataId(dataId)
            val data = retrieveData(dataId, correlationId)
            logger.info("Inserting data into database with data ID: $dataId and correlation ID: $correlationId.")
            storeDataItemWithoutTransaction(DataItem(dataId, objectMapper.writeValueAsString(data)))
            publishStorageEvent(dataId, correlationId)
        }
    }

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
                        QueueNames.INTERNAL_STORAGE_DATASET_DELETION,
                        arguments = [
                            Argument(name = "x-dead-letter-exchange", value = ExchangeName.DEAD_LETTER),
                            Argument(name = "x-dead-letter-routing-key", value = "deadLetterKey"),
                            Argument(name = "defaultRequeueRejected", value = "false"),
                        ],
                    ),
                exchange = Exchange(ExchangeName.BACKEND_DATASET_EVENTS, declare = "false"),
                key = [RoutingKeyNames.DATASET_DELETION],
            ),
        ],
    )
    fun deleteDataset(
        @Payload payload: String,
        @Header(MessageHeaderKey.CORRELATION_ID) correlationId: String,
        @Header(MessageHeaderKey.TYPE) type: String,
    ) {
        MessageQueueUtils.validateMessageType(type, MessageType.DELETE_DATA)
        MessageQueueUtils.rejectMessageOnException {
            val dataId = MessageQueueUtils.readMessagePayload<DataUploadedPayload>(payload, objectMapper).dataId
            MessageQueueUtils.validateDataId(dataId)
            deleteDataItemWithoutTransaction(dataId, correlationId)
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
                        QueueNames.INTERNAL_STORAGE_DATA_POINT_STORAGE,
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
        containerFactory = "consumerBatchContainerFactory",
    )
    @Transactional
    fun storeDataPoint(messages: List<Message>) {
        logger.info("Processing ${messages.size} Data Point Received Messages.")

        MessageQueueUtils.rejectMessageOnException {
            val allDataAndCorrelationIds =
                messages.map {
                    MessageQueueUtils.validateMessageType(it.getType(), MessageType.PUBLIC_DATA_RECEIVED)
                    Pair(it.readMessagePayload<DataPointUploadedPayload>(objectMapper).dataPointId, it.getCorrelationId())
                }
            val allContents =
                temporarilyCachedDataClient.getBatchReceivedPublicData(
                    allDataAndCorrelationIds.map
                        { it.first },
                )
            val allStoredDataPoints = mutableListOf<DataPointItem>()

            for ((dataId, correlationId) in allDataAndCorrelationIds) {
                MessageQueueUtils.validateDataId(dataId)
                val dataPointString = allContents[dataId]
                val storableDataPoint = objectMapper.readValue(dataPointString, StorableDataPoint::class.java)
                logger.info("Storing data point with data ID: $dataId and correlation ID: $correlationId.")
                allStoredDataPoints.add(
                    DataPointItem(
                        dataPointId = dataId,
                        companyId = storableDataPoint.companyId,
                        reportingPeriod = storableDataPoint.reportingPeriod,
                        dataPointType = storableDataPoint.dataPointType,
                        dataPoint = objectMapper.writeValueAsString(storableDataPoint.dataPoint),
                    ),
                )
                publishStorageEvent(dataId, correlationId)
            }
            dataPointItemRepository.saveAll(allStoredDataPoints)
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
        dataId: String,
        correlationId: String,
    ) {
        logger.info("Publishing storage event for data ID $dataId to the message queue. CorrelationId: $correlationId")
        val payload = objectMapper.writeValueAsString(DataIdPayload(dataId = dataId))
        cloudEventMessageHandler.buildCEMessageAndSendToQueue(
            payload, MessageType.DATA_STORED, correlationId, ExchangeName.ITEM_STORED, RoutingKeyNames.DATA,
        )
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
     * Reads data points from a database
     * @param dataIds the IDs of the data to be retrieved
     * @return the data as json string with ID dataId
     */
    fun selectDataPoints(
        dataIds: List<String>,
        correlationId: String,
    ): Map<String, StorableDataPoint> {
        val retrievedEntries = dataPointItemRepository.findAllById(dataIds)
        val missingIdentifiers = dataIds.toSet() - retrievedEntries.map { it.dataPointId }.toSet()
        if (missingIdentifiers.isNotEmpty()) {
            logger.info("Data points with data IDs: $missingIdentifiers could not be found. Correlation ID: $correlationId.")
            throw ResourceNotFoundApiException(
                "Data points not found",
                "No data points with the IDs: $missingIdentifiers could be found in the data store.",
            )
        }
        return retrievedEntries.associate { it.dataPointId to it.toStorableDataPoint(objectMapper) }
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
    fun deleteDataItemWithoutTransaction(
        dataId: String,
        correlationId: String,
    ) {
        logger.info("Deleting data from database with data ID: $dataId and correlation ID: $correlationId.")
        dataItemRepository.deleteById(dataId)
    }
}
