package org.dataland.datalandexternalstorage.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.openApiClient.api.TemporarilyCachedDataControllerApi
import org.dataland.datalandexternalstorage.entities.DataItem
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.datalandmessagequeueutils.constants.ActionType
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageHeaderKey
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
import org.dataland.datalandmessagequeueutils.exceptions.MessageQueueRejectException
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
 * Simple implementation of a data storing service using the EuroDaT data trustee
 * @param cloudEventMessageHandler service for managing CloudEvents messages
 * @param temporarilyCachedDataClient the service for retrieving data from the temporary storage
 * @param objectMapper object mapper used for converting data classes to strings and vice versa
 */
@Component
class EurodatStringDataStore(
    @Autowired var cloudEventMessageHandler: CloudEventMessageHandler,
    @Autowired var temporarilyCachedDataClient: TemporarilyCachedDataControllerApi,
    @Autowired var temporarilyCachedDocumentClient: StreamingTemporarilyCachedPrivateDocumentControllerApi,
    // @Autowired var databaseCredentialResourceClient: DatabaseCredentialResourceApi,
    @Autowired var objectMapper: ObjectMapper,
    @Autowired var messageUtils: MessageQueueUtils,
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
                value = Queue(
                    "requestReceivedEurodatDataStore",
                    arguments = [
                        Argument(name = "x-dead-letter-exchange", value = ExchangeName.DeadLetter),
                        Argument(name = "x-dead-letter-routing-key", value = "deadLetterKey"),
                        Argument(name = "defaultRequeueRejected", value = "false"),
                    ],
                ),
                exchange = Exchange(ExchangeName.PrivateRequestReceived, declare = "false"),
                key = [""],
            ),
        ],
    )
    fun distributeIncomingRequests(
        @Payload payload: String,
        @Header(MessageHeaderKey.CorrelationId) correlationId: String,
        @Header(MessageHeaderKey.Type) type: String,
    ) {
        messageUtils.validateMessageType(type, MessageType.PrivateDataReceived)
        val dataId = JSONObject(payload).getString("dataId")
        val actionType = JSONObject(payload).getString("actionType")
        if (dataId.isEmpty()) {
            throw MessageQueueRejectException("Provided data ID is empty.")
        }
        messageUtils.rejectMessageOnException {
            if (actionType == ActionType.StorePrivateDataAndDocuments) {
                // TODO remove this logger
                logger.info("Received DataID $dataId and CorrelationId: $correlationId")
                persistentlyStoreDataInEurodatAndSendMessage(dataId, correlationId, payload)
            }
        }
    }

    /**
     * Method that stores data in eurodat data truestee and sends a message to the message queue
     * @param payload the content of the message
     * @param correlationId the correlation ID of the current user process
     * @param dataId the dataId of the dataset to be stored
     */
    fun persistentlyStoreDataInEurodatAndSendMessage(dataId: String, correlationId: String, payload: String) {
        logger.info("Received DataID $dataId and CorrelationId: $correlationId")
        logger.info("payload: $payload")
        // TODO call the get /api/v1/client-controller/credential-service/database/safedeposit/{appId} for appID=minaboApp to get credentials
        // val getAuthentication = DatabaseCredentialResourceApi.
        val data = temporarilyCachedDataClient.getReceivedPrivateData(dataId)
        //val documentHashList = JSONObject(payload).getJSONArray("listOfDocumentHashes").toList()
        logger.info("Inserting data into database with data ID: $dataId and correlation ID: $correlationId.")
         storeDataInEurodat(dataId, correlationId, DataItem(dataId, objectMapper.writeValueAsString(data)))
        val jsonArray = JSONObject(payload).getJSONArray("listOfDocumentHashes")

        val list: List<String> = List(jsonArray.length()) { jsonArray.getString(it) }
        storeBlobInEurodat(dataId, correlationId, list)
        cloudEventMessageHandler.buildCEMessageAndSendToQueue(
            payload, MessageType.PrivateDataStored, correlationId, ExchangeName.PrivateItemStored, RoutingKeyNames.data,
        )
    }

    /**
     * Stores a Data Item in eurodat while ensuring that there is no active transaction. This will guarantee that the write
     * is commited after exit of this method.
     * @param dataItem the DataItem to be stored
     */
    @Transactional(propagation = Propagation.NEVER)
    fun storeDataInEurodat(dataId: String, correlationId: String, dataItem: DataItem) {
        logger.info("Storing data for dataId $dataId and correlationId $correlationId in eurodat storage service")
        // TODO call to eurodat
        // dataItemRepository.save(dataItem)
        // DatabaseCredentialResourceApi.apiV1ClientControllerCredentialServiceDatabaseSafedepositAppIdGet()
    }
    /**
     * Stores a Blob Item in eurodat while ensuring that there is no active transaction. This will guarantee that the write
     * is commited after exit of this method.
     * @param dataItem the DataItem to be stored
     */
    @Transactional(propagation = Propagation.NEVER)
    fun storeBlobInEurodat(dataId: String, correlationId: String, documentHashList: List<String>) {
        logger.info("Retrieving documents associated with dataId $dataId and correlationId $correlationId")
        documentHashList.forEach{hash ->
            val resource =temporarilyCachedDocumentClient.getReceivedPrivateDocument(hash)
            val test =  resource.readBytes()
        }
        // TODO call to eurodat
        // DatabaseCredentialResourceApi.apiV1ClientControllerCredentialServiceDatabaseSafedepositAppIdGet()
    }
    // TODO Insert statement into the safedepositbox looks like this:
    /*
    INSERT INTO safedeposit."json"
    (uuid_json, blob_json)
    VALUES('88edd44a-b9e8-49fa-a34b-8493077ee9fb', '2');
    */
}
