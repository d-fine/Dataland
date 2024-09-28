package org.dataland.datalandbackend.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.entities.DataIdAndHashToEurodatIdMappingEntity
import org.dataland.datalandbackend.entities.DataMetaInformationEntity
import org.dataland.datalandbackend.frameworks.vsme.model.VsmeData
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.model.StorableDataSet
import org.dataland.datalandbackend.model.companies.CompanyAssociatedData
import org.dataland.datalandbackend.model.metainformation.DataMetaInformation
import org.dataland.datalandbackend.repositories.DataIdAndHashToEurodatIdMappingRepository
import org.dataland.datalandbackend.utils.IdUtils.generateCorrelationId
import org.dataland.datalandbackend.utils.IdUtils.generateUUID
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandbackendutils.model.DocumentStream
import org.dataland.datalandbackendutils.model.DocumentType
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandbackendutils.utils.sha256
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.datalandmessagequeueutils.constants.ActionType
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.json.JSONObject
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.InputStreamResource
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import java.io.ByteArrayInputStream
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

/**
 * Implementation of a data manager for Dataland including metadata storages
 * @param objectMapper object mapper used for converting data classes to strings and vice versa
 * @param metaDataManager service for managing metadata
 * @param cloudEventMessageHandler service for managing CloudEvents messages
 * @param dataIdAndHashToEurodatIdMappingRepository the repository to map dataId to document hashes and document Ids
 * @param externalStorageDataGetter is a util class which contains the necessary storage clients to be used here
 * @param dataManagerUtils is a util class which contains methods for the data manager services
 */
@Component("PrivateDataManager")
class PrivateDataManager(
    @Autowired private val objectMapper: ObjectMapper,
    @Autowired private val metaDataManager: DataMetaInformationManager,
    @Autowired private val cloudEventMessageHandler: CloudEventMessageHandler,
    @Autowired private val dataIdAndHashToEurodatIdMappingRepository: DataIdAndHashToEurodatIdMappingRepository,
    @Autowired private val externalStorageDataGetter: ExternalStorageDataGetter,
    @Autowired private val dataManagerUtils: DataManagerUtils,
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val jsonDataInMemoryStorage = ConcurrentHashMap<String, String>()
    private val metaInfoEntityInMemoryStorage = ConcurrentHashMap<String, DataMetaInformationEntity>()
    private val documentHashesInMemoryStorage = ConcurrentHashMap<String, MutableMap<String, String>>()
    private val documentInMemoryStorage = ConcurrentHashMap<String, ByteArray>()

    /**
     * Processes a private vsme data storage request.
     * @param companyAssociatedVsmeData contains the JSON to store
     * @param documents contains the documents associated with the JSON
     * @returns the data meta info object generated during the storage process
     */
    fun processPrivateVsmeDataStorageRequest(
        companyAssociatedVsmeData: CompanyAssociatedData<VsmeData>,
        documents: Array<MultipartFile>?,
    ): DataMetaInformation {
        val uploadTime = Instant.now().toEpochMilli()
        val correlationId = generateCorrelationId(companyId = companyAssociatedVsmeData.companyId, dataId = null)
        logger.info(
            "Received MiNaBo data for companyId ${companyAssociatedVsmeData.companyId} to be stored. " +
                "Will be processed with correlationId $correlationId",
        )

        val userAuthentication = DatalandAuthentication.fromContext()
        val storableDataSet =
            StorableDataSet(
                companyId = companyAssociatedVsmeData.companyId,
                dataType = DataType.of(VsmeData::class.java),
                uploaderUserId = userAuthentication.userId,
                uploadTime = uploadTime,
                reportingPeriod = companyAssociatedVsmeData.reportingPeriod,
                data = objectMapper.writeValueAsString(companyAssociatedVsmeData.data),
            )
        val dataId = generateUUID()

        storeJsonInMemory(dataId, storableDataSet, correlationId)
        val metaInfoEntity = buildMetaInfoEntity(dataId, storableDataSet)
        storeMetaInfoEntityInMemory(dataId, metaInfoEntity, correlationId)
        val documentHashes =
            documents
                ?.takeIf { it.isNotEmpty() }
                ?.let { storeDocumentsInMemoryAndReturnTheirHashes(dataId, it, correlationId) }
                ?: mutableMapOf()
        sendReceptionMessage(dataId, storableDataSet, correlationId, documentHashes)
        return metaInfoEntity.toApiModel(userAuthentication)
    }

    private fun storeJsonInMemory(
        dataId: String,
        storableDataSet: StorableDataSet,
        correlationId: String,
    ) {
        val storableVsmeDatasetAsString = objectMapper.writeValueAsString(storableDataSet)
        jsonDataInMemoryStorage[dataId] = storableVsmeDatasetAsString
        logger.info(
            "Stored JSON in memory for companyId ${storableDataSet.companyId} dataId $dataId and " +
                "correlationId $correlationId",
        )
    }

    private fun buildMetaInfoEntity(
        dataId: String,
        storableDataSet: StorableDataSet,
    ): DataMetaInformationEntity {
        val company = dataManagerUtils.getCompanyByCompanyId(storableDataSet.companyId)
        return DataMetaInformationEntity(
            dataId,
            company,
            storableDataSet.dataType.toString(),
            storableDataSet.uploaderUserId,
            storableDataSet.uploadTime,
            storableDataSet.reportingPeriod,
            true,
            QaStatus.Accepted,
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
            val documentUuid = generateUUID()
            documentHashes[documentHash] = documentUuid
            val documentAsByteArray = convertMultipartFileToByteArray(document)
            documentInMemoryStorage[documentHash] = documentAsByteArray
        }
        logger.info(
            "Stored ${documentHashes.size} distinct Vsme document/s in temporary storage for dataId $dataId " +
                "and correlationId $correlationId",
        )
        documentHashesInMemoryStorage[dataId] = documentHashes
        return documentHashes
    }

    private fun convertMultipartFileToByteArray(multipartFile: MultipartFile): ByteArray = multipartFile.bytes

    private fun sendReceptionMessage(
        dataId: String,
        storableDataset: StorableDataSet,
        correlationId: String,
        documentHashes: Map<String, String>,
    ) {
        logger.info(
            "Processed data to be stored in EuroDaT, sending message for dataId $dataId and " +
                "correlationId $correlationId",
        )
        val payload =
            JSONObject(
                mapOf(
                    "dataId" to dataId,
                    "companyId" to storableDataset.companyId,
                    "framework" to storableDataset.dataType.toString(),
                    "reportingPeriod" to storableDataset.reportingPeriod,
                    "actionType" to ActionType.STORE_PRIVATE_DATA_AND_DOCUMENTS,
                    "documentHashes" to documentHashes,
                ),
            ).toString()
        cloudEventMessageHandler.buildCEMessageAndSendToQueue(
            payload, MessageType.PRIVATE_DATA_RECEIVED, correlationId,
            ExchangeName.PRIVATE_REQUEST_RECEIVED, RoutingKeyNames.PRIVATE_DATA_AND_DOCUMENT,
        )
        logger.info("Message to EuroDaT-storage-service for dataId $dataId and correlationId $correlationId was sent")
    }

    /**
     * The method removes entries in the in memory storages which are connected to the specified dataId
     * @param dataId the dataId for which connected entries in the in-memory storages should be removed
     * @param correlationId the correlationId of the storing process
     */
    fun removeRelatedEntriesFromInMemoryStorages(
        dataId: String,
        correlationId: String,
    ) {
        logger.info(
            "Removing entries related to dataId $dataId and correlationId $correlationId from in-memory-storages",
        )
        jsonDataInMemoryStorage.remove(dataId)
        metaInfoEntityInMemoryStorage.remove(dataId)
        val documentHashes = documentHashesInMemoryStorage[dataId]
        documentHashes?.let { removeDocumentsAndHashesFromInMemoryStorages(dataId, it) }
    }

    /**
     * The method persists the mapping information between dataId, document hash and documentId
     * @param dataId the dataId for which the respective information should be persisted
     * @param correlationId the correlationId of the storing process
     */
    fun persistMappingInfo(
        dataId: String,
        correlationId: String,
    ) {
        logger.info(
            "Persisting mapping info for dataId $dataId and correlationId $correlationId",
        )
        val dataIdToJsonMappingEntity =
            DataIdAndHashToEurodatIdMappingEntity(
                dataId = dataId, hash = "JSON",
                eurodatId = "JSON",
            )
        dataIdAndHashToEurodatIdMappingRepository.save(dataIdToJsonMappingEntity)
        val documentHashes = documentHashesInMemoryStorage[dataId]
        if (!documentHashes.isNullOrEmpty()) {
            val dataIdToDocumentHashMappingEntities =
                documentHashes.map { documentHash ->
                    DataIdAndHashToEurodatIdMappingEntity(dataId, documentHash.key, documentHash.value)
                }
            dataIdToDocumentHashMappingEntities.forEach { mappingEntity ->
                dataIdAndHashToEurodatIdMappingRepository.save(mappingEntity)
            }
        }
    }

    /**
     * This method persists the metaDataInformation for the dataset with the specified dataId
     * @param dataId the dataId for which the metaDataInformation should be persisted
     * @param correlationId the correlationId of the storing process
     */
    fun persistMetaInfo(
        dataId: String,
        correlationId: String,
    ): DataMetaInformationEntity {
        logger.info(
            "Persisting meta info for dataId $dataId and correlationId $correlationId",
        )
        val dataMetaInfoEntityForDataId = metaInfoEntityInMemoryStorage[dataId]
        metaDataManager.setNewDatasetActiveAndOldDatasetInactive(dataMetaInfoEntityForDataId!!)
        return metaDataManager.storeDataMetaInformation(dataMetaInfoEntityForDataId)
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
        val rawValue =
            jsonDataInMemoryStorage.getOrElse(dataId) {
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
    fun getDocumentFromInMemoryStore(hash: String): ByteArray? = documentInMemoryStorage[hash]

    /**
     * Retrieves a private vsme data object from the private storage
     * @param dataId the dataId of the dataset to be retrieved
     * @param correlationId the correlationId of the request
     * @return the vsme dataset
     */
    fun getPrivateVsmeData(
        dataId: String,
        correlationId: String,
    ): VsmeData =
        objectMapper.readValue(
            dataManagerUtils
                .getStorableDataset(
                    dataId, DataType.of(VsmeData::class.java), correlationId,
                    ::getJsonStringFromCacheOrExternalStorage,
                ).data,
            VsmeData::class.java,
        )

    private fun getJsonStringFromCacheOrExternalStorage(
        dataId: String,
        correlationId: String,
    ): String =
        jsonDataInMemoryStorage[dataId] ?: dataManagerUtils
            .getDatasetAsJsonStringFromStorageService(dataId, correlationId, ::getJsonStringFromExternalStorage)

    private fun getJsonStringFromExternalStorage(
        dataId: String,
        correlationId: String,
    ): String =
        externalStorageDataGetter
            .getJsonFromExternalStorage(dataId, correlationId)

    /**
     * This method retrieves a document from the storage
     * @param dataId the dataId connected to document to be retrieved
     * @param hash the hash of the requested document
     * @param correlationId the correlationId of the request
     */
    fun retrievePrivateDocumentById(
        dataId: String,
        hash: String,
        correlationId: String,
    ): DocumentStream {
        val eurodatId =
            dataIdAndHashToEurodatIdMappingRepository.findByDataIdAndHash(dataId, hash)?.eurodatId
                ?: throw ResourceNotFoundApiException(
                    "No matching eurodatId found",
                    "Dataland cannot match the dataId $dataId and hash $hash to a eurodatId to retrieve the document " +
                        "from EuroDaT",
                )
        logger.info(
            "Matched dataId $dataId and hash $hash with eurodatId $eurodatId, CorrelationId $correlationId",
        )
        val inMemoryStoredDocument = documentInMemoryStorage[hash]
        return if (inMemoryStoredDocument != null) {
            logger.info("Received document for eurodatId $eurodatId from temporary storage")
            DocumentStream(hash, DocumentType.Pdf, InputStreamResource(ByteArrayInputStream(inMemoryStoredDocument)))
        } else {
            logger.info("Received document for eurodatId $eurodatId from storage service")
            DocumentStream(
                hash, DocumentType.Pdf,
                InputStreamResource(
                    externalStorageDataGetter
                        .getBlobFromExternalStorage(eurodatId, correlationId),
                ),
            )
        }
    }
}
