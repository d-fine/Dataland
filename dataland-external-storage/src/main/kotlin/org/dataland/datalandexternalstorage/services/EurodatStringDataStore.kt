package org.dataland.datalandexternalstorage.services

import DatabaseConnection.executeMySQLQuery
import DatabaseConnection.getConnection
import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.openApiClient.api.TemporarilyCachedDataControllerApi
import org.dataland.datalandeurodatclient.openApiClient.api.DatabaseCredentialResourceApi
import org.dataland.datalandeurodatclient.openApiClient.model.Credentials
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
// TODO Rename service at the end
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
    @Autowired var databaseCredentialResourceClient: DatabaseCredentialResourceApi,
    @Autowired var objectMapper: ObjectMapper,
    @Autowired var messageUtils: MessageQueueUtils,
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val eurodatAppName = "minaboApp"

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
    fun processStorageRequest(
        @Payload payload: String,
        @Header(MessageHeaderKey.CorrelationId) correlationId: String,
        @Header(MessageHeaderKey.Type) type: String,
    ) {
        messageUtils.validateMessageType(type, MessageType.PrivateDataReceived)
        val dataId = JSONObject(payload).getString("dataId")
        if (dataId.isEmpty()) {
            throw MessageQueueRejectException("Provided data ID is empty.")
        }
        logger.info(
            "Received storage request for dataId $dataId and correlationId $correlationId with payload: $payload",
        )
        messageUtils.rejectMessageOnException {
            val actionType = JSONObject(payload).getString("actionType")
            if (actionType == ActionType.StorePrivateDataAndDocuments) {
                storeDataInEurodat(dataId, correlationId, payload)
                sendMessageAfterSuccessfulStorage(payload, correlationId)
            }
        }
    }

    /**
     * Method that triggers the storage processes of the JSON and the associated documents in EuroDaT
     * @param payload the content of the message
     * @param correlationId of the current storage process
     * @param dataId the Dataland dataId of the dataset to be stored
     */
    fun storeDataInEurodat(dataId: String, correlationId: String, payload: String) {
        logger.info("Starting storage process for dataId $dataId and correlationId $correlationId")
        val eurodatCredentials = databaseCredentialResourceClient.apiV1ClientControllerCredentialServiceDatabaseSafedepositAppIdGet(eurodatAppName)
        logger.info("EuroDaT credentials received")
        val jsonToStore = temporarilyCachedDataClient.getReceivedPrivateJson(dataId)
        // TODO renamed to getReceivedPrivateJson
        storeJsonInEurodat(correlationId, DataItem(dataId, jsonToStore), eurodatCredentials)

        val documentHashesOfDocumentsToStore = JSONObject(payload).getJSONArray("documentHashes")
        documentHashesOfDocumentsToStore.forEach { hashAsArrayElement ->
            val hash = hashAsArrayElement as String
            storeBlobInEurodat(dataId, correlationId, hash)
        }
    }

    /**
     * Stores a Data Item in EuroDaT while ensuring that there is no active transaction.
     * This will guarantee that the write is commited after exit of this method.
     * @param dataItem the DataItem to be stored
     */
    @Transactional(propagation = Propagation.NEVER)
    fun storeJsonInEurodat(correlationId: String, dataItem: DataItem, eurodatCredentials: Credentials) {
        logger.info("Storing JSON in EuroDaT for dataId ${dataItem.id} and correlationId $correlationId")
        // TODO remove logger
        logger.info(eurodatCredentials.toString())
        val insertStatement = "INSERT INTO safedeposit.json (uuid_json, blob_json) VALUES(?, ?::jsonb)"
        val conn = getConnection(eurodatCredentials.username, eurodatCredentials.password, eurodatCredentials.jdbcUrl)
        executeMySQLQuery(conn, insertStatement, dataItem.id, dataItem.data)
    }

    /**
     * Stores a Blob Item in EuroDaT while ensuring that there is no active transaction.
     * This will guarantee that the write is commited after exit of this method.
     * @param dataItem the DataItem to be stored
     */
    @Transactional(propagation = Propagation.NEVER)
    fun storeBlobInEurodat(dataId: String, correlationId: String, hash: String) {
        logger.info("Storing document with hash $hash in EuroDaT for dataId $dataId and correlationId $correlationId")
        val resource = temporarilyCachedDocumentClient.getReceivedPrivateDocument(hash)
        val resultByteArray = resource.readBytes()
        // val eurodatCredentials = databaseCredentialResourceClient.apiV1ClientControllerCredentialServiceDatabaseSafedepositAppIdGet(eurodatAppName)
        // val insertStatement =  "INSERT INTO safedeposit.json (uuid_json, blob_pdf) VALUES(?, ?)"
        // val conn = getConnection(eurodatCredentials.username, eurodatCredentials.password, eurodatCredentials.jdbcUrl)
        //  executeMySQLQuery(conn, insertStatement, hash, resultByteArray)
    }
    // TODO call to eurodat
    /**
     * Sends a message to the queue to inform other services that the storage to EuroDaT has been successful.
     * @param payload contains meta info about the stored assets (dataId and hashes)
     * @param correlationId makes it possible to match the message to one specific storage process/thread
     */
    fun sendMessageAfterSuccessfulStorage(payload: String, correlationId: String) {
        cloudEventMessageHandler.buildCEMessageAndSendToQueue(
            payload, MessageType.PrivateDataStored, correlationId, ExchangeName.PrivateItemStored, RoutingKeyNames.data,
        )
    }
}

// TODO Insert statement into the safedepositbox looks like this:
    /*
    INSERT INTO safedeposit."json"
    (uuid_json, blob_json)
    VALUES('88edd44a-b9e8-49fa-a34b-8493077ee9fb', '2');
    */
