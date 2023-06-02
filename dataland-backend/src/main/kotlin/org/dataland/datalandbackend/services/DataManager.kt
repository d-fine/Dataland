package org.dataland.datalandbackend.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.entities.DataMetaInformationEntity
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.model.StorableDataSet
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandinternalstorage.openApiClient.api.StorageControllerApi
import org.dataland.datalandinternalstorage.openApiClient.infrastructure.ClientException
import org.dataland.datalandinternalstorage.openApiClient.infrastructure.ServerException
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageHeaderKey
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
import org.dataland.datalandmessagequeueutils.exceptions.MessageQueueRejectException
import org.dataland.datalandmessagequeueutils.messages.QaCompletedMessage
import org.dataland.datalandmessagequeueutils.utils.MessageQueueUtils
import org.json.JSONObject
import org.slf4j.LoggerFactory
import org.springframework.amqp.rabbit.annotation.Argument
import org.springframework.amqp.rabbit.annotation.Exchange
import org.springframework.amqp.rabbit.annotation.Queue
import org.springframework.amqp.rabbit.annotation.QueueBinding
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.messaging.handler.annotation.Header
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.*

/**
 * Implementation of a data manager for Dataland including metadata storages
 * @param objectMapper object mapper used for converting data classes to strings and vice versa
 * @param companyManager service for managing company data
 * @param metaDataManager service for managing metadata
 * @param storageClient service for managing data
 * @param cloudEventMessageHandler service for managing CloudEvents messages
*/
@Component("DataManager")
class DataManager(
    @Autowired var objectMapper: ObjectMapper,
    @Autowired var companyManager: CompanyManager,
    @Autowired var metaDataManager: DataMetaInformationManager,
    @Autowired var storageClient: StorageControllerApi,
    @Autowired var cloudEventMessageHandler: CloudEventMessageHandler,
    @Autowired var messageUtils: MessageQueueUtils,
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val dataInMemoryStorage = mutableMapOf<String, String>()

    private fun assertActualAndExpectedDataTypeForIdMatch(
        dataId: String,
        dataType: DataType,
        correlationId: String,
    ) {
        val dataMetaInformation = metaDataManager.getDataMetaInformationByDataId(dataId)
        if (DataType.valueOf(dataMetaInformation.dataType) != dataType) {
            throw InvalidInputApiException(
                "Requested data $dataId not of type $dataType",
                "The data with the id: $dataId is registered as type" +
                    " ${dataMetaInformation.dataType} by Dataland instead of your requested" +
                    " type $dataType.",
            )
        }
        logger.info(
            "Requesting Data with ID $dataId and expected type $dataType from framework data storage. " +
                "Correlation ID: $correlationId",
        )
    }

    /**
     * Method to make the data manager add data to a data store, store metadata in Dataland and sending messages to the
     * relevant message queues
     * @param storableDataSet contains all the inputs needed by Dataland
     * @param bypassQa whether the data should be sent to QA or not
     * @return ID of the newly stored data in the data store
     */
    fun storeDataSetInMemoryAndSendReceptionMessageAndPersistMetaInfo(
        storableDataSet: StorableDataSet,
        bypassQa: Boolean,
        correlationId: String,
    ):
        String {
        val dataId = generateRandomDataId()
        storeMetaDataFrom(dataId, storableDataSet, correlationId)
        storeDataSetInTemporaryStoreAndSendMessage(dataId, storableDataSet, bypassQa, correlationId)
        return dataId
    }

    /**
     * Persists the data meta-information to the database ensuring that the database transaction
     * ends directly after this function returns so that a MQ-Message might be sent out after this function completes
     * @param dataId The dataId of the dataset to store
     * @param storableDataSet the dataset to store
     * @param correlationId the correlation id of the insertion process
     */
    @Transactional(propagation = Propagation.NEVER)
    fun storeMetaDataFrom(dataId: String, storableDataSet: StorableDataSet, correlationId: String) {
        val company = companyManager.getCompanyById(storableDataSet.companyId)
        logger.info(
            "Sending StorableDataSet of type ${storableDataSet.dataType} for company ID " +
                "'${storableDataSet.companyId}', Company Name ${company.companyName} to storage Interface. " +
                "Correlation ID: $correlationId",
        )

        val metaData = DataMetaInformationEntity(
            dataId,
            company,
            storableDataSet.dataType.toString(),
            storableDataSet.uploaderUserId,
            storableDataSet.uploadTime,
            storableDataSet.reportingPeriod,
            null,
            QaStatus.Pending,
        )
        metaDataManager.storeDataMetaInformation(metaData)
    }

    /**
     * Method that listens to the qa_queue and updates the metadata information after successful qa process
     * @param jsonString the message describing the result of the completed QA process
     * @param correlationId the correlation ID of the current user process
     * @param type the type of the message
     */
    @RabbitListener(
        bindings = [
            QueueBinding(
                value = Queue(
                    "dataQualityAssuredBackendDataManager",
                    arguments = [
                        Argument(name = "x-dead-letter-exchange", value = ExchangeName.DeadLetter),
                        Argument(name = "x-dead-letter-routing-key", value = "deadLetterKey"),
                        Argument(name = "defaultRequeueRejected", value = "false"),
                    ],
                ),
                exchange = Exchange(ExchangeName.DataQualityAssured, declare = "false"),
                key = [RoutingKeyNames.data],
            ),
        ],
    )
    @Transactional
    fun updateMetaData(
        @Payload jsonString: String,
        @Header(MessageHeaderKey.CorrelationId) correlationId: String,
        @Header(MessageHeaderKey.Type) type: String,
    ) {
        messageUtils.validateMessageType(type, MessageType.QaCompleted)
        val qaCompletedMessage = objectMapper.readValue(jsonString, QaCompletedMessage::class.java)
        val dataId = qaCompletedMessage.identifier
        if (dataId.isEmpty()) {
            throw MessageQueueRejectException("Provided data ID is empty")
        }
        messageUtils.rejectMessageOnException {
            val metaInformation = metaDataManager.getDataMetaInformationByDataId(dataId)
            metaInformation.qaStatus = qaCompletedMessage.validationResult
            if(qaCompletedMessage.validationResult == QaStatus.Accepted) {
                metaDataManager.setActiveDataset(metaInformation)
            }
            logger.info(
                "Received quality assurance: ${qaCompletedMessage.validationResult} for data upload with DataId: " +
                    "$dataId with Correlation Id: $correlationId",
            )
        }
    }

    /**
     * This method retrieves data from the temporary storage
     * @param dataId is the identifier for which all stored data entries in the temporary storage are filtered
     * @return stringified data entry from the temporary store
     */
    fun selectDataSetFromTemporaryStorage(dataId: String): String {
        val rawValue = dataInMemoryStorage.getOrElse(dataId) {
            throw ResourceNotFoundApiException(
                "Data ID not found in temporary storage",
                "Dataland does not know the data id $dataId",
            )
        }
        return objectMapper.writeValueAsString(rawValue)
    }

    /**
     * Method to temporarily store a data set in a hash map and send a message to the storage_queue
     * @param dataId The id of the inserted data set
     * @param storableDataSet The data set to store
     * @param bypassQa Whether the data set should be sent to QA or not
     * @param correlationId The correlation id of the request initiating the storing of data
     * @return ID of the stored data set
     */
    fun storeDataSetInTemporaryStoreAndSendMessage(
        dataId: String,
        storableDataSet: StorableDataSet,
        bypassQa: Boolean,
        correlationId: String,
    ) {
        dataInMemoryStorage[dataId] = objectMapper.writeValueAsString(storableDataSet)
        val payload = JSONObject(mapOf("dataId" to dataId, "bypassQa" to bypassQa)).toString()
        cloudEventMessageHandler.buildCEMessageAndSendToQueue(
            payload, MessageType.DataReceived, correlationId,
            ExchangeName.DataReceived,
        )
        logger.info(
            "Stored StorableDataSet of type '${storableDataSet.dataType}' " +
                "for company ID '${storableDataSet.companyId}' in temporary storage. " +
                "Data ID '$dataId'. Correlation ID: '$correlationId'.",
        )
    }

    /**
     * Method to generate a random Data ID
     * @return generated UUID
     */
    fun generateRandomDataId(): String {
        return "${UUID.randomUUID()}"
    }

    /**
     * Method that listens to the stored queue and removes data entries from the temporary storage once they have been
     * stored in the persisted database. Further it logs success notification associated containing dataId and
     * correlationId
     * @param dataId the ID of the dataset to that was stored
     * @param correlationId the correlation ID of the current user process
     * @param type the type of the message
     */
    @RabbitListener(
        bindings = [
            QueueBinding(
                value = Queue(
                    "dataStoredBackendDataManager",
                    arguments = [
                        Argument(name = "x-dead-letter-exchange", value = ExchangeName.DeadLetter),
                        Argument(name = "x-dead-letter-routing-key", value = "deadLetterKey"),
                        Argument(name = "defaultRequeueRejected", value = "false"),
                    ],
                ),
                exchange = Exchange(ExchangeName.ItemStored, declare = "false"),
                key = [RoutingKeyNames.data],
            ),
        ],
    )
    fun removeStoredItemFromTemporaryStore(
        @Payload dataId: String,
        @Header(MessageHeaderKey.CorrelationId) correlationId: String,
        @Header(MessageHeaderKey.Type) type: String,
    ) {
        messageUtils.validateMessageType(type, MessageType.DataStored)
        if (dataId.isEmpty()) {
            throw MessageQueueRejectException("Provided data ID is empty")
        }
        logger.info("Internal Storage sent a message - job done")
        logger.info(
            "Dataset with dataId $dataId was successfully stored. Correlation ID: $correlationId.",
        )
        messageUtils.rejectMessageOnException {
            dataInMemoryStorage.remove(dataId)
        }
    }

    /**
     * Method to make the data manager get the data of a single entry from the data store
     * @param dataId to identify the stored data
     * @param dataType to check the correctness of the type of the retrieved data
     * @param correlationId to use in combination with dataId to retrieve data and assert type
     * @return data set associated with the data ID provided in the input
     */
    fun getDataSet(dataId: String, dataType: DataType, correlationId: String): StorableDataSet {
        assertActualAndExpectedDataTypeForIdMatch(dataId, dataType, correlationId)
        val dataMetaInformation = metaDataManager.getDataMetaInformationByDataId(dataId)
        lateinit var dataAsString: String
        try {
            dataAsString = getDataFromCacheOrStorageService(dataId, correlationId)
        } catch (e: ClientException) {
            handleInternalStorageClientException(e, dataId, correlationId)
        }
        logger.info("Received Dataset of length ${dataAsString.length}. Correlation ID: $correlationId")
        val dataAsStorableDataSet = objectMapper.readValue(dataAsString, StorableDataSet::class.java)
        dataAsStorableDataSet.requireConsistencyWith(dataMetaInformation)
        return dataAsStorableDataSet
    }

    private fun handleInternalStorageClientException(e: ClientException, dataId: String, correlationId: String) {
        if (e.statusCode == HttpStatus.NOT_FOUND.value()) {
            logger.info("Dataset with id $dataId could not be found. Correlation ID: $correlationId")
            throw ResourceNotFoundApiException(
                "Dataset not found",
                "No dataset with the id: $dataId could be found in the data store.",
                e,
            )
        } else {
            throw e
        }
    }

    private fun getDataFromCacheOrStorageService(dataId: String, correlationId: String): String {
        return dataInMemoryStorage[dataId] ?: getDataFromStorageService(dataId, correlationId)
    }

    private fun getDataFromStorageService(dataId: String, correlationId: String): String {
        val dataAsString: String
        logger.info("Retrieve data from internal storage. Correlation ID: $correlationId")
        try {
            dataAsString = storageClient.selectDataById(dataId, correlationId)
        } catch (e: ServerException) {
            logger.error(
                "Error requesting data. Received ServerException with Message:" +
                    " ${e.message}. Correlation ID: $correlationId",
            )
            throw e
        }
        return dataAsString
    }

    /**
     * Method to check if a data set belongs to a teaser company and hence is publicly available
     * @param dataId the ID of the data set to be checked
     * @return a boolean signalling if the data is public or not
     */
    fun isDataSetPublic(dataId: String): Boolean {
        val associatedCompanyId = metaDataManager.getDataMetaInformationByDataId(dataId).company.companyId
        return companyManager.isCompanyPublic(associatedCompanyId)
    }
}
