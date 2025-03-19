package org.dataland.datalandexternalstorage.services

import org.dataland.datalandbackend.openApiClient.api.TemporarilyCachedDataControllerApi
import org.dataland.datalandeurodatclient.openApiClient.api.DatabaseCredentialResourceApi
import org.dataland.datalandeurodatclient.openApiClient.model.Credentials
import org.dataland.datalandexternalstorage.utils.DatabaseConnection.getConnection
import org.dataland.datalandexternalstorage.utils.DatabaseConnection.insertByteArrayIntoSqlDatabase
import org.dataland.datalandexternalstorage.utils.DatabaseConnection.insertDataIntoSqlDatabase
import org.dataland.datalandexternalstorage.utils.DatabaseConnection.selectDocumentFromSqlDatabase
import org.dataland.datalandexternalstorage.utils.DatabaseConnection.selectJsonStringFromSqlDatabase
import org.dataland.datalandexternalstorage.utils.EurodatDataStoreUtils.retryWrapperMethod
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
import org.json.JSONObject
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.sql.SQLException

/**
 * Simple implementation of a data storing service using the EuroDaT data trustee
 * @param cloudEventMessageHandler service for managing CloudEvents messages
 * @param temporarilyCachedDataClient the service for retrieving data from the temporary storage
 * @param temporarilyCachedDocumentClient the service for retrieven documents from the temporary storage
 * @param databaseCredentialResourceClient the service to retrieve eurodat storage credentials
 * on eurodat
 */
@Component
class EurodatDataStore(
    @Autowired var cloudEventMessageHandler: CloudEventMessageHandler,
    @Autowired var temporarilyCachedDataClient: TemporarilyCachedDataControllerApi,
    @Autowired var temporarilyCachedDocumentClient: StreamingTemporarilyCachedPrivateDocumentControllerApi,
    @Autowired var databaseCredentialResourceClient: DatabaseCredentialResourceApi,
    @Value("\${dataland.eurodatclient.app-name}")
    private val eurodatAppName: String,
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val loggingMessageGetEurodatConnection = "get JDBC connection details from EuroDaT"

    /**
     * Method that triggers the storage processes of the JSON and the associated documents in EuroDaT
     * @param dataId the Dataland dataId of the dataset to be stored
     * @param correlationId of the current storage process
     * @param payload the content of the message
     */
    fun storeDataInEurodat(
        dataId: String,
        correlationId: String,
        payload: String,
    ) {
        logger.info("Starting storage process for dataId $dataId and correlationId $correlationId")
        val eurodatCredentials =
            retryWrapperMethod(loggingMessageGetEurodatConnection) {
                databaseCredentialResourceClient
                    .apiV1ClientControllerCredentialServiceDatabaseSafedepositAppIdGet(eurodatAppName)
            }
        logger.info("EuroDaT credentials received")
        val jsonToStore = temporarilyCachedDataClient.getReceivedPrivateJson(dataId)
        logger.info("Data from temporary storage retrieved.")
        retryWrapperMethod("write data into EuroDaT database") {
            storeJsonInEurodat(correlationId, dataId, jsonToStore, eurodatCredentials)
        }
        logger.info("Data with $dataId stored in eurodat storage. CorrelationId: $correlationId")
        val documentHashesOfDocumentsToStore = JSONObject(payload).getJSONObject("documentHashes")
        documentHashesOfDocumentsToStore.keys().forEach { hashAsArrayElement ->
            val eurodatId = documentHashesOfDocumentsToStore.getString(hashAsArrayElement)
            retryWrapperMethod("write blob into EuroDaT database") {
                storeBlobInEurodat(dataId, correlationId, hashAsArrayElement, eurodatId, eurodatCredentials)
            }
            logger.info(
                "Document with hash: $hashAsArrayElement, eurodatId: $eurodatId for dataId: $dataId was " +
                    "stored in eurodat storage. CorrelationId: $correlationId",
            )
        }
    }

    /**
     * Stores a Data Item in EuroDaT while ensuring that there is no active transaction.
     * This will guarantee that the write is committed after exit of this method.
     * @param correlationId the correlationId of the storage request
     * @param dataId the dataId of the data to be stored
     * @param data the data to be stored
     * @param eurodatCredentials the credentials to log into the eurodat storage
     */
    @Transactional(propagation = Propagation.NEVER)
    fun storeJsonInEurodat(
        correlationId: String,
        dataId: String,
        data: String,
        eurodatCredentials: Credentials,
    ) {
        logger.info("Storing JSON in EuroDaT for dataId $dataId and correlationId $correlationId")
        val insertStatement = "INSERT INTO safedeposit.json (uuid_json, blob_json) VALUES(?, ?::jsonb)"
        val conn = getConnection(eurodatCredentials.username, eurodatCredentials.password, eurodatCredentials.jdbcUrl)
        val sqlReturn = insertDataIntoSqlDatabase(conn, insertStatement, dataId, data)
        if (!sqlReturn) {
            throw SQLException("An error occured while storing dataId $dataId with correlationId $correlationId")
        }
    }

    /**
     * Stores a Blob Item in EuroDaT while ensuring that there is no active transaction.
     * This will guarantee that the write is commited after exit of this method.
     * @param dataId the DataId connected to the document which should be stored
     * @param correlationId the correlationId of the storage request
     * @param hash the hash of the document to be stored
     * @param eurodatId the eurodatId in the UUID format of the document to be stored
     * @param eurodatCredentials the credentials to log into the eurodat storage
     */
    @Transactional(propagation = Propagation.NEVER)
    fun storeBlobInEurodat(
        dataId: String,
        correlationId: String,
        hash: String,
        eurodatId: String,
        eurodatCredentials: Credentials,
    ) {
        logger.info(
            "Storing document with hash $hash and eurodatId $eurodatId in EuroDaT for dataId $dataId and" +
                " correlationId $correlationId",
        )
        val resource = temporarilyCachedDocumentClient.getReceivedPrivateDocument(hash)
        val resultByteArray = resource.readBytes()
        val insertStatement = "INSERT INTO safedeposit.pdf (uuid_pdf, blob_pdf) VALUES(?, ?)"
        val conn = getConnection(eurodatCredentials.username, eurodatCredentials.password, eurodatCredentials.jdbcUrl)
        val sqlReturn = insertByteArrayIntoSqlDatabase(conn, insertStatement, eurodatId, resultByteArray)
        if (!sqlReturn) {
            throw SQLException(
                "An error occured while storing document hash $hash, eurodatId $eurodatId and " +
                    "correlationId $correlationId",
            )
        }
    }

    /**
     * Sends a message to the queue to inform other services that the storage to EuroDaT has been successful.
     * @param payload contains meta info about the stored assets (dataId and hashes)
     * @param correlationId makes it possible to match the message to one specific storage process/thread
     */
    fun sendMessageAfterSuccessfulStorage(
        payload: String,
        correlationId: String,
    ) {
        logger.info("Storing completed. Sending message that storing assignment is done.")
        cloudEventMessageHandler.buildCEMessageAndSendToQueue(
            payload, MessageType.PRIVATE_DATA_STORED, correlationId, ExchangeName.PRIVATE_ITEM_STORED, RoutingKeyNames.DATA,
        )
    }

    /**
     * Select a data object from the eurodat storage by its dataId
     */
    fun selectPrivateDataSet(
        dataId: String,
        correlationId: String,
    ): String {
        logger.info("Select data for data $dataId from eurodat storage.CorrelationId $correlationId")
        val eurodatCredentials =
            retryWrapperMethod(loggingMessageGetEurodatConnection) {
                databaseCredentialResourceClient
                    .apiV1ClientControllerCredentialServiceDatabaseSafedepositAppIdGet(eurodatAppName)
            }
        val conn = getConnection(eurodatCredentials.username, eurodatCredentials.password, eurodatCredentials.jdbcUrl)
        val sqlStatement = "SELECT * FROM safedeposit.json WHERE uuid_json = '$dataId'"
        return selectJsonStringFromSqlDatabase(conn, sqlStatement, dataId)
    }

    /**
     * Select a blob object from the eurodat storage by its eurodatId
     */
    fun selectPrivateDocument(
        eurodatId: String,
        correlationId: String,
    ): ByteArray {
        logger.info("Select document for eurodatId $eurodatId from eurodat storage. CorrelationId $correlationId")
        val eurodatCredentials =
            retryWrapperMethod(loggingMessageGetEurodatConnection) {
                databaseCredentialResourceClient
                    .apiV1ClientControllerCredentialServiceDatabaseSafedepositAppIdGet(eurodatAppName)
            }
        val conn = getConnection(eurodatCredentials.username, eurodatCredentials.password, eurodatCredentials.jdbcUrl)
        val sqlStatement = "SELECT * FROM safedeposit.pdf WHERE uuid_pdf = '$eurodatId'"
        return selectDocumentFromSqlDatabase(conn, sqlStatement, eurodatId)
    }
}
