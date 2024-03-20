package org.dataland.datalandbackend.services

import org.dataland.datalandbackend.entities.DataDocumentMappingEntity
import org.dataland.datalandbackend.frameworks.sme.model.SmeData
import org.dataland.datalandbackend.model.companies.CompanyAssociatedData
import org.dataland.datalandbackend.repositories.DataDocumentsMappingRepository
import org.dataland.datalandinternalstorage.openApiClient.api.StorageControllerApi
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.datalandmessagequeueutils.constants.ActionType
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.utils.MessageQueueUtils
import org.json.JSONObject
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import java.util.*

/**
 * Implementation of a data manager for Dataland including metadata storages
 * @param objectMapper object mapper used for converting data classes to strings and vice versa
 * @param companyManager service for managing company data
 * @param metaDataManager service for managing metadata
 * @param storageClient service for managing data
 * @param cloudEventMessageHandler service for managing CloudEvents messages
*/
@Component("PrivateDataManager")
class PrivateDataManager(
    @Autowired private val metaDataManager: DataMetaInformationManager,
    @Autowired private val storageClient: StorageControllerApi,
    @Autowired private val cloudEventMessageHandler: CloudEventMessageHandler,
    @Autowired private val dataDocumentsMappingRepositoryInterface: DataDocumentsMappingRepository,
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val dataInMemoryStorage = mutableMapOf<String, String>()

    fun storePrivateData(
        data: CompanyAssociatedData<SmeData>,
        documentId: String,
    fun processPrivateSmeDataStorageRequest(
        companyAssociatedSmeData: CompanyAssociatedData<SmeData>,
        documents: Array<MultipartFile>,
        correlationId: String,
    ) {
        logger.info(
            " " + // TODO Emanuel: check later for injection of logger
                "Correlation ID: $correlationId",
        )
        val dataId = generateRandomDataId()
        storeDataSetInMemory(dataId, correlationId)
        sendReceptionMessage(dataId, correlationId)
    }

    /**
     * Method to associate data information with a specific company
     * @param dataDocumentMapping The data meta information which should be stored
     */
    fun storeDataDocumentMapping(
        dataDocumentMapping: DataDocumentMappingEntity,
    ): DataDocumentMappingEntity {
        return dataDocumentsMappingRepositoryInterface.save(dataDocumentMapping)
    }

    private fun storeDataSetInMemory(dataId: String, correlationId: String) {
        // TODO log smth with correlation Id
        dataInMemoryStorage[dataId] = objectMapper.writeValueAsString(storableDataSet)

    }

    private fun sendReceptionMessage(dataId: String, correlationId: String) {
        // TODO log smth with correlation Id
        val payload = JSONObject(
            mapOf(
                "dataId" to dataId,
                "actionType" to
                        ActionType.StoreData, // TODO we need a new action type and message type for private data
            ),
        ).toString()
        cloudEventMessageHandler.buildCEMessageAndSendToQueue(
            payload, MessageType.DataReceived, correlationId, // TODO private data received
            ExchangeName.RequestReceived,
        )
        // TODO log that it has been send (with correlation Id mentioned)
    }

    private fun persistMetaInfo() {

    }
    /**
     * Method to generate a random Data ID
     * @return generated UUID
     */

    private fun generateRandomDataId(): String{ //TODO THIS IS A DUPLICATE!!! already existing in DataManager. move to util to avoid duplicate code!
            return "${UUID.randomUUID()}"
        }
}
