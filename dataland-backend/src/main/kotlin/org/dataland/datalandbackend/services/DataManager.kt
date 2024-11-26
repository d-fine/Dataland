package org.dataland.datalandbackend.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.entities.DataMetaInformationEntity
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.model.StorableDataSet
import org.dataland.datalandbackend.model.metainformation.NonSourceableInfo
import org.dataland.datalandbackend.utils.IdUtils
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandinternalstorage.openApiClient.api.StorageControllerApi
import org.dataland.datalandinternalstorage.openApiClient.infrastructure.ServerException
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.datalandmessagequeueutils.constants.ActionType
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.json.JSONObject
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional
import java.util.concurrent.ConcurrentHashMap

/**
 * Implementation of a data manager for Dataland including metadata storages
 * @param objectMapper object mapper used for converting data classes to strings and vice versa
 * @param companyQueryManager service for managing query regarding company data
 * @param metaDataManager service for managing metadata
 * @param storageClient service for managing data
 * @param cloudEventMessageHandler service for managing CloudEvents messages
 * @param dataManagerUtils holds util methods for handling of data
*/
@Component("DataManager")
class DataManager
    @Suppress("LongParameterList")
    constructor(
        @Autowired private val objectMapper: ObjectMapper,
        @Autowired private val companyQueryManager: CompanyQueryManager,
        @Autowired private val metaDataManager: DataMetaInformationManager,
        @Autowired private val storageClient: StorageControllerApi,
        @Autowired private val cloudEventMessageHandler: CloudEventMessageHandler,
        @Autowired private val dataManagerUtils: DataManagerUtils,
        @Autowired private val companyRoleChecker: CompanyRoleChecker,
        @Autowired private val nonSourceableDataManager: NonSourceableDataManager,
    ) {
        private val logger = LoggerFactory.getLogger(javaClass)
        private val logMessageBuilder = LogMessageBuilder()
        private val publicDataInMemoryStorage = ConcurrentHashMap<String, String>()

        /**
         * Method to make the data manager add data to a data store, store metadata in Dataland and sending messages to the
         * relevant message queues
         * @param storableDataSet contains all the inputs needed by Dataland
         * @param bypassQa whether the data should be sent to QA or not
         * @param correlationId the correlationId of the request
         * @return ID of the newly stored data in the data store
         */
        fun processDataStorageRequest(
            storableDataSet: StorableDataSet,
            bypassQa: Boolean,
            correlationId: String,
        ): String {
            if (bypassQa && !companyRoleChecker.canUserBypassQa(storableDataSet.companyId)) {
                throw AccessDeniedException(logMessageBuilder.bypassQaDeniedExceptionMessage)
            }
            val dataId = IdUtils.generateUUID()
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
        fun storeMetaDataFrom(
            dataId: String,
            storableDataSet: StorableDataSet,
            correlationId: String,
        ) {
            val company = dataManagerUtils.getCompanyByCompanyId(storableDataSet.companyId)
            logger.info(
                "Sending StorableDataSet of type ${storableDataSet.dataType} for company ID " +
                    "'${storableDataSet.companyId}', Company Name ${company.companyName} to storage Interface. " +
                    "Correlation ID: $correlationId",
            )

            val metaData =
                DataMetaInformationEntity(
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
            val nonSourceableInfo =
                NonSourceableInfo(
                    storableDataSet.companyId, storableDataSet.dataType,
                    storableDataSet.reportingPeriod, false, storableDataSet.uploaderUserId,
                )
            nonSourceableDataManager.storeSourceableData(nonSourceableInfo)
        }

        /**
         * This method retrieves public data from the temporary storage
         * @param dataId is the identifier for which all stored data entries in the temporary storage are filtered
         * @return stringified data entry from the temporary store
         */
        fun selectPublicDataSetFromTemporaryStorage(dataId: String): String {
            val rawValue =
                publicDataInMemoryStorage.getOrElse(dataId) {
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
            publicDataInMemoryStorage[dataId] = objectMapper.writeValueAsString(storableDataSet)
            val payload =
                JSONObject(
                    mapOf(
                        "dataId" to dataId, "bypassQa" to bypassQa,
                        "actionType" to
                            ActionType.STORE_PUBLIC_DATA,
                    ),
                ).toString()
            cloudEventMessageHandler.buildCEMessageAndSendToQueue(
                payload, MessageType.PUBLIC_DATA_RECEIVED, correlationId,
                ExchangeName.REQUEST_RECEIVED,
            )
            logger.info(
                "Stored StorableDataSet of type '${storableDataSet.dataType}' " +
                    "for company ID '${storableDataSet.companyId}' in temporary storage. " +
                    "Data ID '$dataId'. Correlation ID: '$correlationId'.",
            )
        }

        /**
         * Method to make the data manager get the data of a single entry from the data store
         * @param dataId to identify the stored data
         * @param dataType to check the correctness of the type of the retrieved data
         * @param correlationId to use in combination with dataId to retrieve data and assert type
         * @return data set associated with the data ID provided in the input
         */
        fun getPublicDataSet(
            dataId: String,
            dataType: DataType,
            correlationId: String,
        ): StorableDataSet =
            dataManagerUtils.getStorableDataset(
                dataId, dataType, correlationId,
                ::getJsonStringFromCacheOrInternalStorage,
            )

        private fun getJsonStringFromCacheOrInternalStorage(
            dataId: String,
            correlationId: String,
        ): String =
            publicDataInMemoryStorage[dataId] ?: dataManagerUtils
                .getDatasetAsJsonStringFromStorageService(
                    dataId,
                    correlationId, ::getJsonStringFromInternalStorage,
                )

        private fun getJsonStringFromInternalStorage(
            dataId: String,
            correlationId: String,
        ): String =
            storageClient
                .selectDataById(dataId, correlationId)

        /**
         * Method to check if a data set belongs to a teaser company and hence is publicly available
         * @param dataId the ID of the data set to be checked
         * @return a boolean signalling if the data is public or not
         */
        @Transactional(readOnly = true)
        fun isDataSetPublic(dataId: String): Boolean {
            val associatedCompanyId = metaDataManager.getDataMetaInformationByDataId(dataId).company.companyId
            return companyQueryManager.isCompanyPublic(associatedCompanyId)
        }

        /**
         * Method to remove a dataset from the dataland data store
         * @param dataId the dataId of the dataset to be removed
         * @param correlationId the correlationId of the deletion request
         */
        fun deleteCompanyAssociatedDataByDataId(
            dataId: String,
            correlationId: String,
        ) {
            try {
                metaDataManager.deleteDataMetaInfo(dataId)
                val payload =
                    JSONObject(
                        mapOf(
                            "dataId" to dataId, "bypassQa" to false,
                            "actionType" to
                                ActionType.DELETE_DATA,
                        ),
                    ).toString()
                cloudEventMessageHandler.buildCEMessageAndSendToQueue(
                    payload, MessageType.PUBLIC_DATA_RECEIVED, correlationId,
                    ExchangeName.REQUEST_RECEIVED,
                )
                logger.info(
                    "Received deletion request for dataset with DataId: " +
                        "$dataId with Correlation Id: $correlationId",
                )
            } catch (e: ServerException) {
                logger.error(
                    "Error deleting data. Received ServerException with Message:" +
                        " ${e.message}. Data ID: $dataId",
                )
                throw e
            }
        }

        /**
         * This method removes a dataset from the in memory storage
         * @param dataId the dataId of the dataset to be removed from the in-memory store
         */
        fun removeDataSetFromInMemoryStore(dataId: String) {
            publicDataInMemoryStorage.remove(dataId)
        }
    }
