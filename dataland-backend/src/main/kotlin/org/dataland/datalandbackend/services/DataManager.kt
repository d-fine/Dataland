package org.dataland.datalandbackend.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.entities.DataMetaInformationEntity
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.model.StorableDataSet
import org.dataland.datalandbackend.model.enums.data.QAStatus
import org.dataland.datalandbackendutils.exceptions.InternalServerErrorApiException
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandinternalstorage.openApiClient.api.StorageControllerApi
import org.dataland.datalandinternalstorage.openApiClient.infrastructure.ServerException
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.slf4j.LoggerFactory
import org.springframework.amqp.AmqpException
import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.annotation.RabbitListener
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.ComponentScan
import org.springframework.stereotype.Component
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
@ComponentScan(basePackages = ["org.dataland"])
@Component("DataManager")
class DataManager(
    @Autowired var objectMapper: ObjectMapper,
    @Autowired var companyManager: CompanyManager,
    @Autowired var metaDataManager: DataMetaInformationManager,
    @Autowired var storageClient: StorageControllerApi,
    @Autowired var cloudEventMessageHandler: CloudEventMessageHandler,
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val dataInformationHashMap = mutableMapOf<String, String>()

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
     * Method to make the data manager add data to a data store and store meta data in Dataland
     * @param storableDataSet contains all the inputs needed by Dataland
     * @return ID of the newly stored data in the data store
     */
    @Transactional
    fun addDataSet(storableDataSet: StorableDataSet, correlationId: String): String {
        val company = companyManager.getCompanyById(storableDataSet.companyId)
        logger.info(
            "Sending StorableDataSet of type ${storableDataSet.dataType} for company ID " +
                "${storableDataSet.companyId}, Company Name ${company.companyName} to storage Interface. " +
                "Correlation ID: $correlationId",
        )
        val dataId: String = storeDataSet(storableDataSet, company.companyName, correlationId)
        val updatedMetaData = DataMetaInformationEntity(
            dataId, storableDataSet.dataType.toString(),
            storableDataSet.uploaderUserId, storableDataSet.uploadTime, company, QAStatus.Pending,
        )
        metaDataManager.storeDataMetaInformation(updatedMetaData)
        sendMessageToQueue(dataId, "New data - QA necessary", correlationId, "upload_queue")
        return dataId
    }

    private fun sendMessageToQueue(dataId: String, type: String, correlationId: String, messageQueue: String) {
        try {
            cloudEventMessageHandler.buildCEMessageAndSendToQueue(
                dataId, type, correlationId,
                messageQueue,
            )
        } catch (exception: AmqpException) {
            val internalMessage = "Error sending message to $messageQueue." +
                " Received AmqpException with message: ${exception.message}. Correlation ID: $correlationId."
            logger.error(internalMessage)
            throw AmqpException(internalMessage, exception)
        }
    }

    /**
     * Method that listens to the qa_queue and updates the metadata information after successful qa process
     * @param message is the message delivered on the message queue
     */
    @RabbitListener(queues = ["qa_queue"])
    fun updateMetaDataAfterQA(message: Message) {
        val dataId = cloudEventMessageHandler.bodyToString(message)
        val correlationId = message.messageProperties.headers["cloudEvents:id"].toString()
        if (!dataId.isNullOrEmpty()) {
            val metaInformation = metaDataManager.getDataMetaInformationByDataId(dataId)
            metaDataManager.storeDataMetaInformation(metaInformation.copy(qaStatus = QAStatus.Accepted))
            logger.info(
                "Received quality assurance for data upload with DataId: $dataId with Correlation Id: $correlationId",
            )
        } else {
            val internalMessage = "Error updating metadata data. Correlation ID: $correlationId"
            logger.error(internalMessage)
            throw InternalServerErrorApiException(
                "Update of meta data failed", "The update of the metadataset failed",
                internalMessage,
            )
        }
    }

    /**
     * This method retrieves data from the temporal storage
     * @param dataId is the identifier for which all stored data entries in the temporary storage are filtered
     */
    fun selectDataSetForInternalStorage(dataId: String): String {
        val rawValue = dataInformationHashMap.getOrElse(dataId) {
            throw ResourceNotFoundApiException(
                "Non-persisted data ID not found",
                "Dataland does not know the non-persisted data id $dataId",
            )
        }
        return objectMapper.writeValueAsString(rawValue)
    }

    /**
     * Method to temporarily store a data set via the hash map and send a notification to the storage_queue
     * @param storableDataSet The data set to store
     * @param companyName The name of the company corresponding to the data set to store
     * @param correlationId
     */
    fun storeDataSet(
        storableDataSet: StorableDataSet,
        companyName: String,
        correlationId: String,
    ): String {
        val dataId = generateRandomDataId()
        dataInformationHashMap[dataId] = objectMapper.writeValueAsString(storableDataSet)
        sendMessageToQueue(dataId, "Data to be stored", correlationId, "storage_queue")
        logger.info(
            "Stored StorableDataSet of type ${storableDataSet.dataType} for company ID in temporary store" +
                "${storableDataSet.companyId}. Company Name $companyName received ID $dataId from storage. " +
                "Correlation ID: $correlationId.",
        )
        return(dataId)
    }

    /**
     * Method to generate a random Data ID
     */
    fun generateRandomDataId(): String {
        return "${UUID.randomUUID()}:${UUID.randomUUID()}_${UUID.randomUUID()}"
    }

    /**
     * Method to log success notification associated to certain dataId and correlationId
     * @param message Message retrieved from stored_queue
     */
    @RabbitListener(queues = ["stored_queue"])
    fun loggingOfStoredDataSet(message: Message) {
        val dataId = cloudEventMessageHandler.bodyToString(message)
        val correlationId = message.messageProperties.headers["cloudEvents:id"].toString()
        if (!dataId.isNullOrEmpty()) {
            logger.info("Internal Storage sent a message - job done")
            logger.info(
                "Dataset with dataId $dataId was successfully stored. Correlation ID: $correlationId.",
            )
            dataInformationHashMap.remove(dataId)
        } else {
            val internalMessage = "Error storing data. Correlation ID: $correlationId"
            logger.error(internalMessage)
            throw InternalServerErrorApiException(
                "Storing of dataset failed", "The storing of the dataset failed",
                internalMessage,
            )
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
        val dataAsString = getDataFromStorage(dataId, correlationId)
        if (dataAsString == "") {
            throw ResourceNotFoundApiException(
                "Dataset not found",
                "No dataset with the id: $dataId could be found in the data store.",
            )
        }
        logger.info("Received Dataset of length ${dataAsString.length}. Correlation ID: $correlationId")
        val dataAsStorableDataSet = objectMapper.readValue(dataAsString, StorableDataSet::class.java)
        dataAsStorableDataSet.requireConsistencyWith(dataMetaInformation)
        return dataAsStorableDataSet
    }

    private fun getDataFromStorage(dataId: String, correlationId: String): String {
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
