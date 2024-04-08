package org.dataland.datalandbackend.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.entities.DataIdToAssetIdMappingEntity
import org.dataland.datalandbackend.entities.DataMetaInformationEntity
import org.dataland.datalandbackend.frameworks.sme.model.SmeData
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.model.StorableDataSet
import org.dataland.datalandbackend.model.companies.CompanyAssociatedData
import org.dataland.datalandbackend.model.metainformation.DataMetaInformation
import org.dataland.datalandbackend.repositories.DataIdToAssetIdMappingRepository
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandbackendutils.services.generateRandomDataId
import org.dataland.datalandbackendutils.utils.sha256
import org.dataland.datalandinternalstorage.openApiClient.api.StorageControllerApi
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.datalandmessagequeueutils.constants.ActionType
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageHeaderKey
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
import org.dataland.datalandmessagequeueutils.exceptions.MessageQueueRejectException
import org.dataland.datalandmessagequeueutils.utils.MessageQueueUtils
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
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
import org.springframework.web.multipart.MultipartFile
import java.time.Instant
import java.util.*

/**
 * Implementation of a data manager for Dataland including metadata storages
 * @param objectMapper object mapper used for converting data classes to strings and vice versa
 * @param companyQueryManager service for managing company data
 * @param metaDataManager service for managing metadata
 * @param storageClient service for managing data
 * @param cloudEventMessageHandler service for managing CloudEvents messages
*/
@Component("PrivateDataManager")
class PrivateDataManager(
    @Autowired private val objectMapper: ObjectMapper,
    @Autowired private val companyQueryManager: CompanyQueryManager,
    @Autowired private val metaDataManager: DataMetaInformationManager,
    @Autowired private val storageClient: StorageControllerApi,
    @Autowired private val cloudEventMessageHandler: CloudEventMessageHandler,
    @Autowired private val messageUtils: MessageQueueUtils,
    @Autowired private val dataIdToAssetIdMappingRepository: DataIdToAssetIdMappingRepository,
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val privateDataInMemoryStorage = mutableMapOf<String, String>()
    private val metaInfoEntityInMemoryStorage = mutableMapOf<String, DataMetaInformationEntity>()
    private val documentInMemoryStorage = mutableMapOf<String, ByteArray>()
    private val dataDocumentMapInMemoryStorage = mutableMapOf<String, MutableList<String>>()

    /**
     * Processes a private sme data storage request.
     * @param companyAssociatedSmeData contains the JSON to store
     * @param documents contains the documents associated with the JSON
     * @returns the data meta info object generated during the storage process
     */
    fun processPrivateSmeDataStorageRequest(
        companyAssociatedSmeData: CompanyAssociatedData<SmeData>,
        documents: Array<MultipartFile>?,
    ): DataMetaInformation {
        val uploadTime = Instant.now().toEpochMilli()
        val correlationId = UUID.randomUUID().toString()
        logger.info(
            "Received MiNaBo data for companyId ${companyAssociatedSmeData.companyId} to be stored. " +
                "Will be processed with correlationId $correlationId",
        )

        val userAuthentication = DatalandAuthentication.fromContext()
        val storableDataSet = StorableDataSet(
            companyId = companyAssociatedSmeData.companyId,
            dataType = DataType.of(SmeData::class.java),
            uploaderUserId = userAuthentication.userId,
            uploadTime = uploadTime,
            reportingPeriod = companyAssociatedSmeData.reportingPeriod,
            data = companyAssociatedSmeData.data.toString(),
        )
        val dataId = generateRandomDataId()

        storeDatasetInMemory(dataId, storableDataSet, correlationId)
        val metaInfoEntity = buildMetaInfoEntity(dataId, storableDataSet)
        storeMetaInfoEntityInMemory(dataId, metaInfoEntity, correlationId)
        val documentHashes = storeDocumentsInMemoryAndReturnTheirHashes(dataId, documents, correlationId)
        sendReceptionMessage(dataId, correlationId, documentHashes)
        return metaInfoEntity.toApiModel(userAuthentication)
    }

    private fun storeDatasetInMemory(dataId: String, storableDataSet: StorableDataSet, correlationId: String) {
        logger.info(
            "Storing storable dataset in memory for companyId: ${storableDataSet.companyId}, dataId: $dataId and " +
                "correlationId: $correlationId",
        )
        val storableSmeDatasetAsString = objectMapper.writeValueAsString(storableDataSet)
        privateDataInMemoryStorage[dataId] = storableSmeDatasetAsString
    }

    private fun buildMetaInfoEntity(dataId: String, storableDataSet: StorableDataSet): DataMetaInformationEntity {
        val company = companyQueryManager.getCompanyById(storableDataSet.companyId)
        return DataMetaInformationEntity(
            dataId,
            company,
            storableDataSet.dataType.toString(),
            storableDataSet.uploaderUserId,
            storableDataSet.uploadTime,
            storableDataSet.reportingPeriod,
            null,
            QaStatus.Pending,
        )
    }

    private fun storeMetaInfoEntityInMemory(dataId: String, metaInfoEntity: DataMetaInformationEntity, correlationId: String) {
        logger.info(
            "Storing metadata entry in memory for companyId: ${metaInfoEntity.company.companyId}, dataId: $dataId and " +
                "correlationId: $correlationId",
        )
        metaInfoEntityInMemoryStorage[dataId] = metaInfoEntity
    }

    private fun storeDocumentsInMemoryAndReturnTheirHashes(dataId: String, documents: Array<MultipartFile>?, correlationId: String): MutableList<String> {
        // TODO: MultipartFiles refer to temporary files that only exist during the lifetime of the request
        //  ==> Need to copy it to refer to it afterwards.
        //  See: https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/web/multipart/MultipartFile.html
        //  Maybe we should use the same approach as in the other document service
        //  I've changed it a bit, so that it now works for pdfs. Someone should double check if the approach now is fine
        //  and we need to decide if we want to accept other types and how to handle/convert them - Stephan
        logger.info("Storing Sme documents in temporary storage for dataId $dataId, $documents and correlationId $correlationId.")
        val documentHashes = mutableListOf<String>()
        if (!documents.isNullOrEmpty()) {
            for (document in documents) {
                val documentId = document.bytes.sha256() // TODO needs to be the same as in Frontend!! test? one-off test?
                val documentAsByteArray = convertMultipartFileToByteArray(document, correlationId)
                documentHashes.add(documentId)
                documentInMemoryStorage[documentId] = documentAsByteArray
            }

            dataDocumentMapInMemoryStorage[dataId] = documentHashes
        }
        return documentHashes
    }

    private fun sendReceptionMessage(dataId: String, correlationId: String, documentHashes: MutableList<String>) {
        logger.info(
            "Processed data to be stored in external storage, sending message for dataId: $dataId and " +
                "correlationId: $correlationId",
        )
        val payload = JSONObject(
            mapOf(
                "dataId" to dataId,
                "actionType" to
                    ActionType.StorePrivateDataAndDocuments,
                "documentHashes" to documentHashes,
            ),
        ).toString()
        cloudEventMessageHandler.buildCEMessageAndSendToQueue(
            payload, MessageType.PrivateDataReceived, correlationId,
            ExchangeName.PrivateRequestReceived,
        )
        logger.info(
            "Message to external storage for dataId: $dataId and correlationId: $correlationId was " +
                "sent successfully",
        )
    }

    private fun persistMappingInfo(dataId: String, correlationId: String) {
        logger.info(
            "Storing mapping entry permanently for dataId: $dataId and correlationId: $correlationId as it " +
                "was successfully stored in the external datastore",
        )
        val dataIdToJsonMappingEntity = DataIdToAssetIdMappingEntity(dataId = dataId, assetId = "JSON")
        dataIdToAssetIdMappingRepository.save(dataIdToJsonMappingEntity)
        val dataDocumentsMapping = dataDocumentMapInMemoryStorage[dataId]
        if (!dataDocumentsMapping.isNullOrEmpty()) {
            val dataIdToDocumentMappingEntities =
                dataDocumentsMapping!!.map { document ->
                    DataIdToAssetIdMappingEntity(dataId, document)
                }
            dataIdToDocumentMappingEntities.forEach {
                    document ->
                dataIdToAssetIdMappingRepository.save(document)
            }
        }
    }
    private fun persistMetaInfo(dataId: String, correlationId: String) {
        val dataMetaInfoEntityForDataId = metaInfoEntityInMemoryStorage[dataId]
        val dataMetaInfoToStore = dataMetaInfoEntityForDataId?.copy(qaStatus = QaStatus.Accepted)
        metaDataManager.setActiveDataset(dataMetaInfoToStore!!)
        metaDataManager.storeDataMetaInformation(dataMetaInfoToStore!!)
    }

    /**
     *
     */
    @RabbitListener(
        bindings = [
            QueueBinding(
                value = Queue(
                    "dataStoredBackendPrivateDataManager",
                    arguments = [
                        Argument(name = "x-dead-letter-exchange", value = ExchangeName.DeadLetter),
                        Argument(name = "x-dead-letter-routing-key", value = "deadLetterKey"),
                        Argument(name = "defaultRequeueRejected", value = "false"),
                    ],
                ),
                exchange = Exchange(ExchangeName.PrivateItemStored, declare = "false"),
                key = [RoutingKeyNames.data],
            ),
        ],
    )
    private fun processStoredPrivateSmeData(
        @Payload payload: String,
        @Header(MessageHeaderKey.CorrelationId) correlationId: String,
        @Header(MessageHeaderKey.Type) type: String,
    ) {
        logger.info(payload)
        messageUtils.validateMessageType(type, MessageType.PrivateDataStored)
        val dataId = JSONObject(payload).getString("dataId")
        if (dataId.isEmpty()) {
            throw MessageQueueRejectException("Provided data ID is empty")
        }
        logger.info(
            "Private dataset with dataId $dataId was successfully stored on EuroDaT. Correlation ID: $correlationId.",
        )
        messageUtils.rejectMessageOnException {
            persistMappingInfo(dataId, correlationId)
            persistMetaInfo(dataId, correlationId)
            privateDataInMemoryStorage.remove(dataId)
            metaInfoEntityInMemoryStorage.remove(dataId)
            documentInMemoryStorage.remove(dataId)
        }
    }

    /**
     * This method retrieves private data from the temporary storage
     * @param dataId is the identifier for which all stored data entries in the temporary storage are filtered
     * @return stringified data entry from the temporary store
     */
    fun selectPrivateDataSetFromTemporaryStorage(dataId: String): String {
        val rawValue = privateDataInMemoryStorage.getOrElse(dataId) {
            throw ResourceNotFoundApiException(
                "Data ID not found in temporary storage",
                "Dataland does not know the data id $dataId",
            )
        }
        return objectMapper.writeValueAsString(rawValue)
    }
    // TODO this method has to return data and documents, alternatively we use two different endpoints

    private fun convertMultipartFileToByteArray(multipartFile: MultipartFile, correlationId: String): ByteArray {
        return multipartFile.bytes
    }

    /**
     * Retrieves the data identified by the given hash from the in-memory store.
     */
    fun getDocumentFromInMemoryStore(hash: String): ByteArray? {
        return documentInMemoryStorage[hash]
    }
}
