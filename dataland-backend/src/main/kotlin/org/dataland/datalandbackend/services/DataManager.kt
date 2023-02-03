package org.dataland.datalandbackend.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.entities.StoredCompanyEntity
import org.dataland.datalandbackend.model.DataMetaInformation
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.model.StorableDataSet
import org.dataland.datalandbackend.model.StorageHashMap
import org.dataland.datalandbackendutils.cloudevents.CloudEventMessageHandler
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandinternalstorage.openApiClient.api.StorageControllerApi
import org.dataland.datalandinternalstorage.openApiClient.infrastructure.ServerException
import org.slf4j.LoggerFactory
import org.springframework.amqp.core.Message
import org.springframework.amqp.rabbit.annotation.RabbitHandler
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
 * @param dataInformationHashMap map for temporarily storing data information in memory
*/
@ComponentScan(basePackages = ["org.dataland"])
@Component("DataManager")
class DataManager(
    @Autowired var objectMapper: ObjectMapper,
    @Autowired var companyManager: CompanyManager,
    @Autowired var metaDataManager: DataMetaInformationManager,
    @Autowired var storageClient: StorageControllerApi,
    @Autowired var cloudEventMessageHandler: CloudEventMessageHandler,
    @Autowired var dataInformationHashMap: StorageHashMap

) {
    private val logger = LoggerFactory.getLogger(javaClass)

    private fun assertActualAndExpectedDataTypeForIdMatch(
        dataId: String,
        dataType: DataType,
        correlationId: String
    ) {
        val dataMetaInformation = metaDataManager.getDataMetaInformationByDataId(dataId)
        if (DataType.valueOf(dataMetaInformation.dataType) != dataType) {
            throw InvalidInputApiException(
                "Requested data $dataId not of type $dataType",
                "The data with the id: $dataId is registered as type" +
                    " ${dataMetaInformation.dataType} by Dataland instead of your requested" +
                    " type $dataType."
            )
        }
        logger.info(
            "Requesting Data with ID $dataId and expected type $dataType from framework data storage. " +
                "Correlation ID: $correlationId"
        )
    }

    /**
     * Method to make the data manager add data to a data store and store meta data in Dataland
     * @param storableDataSet contains all the inputs needed by Dataland
     * @return ID of the newly stored data in the data store
     */
    @Transactional
    @RabbitHandler
    fun addDataSet(storableDataSet: StorableDataSet, correlationId: String): String {
        val company = companyManager.getCompanyById(storableDataSet.companyId)
        logger.info(
            "Sending StorableDataSet of type ${storableDataSet.dataType} for company ID " +
                "${storableDataSet.companyId}, Company Name ${company.companyName} to storage Interface. " +
                "Correlation ID: $correlationId"
        )
        val dataId: String = storeDataSet(storableDataSet, company.companyName, correlationId)
        storeMetaDataInformation(dataId, storableDataSet.dataType, storableDataSet.uploaderUserId, storableDataSet.uploadTime, company, "No")
        cloudEventMessageHandler.buildCEMessageAndSendToQueue(dataId, "New data - QA necessary", correlationId, "upload_queue")
        return dataId
    }

    /**
     * Method that listens to the qa_queue and updates the metadata information after successful qa process
     * @param message is the message delivered on the message queue
     */
    @RabbitListener(queues = ["qa_queue"])
    @RabbitHandler
    fun updateMetaDataAfterQA(message: Message) {
        val dataId = cloudEventMessageHandler.bodyToString(message)
        val correlationId = message.messageProperties.headers["cloudEvents:id"].toString()
        if (dataId.isNotEmpty()) {
            val metaInformation = metaDataManager.getDataMetaInformationByDataId(dataId)
            storeMetaDataInformation(dataId, DataType.valueOf(metaInformation.dataType), metaInformation.uploaderUserId, metaInformation.uploadTime, metaInformation.company, "Yes")
            logger.info("Received quality assurance for data upload with DataId: $dataId with Correlation Id: $correlationId")
        }
    }

    private fun storeMetaDataInformation(
        dataId: String,
        dataType: DataType,
        uploaderUserId: String,
        uploadTime: Long,
        company: StoredCompanyEntity,
        qualityAssuredYesNo: String,
    ) {
        metaDataManager.storeDataMetaInformation(
            dataId,
            dataType,
            uploaderUserId,
            uploadTime,
            company,
            qualityAssuredYesNo,
        )
    }

    @RabbitHandler
    private fun storeDataSet(
        storableDataSet: StorableDataSet,
        companyName: String,
        correlationId: String
    ): String {
        val dataId = "${UUID.randomUUID()}:${UUID.randomUUID()}_${UUID.randomUUID()}"
        dataInformationHashMap.map.put(dataId, objectMapper.writeValueAsString(storableDataSet))
        logger.info(dataInformationHashMap.map[dataId])
        cloudEventMessageHandler.buildCEMessageAndSendToQueue(dataId, "Data to be stored", correlationId,
            "storage_queue")
        logger.info(
            "Stored StorableDataSet of type ${storableDataSet.dataType} for company ID ${storableDataSet.companyId}," +
                " Company Name $companyName received ID $dataId from storage. Correlation ID: $correlationId."
        )
        return(dataId)
    }

    /**
     * Method to log success notification associated to certain dataId and correlationId
     * @param message Message retrieved from stored_queue
     */
    @RabbitListener(queues = ["stored_queue"])
    // @RabbitHandler
    fun loggingOfStoredDataSet(message: Message) {
        val dataId = cloudEventMessageHandler.bodyToString(message)
        val correlationId = message.messageProperties.headers["cloudEvents:id"].toString()
        logger.info("Internal Storage sent a message - job done")
        logger.info(
            "Dataset with dataId $dataId was sucessfully stored. Correlation ID: $correlationId."
        )
        dataInformationHashMap.map.remove(dataId)
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
                "No dataset with the id: $dataId could be found in the data store."
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
                    " ${e.message}. Correlation ID: $correlationId"
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
