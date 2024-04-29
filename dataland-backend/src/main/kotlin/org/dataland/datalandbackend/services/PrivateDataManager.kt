package org.dataland.datalandbackend.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.DatamanagerUtils
import org.dataland.datalandbackend.entities.DataIdToAssetIdMappingEntity
import org.dataland.datalandbackend.entities.DataMetaInformationEntity
import org.dataland.datalandbackend.frameworks.sme.model.SmeData
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.model.StorableDataSet
import org.dataland.datalandbackend.model.companies.CompanyAssociatedData
import org.dataland.datalandbackend.model.metainformation.DataMetaInformation
import org.dataland.datalandbackend.repositories.DataIdToAssetIdMappingRepository
import org.dataland.datalandbackend.utils.IdUtils
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandbackendutils.utils.sha256
import org.dataland.datalandexternalstorage.openApiClient.api.ExternalStorageControllerApi
import org.dataland.datalandinternalstorage.openApiClient.infrastructure.ClientException
import org.dataland.datalandinternalstorage.openApiClient.infrastructure.ServerException
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
 * @param cloudEventMessageHandler service for managing CloudEvents messages
 * @param messageUtils contains util methods for messages used on the message queue
 * @param dataIdToAssetIdMappingRepository the repository to map dataId to document hashes and document Ids
*/
@Component("PrivateDataManager")
class PrivateDataManager(
    @Autowired private val objectMapper: ObjectMapper,
    @Autowired private val companyQueryManager: CompanyQueryManager,
    @Autowired private val metaDataManager: DataMetaInformationManager,
    @Autowired private val cloudEventMessageHandler: CloudEventMessageHandler,
    @Autowired private val messageUtils: MessageQueueUtils,
    @Autowired private val dataIdToAssetIdMappingRepository: DataIdToAssetIdMappingRepository,
    @Autowired private val datamanagerUtils: DatamanagerUtils,
    @Autowired private val storageClient: ExternalStorageControllerApi,
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val jsonDataInMemoryStorage = mutableMapOf<String, String>()
    private val metaInfoEntityInMemoryStorage = mutableMapOf<String, DataMetaInformationEntity>()
    private val documentHashesInMemoryStorage = mutableMapOf<String, MutableMap<String, String>>()
    private val documentInMemoryStorage = mutableMapOf<String, ByteArray>()

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
        val dataId = IdUtils.generateUUID()

        storeJsonInMemory(dataId, storableDataSet, correlationId)
        val metaInfoEntity = buildMetaInfoEntity(dataId, storableDataSet)
        storeMetaInfoEntityInMemory(dataId, metaInfoEntity, correlationId)
        val documentHashes = documents?.takeIf { it.isNotEmpty() }
            ?.let { storeDocumentsInMemoryAndReturnTheirHashes(dataId, it, correlationId) }
            ?: mutableMapOf()
        sendReceptionMessage(dataId, correlationId, documentHashes)
        return metaInfoEntity.toApiModel(userAuthentication)
    }

    private fun storeJsonInMemory(dataId: String, storableDataSet: StorableDataSet, correlationId: String) {
        val storableSmeDatasetAsString = objectMapper.writeValueAsString(storableDataSet)
        jsonDataInMemoryStorage[dataId] = storableSmeDatasetAsString
        logger.info(
            "Stored JSON in memory for companyId ${storableDataSet.companyId} dataId $dataId and " +
                "correlationId $correlationId",
        )
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

    private fun storeMetaInfoEntityInMemory(
        dataId: String,
        metaInfoEntity: DataMetaInformationEntity,
        correlationId: String,
    ) {
        metaInfoEntityInMemoryStorage[dataId] = metaInfoEntity
        logger.info(
            "Stored metadata entry in memory for companyId ${metaInfoEntity.company.companyId}, " +
                "dataId $dataId and correlationId $correlationId",
        )
    }

    private fun storeDocumentsInMemoryAndReturnTheirHashes(
        dataId: String,
        documents: Array<MultipartFile>,
        correlationId: String,
    ): MutableMap<String, String> {
        val documentHashes = mutableMapOf<String, String>()
        for (document in documents) {
            val documentHash = document.bytes.sha256()
            val documentUuid = IdUtils.generateUUID()
            documentHashes[documentHash] = documentUuid
            val documentAsByteArray = convertMultipartFileToByteArray(document)
            documentInMemoryStorage[documentHash] = documentAsByteArray
        }
        logger.info(
            "Stored ${documentHashes.size} distinct Sme document/s in temporary storage for dataId $dataId " +
                "and correlationId $correlationId",
        )
        documentHashesInMemoryStorage[dataId] = documentHashes
        return documentHashes
    }

    private fun convertMultipartFileToByteArray(multipartFile: MultipartFile): ByteArray {
        return multipartFile.bytes
    }

    private fun sendReceptionMessage(
        dataId: String,
        correlationId: String,
        documentHashes: Map<String, String>,
    ) {
        logger.info(
            "Processed data to be stored in EuroDaT, sending message for dataId $dataId and " +
                "correlationId $correlationId",
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
            "Message to EuroDaT-storage-service for dataId $dataId and correlationId $correlationId was sent",
        )
    }

    /**
     * This method
     * @param payload the paylod of the received message from the message queue
     * @param correlationId the correlationId of the request
     * @param type the type of the message
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
    fun processStoredPrivateSmeData(
        @Payload payload: String,
        @Header(MessageHeaderKey.CorrelationId) correlationId: String,
        @Header(MessageHeaderKey.Type) type: String,
    ) {
        messageUtils.validateMessageType(type, MessageType.PrivateDataStored)
        val dataId = JSONObject(payload).getString("dataId")
        if (dataId.isEmpty()) {
            throw MessageQueueRejectException("Provided data ID is empty")
        }
        logger.info(
            "Received message that dataset with dataId $dataId and correlationId $correlationId was successfully " +
                "stored on EuroDaT. Starting to persist mapping info, meta info and clearing in-memory-storages",
        )
        messageUtils.rejectMessageOnException {
            persistMappingInfo(dataId, correlationId)
            persistMetaInfo(dataId, correlationId)
            removeRelatedEntriesFromInMemoryStorages(dataId, correlationId)
        }
    }

    private fun removeRelatedEntriesFromInMemoryStorages(dataId: String, correlationId: String) {
        logger.info(
            "Removing entries related to dataId $dataId and correlationId $correlationId from in-memory-storages",
        )
        jsonDataInMemoryStorage.remove(dataId)
        metaInfoEntityInMemoryStorage.remove(dataId)
        val documentHashes = documentHashesInMemoryStorage[dataId]
        documentHashes?.let { removeDocumentsAndHashesFromInMemoryStorages(dataId, it) }
    }

    private fun persistMappingInfo(dataId: String, correlationId: String) {
        logger.info(
            "Persisting mapping info for dataId $dataId and correlationId $correlationId",
        )
        val dataIdToJsonMappingEntity = DataIdToAssetIdMappingEntity(
            dataId = dataId, assetId = "JSON",
            eurodatId = "JSON",
        )
        dataIdToAssetIdMappingRepository.save(dataIdToJsonMappingEntity)
        val documentHashes = documentHashesInMemoryStorage[dataId]
        if (!documentHashes.isNullOrEmpty()) {
            val dataIdToDocumentHashMappingEntities =
                documentHashes.map { documentHash ->
                    DataIdToAssetIdMappingEntity(dataId, documentHash.key, documentHash.value)
                }
            dataIdToDocumentHashMappingEntities.forEach {
                    mappingEntity ->
                dataIdToAssetIdMappingRepository.save(mappingEntity)
            }
        }
    }
    private fun persistMetaInfo(dataId: String, correlationId: String) {
        logger.info(
            "Persisting meta info for dataId $dataId and correlationId $correlationId",
        )
        val dataMetaInfoEntityForDataId = metaInfoEntityInMemoryStorage[dataId]
        val dataMetaInfoToStore = dataMetaInfoEntityForDataId?.copy(qaStatus = QaStatus.Accepted)
        metaDataManager.setActiveDataset(dataMetaInfoToStore!!)
        metaDataManager.storeDataMetaInformation(dataMetaInfoToStore)
    }

    private fun removeDocumentsAndHashesFromInMemoryStorages(
        dataId: String,
        documentHashes: MutableMap<String, String>,
    ) {
        documentHashes.keys.forEach { hash ->
            documentInMemoryStorage.remove(hash)
        }
        documentHashesInMemoryStorage.remove(dataId)
    }

    /**
     * This method retrieves private data from the temporary storage
     * @param dataId is the identifier for which all stored data entries in the temporary storage are filtered
     * @return stringified data entry from the temporary store
     */
    fun getJsonFromInMemoryStore(dataId: String): String {
        val rawValue = jsonDataInMemoryStorage.getOrElse(dataId) {
            throw ResourceNotFoundApiException(
                "Data ID not found in temporary storage",
                "Dataland does not know the data id $dataId",
            )
        }
        return objectMapper.writeValueAsString(rawValue)
    }

    /**
     * Retrieves the document identified by the given hash from the in-memory store.
     * @param hash of the document which should be retrieved
     */
    fun getDocumentFromInMemoryStore(hash: String): ByteArray? {
        return documentInMemoryStorage[hash]
    }

    /**
     * Retrieves a private sme data object from the private storage
     */
    fun getPrivateDataSet(dataId: String, correlationId: String): StorableDataSet {
        val dataMetaInformation = metaDataManager.getDataMetaInformationByDataId(dataId)
        datamanagerUtils.assertActualAndExpectedDataTypeForIdMatch(
            dataId, DataType.of(SmeData::class.java),
            dataMetaInformation, correlationId,
        )
        lateinit var dataAsString: String
        try {
            dataAsString = getDataFromCacheOrStorageService(dataId, correlationId)
        } catch (e: ClientException) {
            datamanagerUtils.handleStorageClientException(e, dataId, correlationId)
        }
        logger.info("Received Dataset of length ${dataAsString.length}. Correlation ID: $correlationId")
        val dataAsStorableDataSet = objectMapper.readValue(dataAsString, StorableDataSet::class.java)
        dataAsStorableDataSet.requireConsistencyWith(dataMetaInformation)
        return dataAsStorableDataSet
    }
    private fun getDataFromCacheOrStorageService(dataId: String, correlationId: String): String {
        return jsonDataInMemoryStorage[dataId] ?: getDataFromStorageService(dataId, correlationId)
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
}
