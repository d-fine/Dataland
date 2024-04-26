package org.dataland.datalandexternalstorage.services

import DatabaseConnection.getConnection
import DatabaseConnection.insertByteArrayIntoSqlDatabase
import DatabaseConnection.insertDataIntoSqlDatabase
import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.annotation.PostConstruct
import org.dataland.datalandbackend.openApiClient.api.TemporarilyCachedDataControllerApi
import org.dataland.datalandeurodatclient.openApiClient.api.DatabaseCredentialResourceApi
import org.dataland.datalandeurodatclient.openApiClient.api.SafeDepositDatabaseResourceApi
import org.dataland.datalandeurodatclient.openApiClient.model.Credentials
import org.dataland.datalandeurodatclient.openApiClient.model.SafeDepositDatabaseRequest
import org.dataland.datalandeurodatclient.openApiClient.model.SafeDepositDatabaseResponse
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
import org.springframework.beans.factory.annotation.Value
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.sql.SQLException

/**
 * Simple implementation of a data storing service using the EuroDaT data trustee
 * @param cloudEventMessageHandler service for managing CloudEvents messages
 * @param temporarilyCachedDataClient the service for retrieving data from the temporary storage
 * @param temporarilyCachedDocumentClient the service for retrieven documents from the temporary storage
 * @param objectMapper object mapper used for converting data classes to strings and vice versa
 * @param databaseCredentialResourceClient the service to retrieve eurodat storage credentials
 * @param safeDepositDatabaseResourceClient the service to create the safe deposit box used to store private data
 * on eurodat
 * @param messageUtils contains utils connected to the messages on the message queue
 */
@Suppress("LongParameterList")
@Component
class EurodatDataStore(
    @Autowired var cloudEventMessageHandler: CloudEventMessageHandler,
    @Autowired var temporarilyCachedDataClient: TemporarilyCachedDataControllerApi,
    @Autowired var temporarilyCachedDocumentClient: StreamingTemporarilyCachedPrivateDocumentControllerApi,
    @Autowired var databaseCredentialResourceClient: DatabaseCredentialResourceApi,
    @Autowired var safeDepositDatabaseResourceClient: SafeDepositDatabaseResourceApi,
    @Autowired var objectMapper: ObjectMapper,
    @Autowired var messageUtils: MessageQueueUtils,
    @Value("\${dataland.eurodatclient.app-name}")
    private val eurodatAppName: String,
    @Value("\${dataland.eurodatclient.max-retries-connecting}")
    private val maxRetriesConnectingToEurodat: Int,
    @Value("\${dataland.eurodatclient.milliseconds-between-retries}")
    private val millisecondsBetweenRetriesConnectingToEurodat: Int,
    @Value("\${dataland.eurodatclient.initialize-safe-deposit-box}")
    private val initializeSafeDepositBox: Boolean,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Tries to create a safe deposit box in EuroDaT for storage of Dataland data a pre-defined number of times and
     * then throws a final exception after the retries are used up.
     */
    @PostConstruct
    fun createSafeDepositBox() {
        if (initializeSafeDepositBox) {
            logger.info("Checking if safe deposit box exits. If not creating safe deposit box")
            retryWrapperMethod("createSafeDepositBox") {
                isSafeDepositBoxAvailable()
            }
        }
    }

    /**
     * This method will rerun a given method if an exception is thrown while running it
     * @param inputMethod to specify in the logs which method should be rerun
     */
    @Suppress("TooGenericExceptionCaught")
    fun <T> retryWrapperMethod(inputMethod: String, block: () -> T): T {
        var retryCount = 0
        while (retryCount <= maxRetriesConnectingToEurodat) {
            try {
                logger.info("Trying to run the method $inputMethod. Try number ${retryCount + 1}.")
                return block()
            } catch (e: Exception) {
                logger.error("An error occurred while executing the method $inputMethod: ${e.message}. Trying again")
                if (retryCount == maxRetriesConnectingToEurodat) {
                    logger.error(
                        "An error occurred while executing the method $inputMethod: ${e.message}. " +
                            "Process terminated",
                    )
                    throw e
                }
            }
            retryCount++
            Thread.sleep(millisecondsBetweenRetriesConnectingToEurodat.toLong())
        }
        return block()
    }

    // TODO check the if condition for the first time a deposit box was created
    @Suppress("TooGenericExceptionThrown")
    private fun isSafeDepositBoxAvailable() {
        if (postSafeDepositBoxCreationRequest().response.contains("Database already exists")) {
            logger.info("Safe deposit box exists.")
        } else {
            throw Exception("Service not there.")
        }
    }

    /**
     * Sends a POST request to the safe deposit box creation endpoint of the EuroDaT client.
     */
    fun postSafeDepositBoxCreationRequest(): SafeDepositDatabaseResponse {
        val creationRequest = SafeDepositDatabaseRequest(eurodatAppName)
        return safeDepositDatabaseResourceClient.apiV1ClientControllerDatabaseServicePost(creationRequest)
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
                try {
                    storeDataInEurodat(dataId, correlationId, payload)
                    sendMessageAfterSuccessfulStorage(payload, correlationId)
                } catch (ex: SQLException) {
                    logger.error("A sql exception was thronw: $ex")
                }
            }
        }
    }

    /**
     * Method that triggers the storage processes of the JSON and the associated documents in EuroDaT
     * @param dataId the Dataland dataId of the dataset to be stored
     * @param correlationId of the current storage process
     * @param payload the content of the message
     */
    fun storeDataInEurodat(dataId: String, correlationId: String, payload: String) {
        logger.info("Starting storage process for dataId $dataId and correlationId $correlationId")
        val eurodatCredentials = retryWrapperMethod("getEurodatCredentials") {
            databaseCredentialResourceClient
                .apiV1ClientControllerCredentialServiceDatabaseSafedepositAppIdGet(eurodatAppName)
        }
        logger.info("EuroDaT credentials received")
        val jsonToStore = temporarilyCachedDataClient.getReceivedPrivateJson(dataId)
        logger.info("Data from temporary storage retrieved.")
        retryWrapperMethod("storeJsonInEurodat") {
            storeJsonInEurodat(correlationId, DataItem(dataId, jsonToStore), eurodatCredentials)
        }
        logger.info("Data stored in eurodat storage.")
        val documentHashesOfDocumentsToStore = JSONObject(payload).getJSONObject("documentHashes")
        documentHashesOfDocumentsToStore.keys().forEach { hashAsArrayElement ->
            val documentId = documentHashesOfDocumentsToStore[hashAsArrayElement] as String
            retryWrapperMethod("storeBlobInEurodat") {
                storeBlobInEurodat(dataId, correlationId, hashAsArrayElement, documentId, eurodatCredentials)
            }
        }
        logger.info("Documents stored in eurodat storage.")
    }

    /**
     * Stores a Data Item in EuroDaT while ensuring that there is no active transaction.
     * This will guarantee that the write is commited after exit of this method.
     * @param correlationId the correlationId of the storage request
     * @param dataItem the DataItem to be stored
     * @param eurodatCredentials the credentials to log into the eurodat storage
     */
    @Suppress("TooGenericExceptionThrown")
    @Transactional(propagation = Propagation.NEVER)
    fun storeJsonInEurodat(correlationId: String, dataItem: DataItem, eurodatCredentials: Credentials) {
        logger.info("Storing JSON in EuroDaT for dataId ${dataItem.id} and correlationId $correlationId")
        val insertStatement = "INSERT INTO safedeposit.json (uuid_json, blob_json) VALUES(?, ?::jsonb)"
        val conn = getConnection(eurodatCredentials.username, eurodatCredentials.password, eurodatCredentials.jdbcUrl)
        val sqlReturn = insertDataIntoSqlDatabase(conn, insertStatement, dataItem.id, dataItem.data)
        if (!sqlReturn) {
            throw Exception("An error occured while storing dataId ${dataItem.id} with correlationId $correlationId")
        }
    }

    /**
     * Stores a Blob Item in EuroDaT while ensuring that there is no active transaction.
     * This will guarantee that the write is commited after exit of this method.
     * @param dataId the DataId connected to the document which should be stored
     * @param correlationId the correlationId of the storage request
     * @param hash the hash of the document to be stored
     * @param documentId the documentId in the UUID format of the document to be stored
     * @param eurodatCredentials the credentials to log into the eurodat storage
     */
    @Suppress("TooGenericExceptionThrown")
    @Transactional(propagation = Propagation.NEVER)
    fun storeBlobInEurodat(
        dataId: String,
        correlationId: String,
        hash: String,
        documentId: String,
        eurodatCredentials: Credentials,
    ) {
        logger.info(
            "Storing document with hash $hash and documentId $documentId in EuroDaT for dataId $dataId and" +
                " correlationId $correlationId",
        )
        val resource = temporarilyCachedDocumentClient.getReceivedPrivateDocument(hash)
        val resultByteArray = resource.readBytes()
        val insertStatement = "INSERT INTO safedeposit.pdf (uuid_pdf, blob_pdf) VALUES(?, ?)"
        val conn = getConnection(eurodatCredentials.username, eurodatCredentials.password, eurodatCredentials.jdbcUrl)
        val sqlReturn = insertByteArrayIntoSqlDatabase(conn, insertStatement, documentId, resultByteArray)
        if (!sqlReturn) {
            throw Exception(
                "An error occured while storing document hash $hash, documentId $documentId and " +
                    "correlationId $correlationId",
            )
        }
    }

    /**
     * Sends a message to the queue to inform other services that the storage to EuroDaT has been successful.
     * @param payload contains meta info about the stored assets (dataId and hashes)
     * @param correlationId makes it possible to match the message to one specific storage process/thread
     */
    fun sendMessageAfterSuccessfulStorage(payload: String, correlationId: String) {
        logger.info("Storing completed. Sending message that storing assignment is done.")
        cloudEventMessageHandler.buildCEMessageAndSendToQueue(
            payload, MessageType.PrivateDataStored, correlationId, ExchangeName.PrivateItemStored, RoutingKeyNames.data,
        )
    }
}
