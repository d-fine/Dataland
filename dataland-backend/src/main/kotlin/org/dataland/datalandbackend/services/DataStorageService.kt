package org.dataland.datalandbackend.services

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import org.dataland.datalandbackend.entities.DataMetaInformationEntity
import org.dataland.datalandbackend.entities.DatasetDatapointEntity
import org.dataland.datalandbackend.model.StorableDataset
import org.dataland.datalandbackend.model.datapoints.UploadedDataPoint
import org.dataland.datalandbackend.model.documents.CompanyReport
import org.dataland.datalandbackend.model.metainformation.DataPointMetaInformation
import org.dataland.datalandbackend.repositories.DatasetDatapointRepository
import org.dataland.datalandbackend.services.CompanyQueryManager
import org.dataland.datalandbackend.services.DataManager
import org.dataland.datalandbackend.services.DatasetStorageService
import org.dataland.datalandbackend.services.MessageQueuePublications
import org.dataland.datalandbackend.services.datapoints.DataPointManager
import org.dataland.datalandbackend.services.datapoints.DataPointMetaInformationManager
import org.dataland.datalandbackend.utils.DataPointUtils
import org.dataland.datalandbackend.utils.DataPointValidator
import org.dataland.datalandbackend.utils.IdUtils
import org.dataland.datalandbackend.utils.ReferencedReportsUtilities
import org.dataland.datalandbackend.utils.ReferencedReportsUtilities.Companion.REFERENCED_REPORTS_ID
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandbackendutils.utils.JsonComparator
import org.dataland.datalandbackendutils.utils.JsonSpecificationLeaf
import org.dataland.datalandbackendutils.utils.JsonSpecificationUtils
import org.dataland.datalandbackendutils.utils.QaBypass
import org.dataland.datalandinternalstorage.openApiClient.api.StorageControllerApi
import org.dataland.datalandmessagequeueutils.messages.data.InitialQaStatus
import org.dataland.datalandmessagequeueutils.messages.data.PresetQaStatus
import org.dataland.specificationservice.openApiClient.api.SpecificationControllerApi
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.LocalDate
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.set

/**
 * Service to determine the category of a given data type string, relevant constituents of datasets and similar tasks
 */
@Service("DataCompositionService")
class DataStorageService
    @Autowired
    constructor(
        private val companyRoleChecker: CompanyRoleChecker,
        private val objectMapper: ObjectMapper,
        private val datasetDatapointRepository: DatasetDatapointRepository,
        private val specificationControllerApi: SpecificationControllerApi,
        private val companyQueryManager: CompanyQueryManager,
        private val metaDataManager: DataMetaInformationManager,
        private val storageClient: StorageControllerApi,
        private val dataManagerUtils: DataManagerUtils,
        private val messageQueuePublications: MessageQueuePublications,
        private val dataManager: DataManager,
        private val dataPointValidator: DataPointValidator,
        private val dataPointManager: DataPointManager,
        private val referencedReportsUtilities: ReferencedReportsUtilities,
        private val companyManager: CompanyQueryManager,
        private val dataPointUtils: DataPointUtils,
        private val dataPointMetaDataManager: DataPointMetaInformationManager,
        private val logMessageBuilder: LogMessageBuilder,
    ) {
        fun storeDataset() {}

        fun storeDataPoint() {}

        fun storeAssembledDataset() {}

        fun storeDatasetMetaInformation() {}

        fun storeDataPointMetaInformation() {}

        fun splitAssembledDatasetIntoDataPoints() {}

        fun publishDatasetStoredMessage() {}

        fun publishDataPointStoredMessage() {}

        private val logger = LoggerFactory.getLogger(javaClass)
        private val publicDataInMemoryStorage = ConcurrentHashMap<String, String>()

        /**
         * Method to make the data manager add data to a data store, store metadata in Dataland and sending messages to the
         * relevant message queues
         * @param uploadedDataset contains all the inputs needed by Dataland
         * @param bypassQa whether the data should be sent to QA or not
         * @param correlationId the correlationId of the request
         * @return ID of the newly stored data in the data store
         */
        fun storeNonAssembledDataset(
            // revisit this input
            uploadedDataset: StorableDataset,
            bypassQa: Boolean,
            correlationId: String,
        ): String {
            val dataId = IdUtils.generateUUID()
            storeDatasetMetaData(dataId, uploadedDataset, correlationId)
            logger.info(
                "Storing data of type '${uploadedDataset.dataType}' for company ID '${uploadedDataset.companyId}'" +
                    " in temporary storage. Data ID '$dataId'. Correlation ID: '$correlationId'.",
            )
            storeDataInTemporaryStorage(dataId, objectMapper.writeValueAsString(uploadedDataset))
            messageQueuePublications.publishDatasetUploadedMessage(dataId, bypassQa, correlationId)
            return dataId
        }

        /**
         * Persists the data meta-information to the database and the updates the data source history
         * in the database if necessary ensuring that the database transaction ends directly after this
         * function returns so that a MQ-Message might be sent out after this function completes
         * @param dataId The dataId of the dataset to store
         * @param storableDataset the dataset to store
         * @param correlationId the correlation id of the insertion process
         */
        fun storeDatasetMetaData(
            dataId: String,
            storableDataset: StorableDataset,
            correlationId: String,
        ) {
            val company = dataManagerUtils.getCompanyByCompanyId(storableDataset.companyId)
            logger.info(
                "Sending StorableDataset of type ${storableDataset.dataType} for company ID " +
                    "'${storableDataset.companyId}', Company Name ${company.companyName} to storage Interface. " +
                    "Correlation ID: $correlationId",
            )

            val metaData =
                DataMetaInformationEntity(
                    dataId,
                    company,
                    storableDataset.dataType.toString(),
                    storableDataset.uploaderUserId,
                    storableDataset.uploadTime,
                    storableDataset.reportingPeriod,
                    null,
                    QaStatus.Pending,
                )
            metaDataManager.storeDataMetaInformation(metaData)
        }

        /**
         * Store data in the temporary storage
         * @param dataId the id of the data
         * @param data the data to store as a string
         */
        fun storeDataInTemporaryStorage(
            dataId: String,
            data: String,
        ) {
            publicDataInMemoryStorage[dataId] = data
        }

        /**
         * This method retrieves public data from the temporary storage
         * @param dataId is the identifier for which all stored data entries in the temporary storage are filtered
         * @return stringified data entry from the temporary store
         */
        fun retrieveDataFromTemporaryStorage(dataId: String): String =
            publicDataInMemoryStorage[dataId] ?: throw ResourceNotFoundApiException(
                "Data ID not found in temporary storage",
                "Dataland does not know the data id $dataId",
            )

        /**
         * Method to get data from the cache or the internal storage
         */
        fun getDataFromCache(dataId: String): String? = publicDataInMemoryStorage[dataId]

        /**
         * This method removes a dataset from the in memory storage
         * @param dataId the dataId of the dataset to be removed from the in-memory store
         */
        fun removeDatasetFromInMemoryStore(dataId: String) {
            publicDataInMemoryStorage.remove(dataId)
        }

        /**
         * Processes a dataset by breaking it up and storing its data points in the internal storage
         * @param uploadedDataset the dataset to process
         * @param bypassQa whether to bypass the QA process
         * @param correlationId the correlation id for the operation
         * @return the id of the stored dataset
         */
        @Transactional
        fun storeAssembledDataset(
            uploadedDataset: StorableDataset,
            bypassQa: Boolean,
            correlationId: String,
        ): String {
            val (dataContent, referencedReports, fileReferenceToPublicationDateMapping, fileReferenceToFileNameMapping) =
                splitDatasetIntoDataPoints(uploadedDataset.data, uploadedDataset.dataType.toString())
            dataPointValidator.validateDataset(dataContent, referencedReports, correlationId)
            return storeSplitDataset(
                uploadedDataset, correlationId, bypassQa, dataContent, fileReferenceToPublicationDateMapping,
                fileReferenceToFileNameMapping,
            )
        }

        /**
         * Data class to store a dataset intermediately after it has been split into data points
         */
        data class SplitDataset(
            val dataContent: Map<String, JsonSpecificationLeaf>,
            val referencedReports: Map<String, CompanyReport>?,
            val fileReferenceToPublicationDateMapping: Map<String, LocalDate>,
            val fileReferenceToFileNameMapping: Map<String, String>,
        )

        /**
         * Splits the dataset into individual data points
         * @param data the dataset to split
         * @param dataType the type of the dataset
         * @return the split dataset
         */
        fun splitDatasetIntoDataPoints(
            data: String,
            dataType: String,
        ): SplitDataset {
            val frameworkSpecification = dataPointUtils.getFrameworkSpecification(dataType)
            val frameworkSchema = objectMapper.readTree(frameworkSpecification.schema) as ObjectNode
            val frameworkUsesReferencedReports = frameworkSpecification.referencedReportJsonPath != null

            referencedReportsUtilities
                .insertReferencedReportsIntoFrameworkSchema(
                    frameworkSchema,
                    frameworkSpecification.referencedReportJsonPath,
                )

            val dataContent =
                JsonSpecificationUtils
                    .dehydrateJsonSpecification(
                        frameworkSchema,
                        objectMapper.readTree(data) as ObjectNode,
                    ).toMutableMap()

            val referencedReports =
                if (frameworkUsesReferencedReports) {
                    referencedReportsUtilities.parseReferencedReportsFromJsonLeaf(
                        dataContent[REFERENCED_REPORTS_ID],
                    )
                } else {
                    null
                }

            dataContent.remove(REFERENCED_REPORTS_ID)
            val fileReferenceToPublicationDateMapping =
                referencedReports
                    ?.values
                    ?.filter { it.publicationDate != null }
                    ?.associate { it.fileReference to it.publicationDate!! }
                    ?: emptyMap()

            val fileReferenceToFileNameMapping =
                referencedReports
                    ?.values
                    ?.filter { it.fileName != null }
                    ?.associate { it.fileReference to it.fileName!! }
                    ?: emptyMap()

            return SplitDataset(
                dataContent,
                referencedReports,
                fileReferenceToPublicationDateMapping,
                fileReferenceToFileNameMapping,
            )
        }

        /**
         * Stores an individual data point in the internal storage
         * @param dataPointJsonLeaf the data point to store
         * @param fileReferenceToPublicationDateMapping a mapping of file references to publication dates
         * @param dataPointType the type of the data point
         * @param correlationId the correlation id for the operation
         * @param uploadedDataset the dataset the data point belongs to
         * @return the id of the stored data point
         */
        private fun storeIndividualDataPoint(
            dataPointJsonLeaf: JsonSpecificationLeaf,
            fileReferenceToPublicationDateMapping: Map<String, LocalDate>,
            fileReferenceToFileNameMapping: Map<String, String>,
            dataPointType: String,
            correlationId: String,
            uploadedDataset: StorableDataset,
        ): DataPointMetaInformation? {
            val dataPoint = dataPointJsonLeaf.content
            if (JsonComparator.isFullyNullObject(dataPoint)) return null

            referencedReportsUtilities.updateJsonNodeWithDataFromReferencedReports(
                dataPoint,
                fileReferenceToPublicationDateMapping,
                fileReferenceToFileNameMapping,
                "dataSource",
            )
            logger.info(
                "Storing value found for $dataPointType " +
                    "under ${dataPointJsonLeaf.jsonPath} (correlation ID: $correlationId)",
            )

            val dataPointId = IdUtils.generateUUID()
            val metaInfo =
                dataPointManager.storeDataPoint(
                    uploadedDataPoint =
                        UploadedDataPoint(
                            dataPoint = objectMapper.writeValueAsString(dataPoint),
                            dataPointType = dataPointType,
                            companyId = uploadedDataset.companyId,
                            reportingPeriod = uploadedDataset.reportingPeriod,
                        ),
                    dataPointId = dataPointId,
                    uploaderUserId = uploadedDataset.uploaderUserId,
                    uploadTime = uploadedDataset.uploadTime,
                    correlationId = correlationId,
                )
            return metaInfo
        }

        /**
         * Creates all individual data points and associated them with the data points
         */
        @Suppress("kotlin:S107")
        @Transactional
        fun storeDataPointsForDataset(
            datasetId: String,
            dataContent: Map<String, JsonSpecificationLeaf>,
            fileReferenceToPublicationDateMapping: Map<String, LocalDate>,
            fileReferenceToFileNameMapping: Map<String, String>,
            uploadedDataset: StorableDataset,
            correlationId: String,
            initialQa: InitialQaStatus,
        ) {
            logger.info("Processing dataset with id $datasetId (correlation ID: $correlationId).")
            val companyInformation = companyManager.getCompanyById(uploadedDataset.companyId).toApiModel()

            val createdDataIds = mutableMapOf<String, String>()
            dataContent.forEach { (dataPointType, dataPointJsonLeaf) ->
                storeIndividualDataPoint(
                    dataPointJsonLeaf = dataPointJsonLeaf,
                    fileReferenceToPublicationDateMapping = fileReferenceToPublicationDateMapping,
                    fileReferenceToFileNameMapping = fileReferenceToFileNameMapping,
                    dataPointType = dataPointType,
                    correlationId = correlationId,
                    uploadedDataset = uploadedDataset,
                )?.let {
                    messageQueuePublications.publishDataPointUploadedMessage(
                        dataPointMetaInformation = it,
                        companyInformation = companyInformation,
                        initialQa = initialQa,
                        correlationId = correlationId,
                    )
                    createdDataIds[dataPointType] = it.dataPointId
                }
            }
            this.datasetDatapointRepository.save(
                DatasetDatapointEntity(datasetId = datasetId, dataPoints = createdDataIds),
            )
            logger.info("Completed processing dataset (correlation ID: $correlationId).")
        }

        /**
         * Stores the individual data points contained in the dataset in the internal storage
         * @param uploadedDataset the dataset to store
         * @param correlationId the correlation id for the operation
         * @param bypassQa whether to bypass the QA process
         * @param dataContent the content of the dataset
         * @param fileReferenceToPublicationDateMapping a mapping of file references to publication dates
         * @return the id of the dataset
         */
        private fun storeSplitDataset(
            uploadedDataset: StorableDataset,
            correlationId: String,
            bypassQa: Boolean,
            dataContent: Map<String, JsonSpecificationLeaf>,
            fileReferenceToPublicationDateMapping: Map<String, LocalDate>,
            fileReferenceToFileNameMapping: Map<String, String>,
        ): String {
            val datasetId = IdUtils.generateUUID()
            dataManager.storeMetaDataFrom(datasetId, uploadedDataset, correlationId)
            messageQueuePublications.publishDatasetQaRequiredMessage(datasetId, bypassQa, correlationId)

            val (qaStatus, comment) = QaBypass.getCommentAndStatusForBypass(bypassQa)

            storeDataPointsForDataset(
                datasetId = datasetId,
                dataContent = dataContent,
                fileReferenceToPublicationDateMapping = fileReferenceToPublicationDateMapping,
                fileReferenceToFileNameMapping = fileReferenceToFileNameMapping,
                uploadedDataset = uploadedDataset,
                correlationId = correlationId,
                initialQa =
                    PresetQaStatus(
                        qaStatus = qaStatus,
                        qaComment = comment,
                    ),
            )

            return datasetId
        }

        /**
         * Processes a single data point by validating it and storing it in the internal storage
         * @param uploadedDataPoint the data point to process
         * @param uploaderUserId the user id of the user who uploaded the data point
         * @param bypassQa whether to bypass the QA process
         * @param correlationId the correlation id for the operation
         * @return the meta information of the stored data point
         */
        @Transactional
        fun processDataPoint(
            uploadedDataPoint: UploadedDataPoint,
            uploaderUserId: String,
            bypassQa: Boolean,
            correlationId: String,
        ): DataPointMetaInformation {
            dataPointValidator.validateDataPoint(uploadedDataPoint.dataPointType, uploadedDataPoint.dataPoint, correlationId)
            logger.info("Storing '${uploadedDataPoint.dataPointType}' data point with bypassQa set to: $bypassQa.")
            val dataPointId = IdUtils.generateUUID()

            if (bypassQa && !companyRoleChecker.canUserBypassQa(uploadedDataPoint.companyId)) {
                throw AccessDeniedException(logMessageBuilder.bypassQaDeniedExceptionMessage)
            }

            val companyInformation =
                companyQueryManager
                    .getCompanyById(uploadedDataPoint.companyId)
                    .toApiModel()

            val dataPointMetaInformation =
                storeDataPoint(
                    uploadedDataPoint = uploadedDataPoint,
                    dataPointId = dataPointId,
                    uploaderUserId = uploaderUserId,
                    correlationId = correlationId,
                    uploadTime = Instant.now().toEpochMilli(),
                )
            messageQueuePublications.publishDataPointUploadedMessageWithBypassQa(
                dataPointMetaInformation = dataPointMetaInformation,
                companyInformation = companyInformation,
                bypassQa = bypassQa,
                correlationId = correlationId,
            )
            return dataPointMetaInformation
        }

        /**
         * Stores a single data point in the internal storage
         * @param dataPointId the ID of the data point
         * @param uploadedDataPoint the data point to store
         * @param uploaderUserId the user id of the user who uploaded the data point
         * @param correlationId the correlation id for the operation
         * @return the id of the stored data point
         */
        @Transactional
        fun storeDataPoint(
            uploadedDataPoint: UploadedDataPoint,
            dataPointId: String,
            uploaderUserId: String,
            uploadTime: Long,
            correlationId: String,
        ): DataPointMetaInformation {
            val dataPointMetaInformationEntity = uploadedDataPoint.toDataPointMetaInformationEntity(dataPointId, uploaderUserId, uploadTime)
            dataPointMetaDataManager.storeDataPointMetaInformation(dataPointMetaInformationEntity)
            dataManager.storeDataInTemporaryStorage(dataPointId, objectMapper.writeValueAsString(uploadedDataPoint), correlationId)

            return dataPointMetaInformationEntity.toApiModel()
        }
    }
