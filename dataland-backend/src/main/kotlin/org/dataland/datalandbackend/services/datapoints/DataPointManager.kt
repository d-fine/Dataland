package org.dataland.datalandbackend.services.datapoints

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import org.dataland.datalandbackend.entities.DatasetDatapointEntity
import org.dataland.datalandbackend.model.StorableDataSet
import org.dataland.datalandbackend.model.datapoints.UploadedDataPoint
import org.dataland.datalandbackend.model.documents.CompanyReport
import org.dataland.datalandbackend.model.metainformation.DataPointMetaInformation
import org.dataland.datalandbackend.repositories.DatasetDatapointRepository
import org.dataland.datalandbackend.services.CompanyQueryManager
import org.dataland.datalandbackend.services.CompanyRoleChecker
import org.dataland.datalandbackend.services.DataManager
import org.dataland.datalandbackend.services.LogMessageBuilder
import org.dataland.datalandbackend.services.MessageQueuePublications
import org.dataland.datalandbackend.utils.DataPointValidator
import org.dataland.datalandbackend.utils.IdUtils
import org.dataland.datalandbackend.utils.JsonOperations
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.model.BasicDataPointDimensions
import org.dataland.datalandbackendutils.utils.JsonSpecificationLeaf
import org.dataland.datalandbackendutils.utils.JsonSpecificationUtils
import org.dataland.datalandinternalstorage.openApiClient.api.StorageControllerApi
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.dataland.specificationservice.openApiClient.api.SpecificationControllerApi
import org.dataland.specificationservice.openApiClient.infrastructure.ClientException
import org.dataland.specificationservice.openApiClient.model.FrameworkSpecificationDto
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.access.AccessDeniedException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import kotlin.jvm.optionals.getOrNull

/**
 * Class for managing data points and associated validations
 * @param dataManager service for handling data storage
 * @param metaDataManager service for handling data meta information
 */
@Suppress("LongParameterList")
@Service("DataPointManager")
class DataPointManager
    @Autowired
    constructor(
        private val dataManager: DataManager,
        private val metaDataManager: DataPointMetaInformationManager,
        private val storageClient: StorageControllerApi,
        private val messageQueuePublications: MessageQueuePublications,
        private val dataPointValidator: DataPointValidator,
        private val companyQueryManager: CompanyQueryManager,
        private val companyRoleChecker: CompanyRoleChecker,
        private val objectMapper: ObjectMapper,
        private val logMessageBuilder: LogMessageBuilder,
        private val specificationClient: SpecificationControllerApi,
        private val datasetDatapointRepository: DatasetDatapointRepository,
    ) {
        private val logger = LoggerFactory.getLogger(javaClass)

        /**
         * Processes a single data point by validating it and storing it in the internal storage
         * @param uploadedDataPoint the data point to process
         * @param uploaderUserId the user id of the user who uploaded the data point
         * @param bypassQa whether to bypass the QA process
         * @param correlationId the correlation id for the operation
         * @return the meta information of the stored data point
         */
        fun processDataPoint(
            uploadedDataPoint: UploadedDataPoint,
            uploaderUserId: String,
            bypassQa: Boolean,
            correlationId: String,
        ): DataPointMetaInformation {
            dataPointValidator.validateDataPoint(uploadedDataPoint.dataPointIdentifier, uploadedDataPoint.dataPointContent, correlationId)
            logger.info("Storing '${uploadedDataPoint.dataPointIdentifier}' data point with bypassQa set to: $bypassQa.")
            val dataId = IdUtils.generateUUID()

            if (bypassQa && !companyRoleChecker.canUserBypassQa(uploadedDataPoint.companyId)) {
                throw AccessDeniedException(logMessageBuilder.bypassQaDeniedExceptionMessage)
            }

            val dataPointMetaInformation = storeDataPoint(uploadedDataPoint, dataId, uploaderUserId, correlationId)
            messageQueuePublications.publishDataPointUploadedMessage(dataId, bypassQa, correlationId)
            return dataPointMetaInformation
        }

        /**
         * Stores a single data point in the internal storage
         * @param dataId the ID of the data point
         * @param uploadedDataPoint the data point to store
         * @param uploaderUserId the user id of the user who uploaded the data point
         * @param correlationId the correlation id for the operation
         * @return the id of the stored data point
         */
        fun storeDataPoint(
            uploadedDataPoint: UploadedDataPoint,
            dataId: String,
            uploaderUserId: String,
            correlationId: String,
        ): DataPointMetaInformation {
            val dataPointMetaInformationEntity = uploadedDataPoint.toDataPointMetaInformationEntity(dataId, uploaderUserId)
            metaDataManager.storeDataPointMetaInformation(dataPointMetaInformationEntity)
            dataManager.storeDataInTemporaryStorage(dataId, objectMapper.writeValueAsString(uploadedDataPoint), correlationId)

            return dataPointMetaInformationEntity.toApiModel(DatalandAuthentication.fromContextOrNull())
        }

        /**
         * Checks if a company is associated with a data point marked for public access.
         * @param dataId the id of the data point
         * @return true if the company is associated with the data point, false otherwise
         */
        fun isCompanyAssociatedWithDataPointMarkedForPublicAccess(dataId: String): Boolean {
            val metaInfo = metaDataManager.getDataPointMetaInformationByDataId(dataId)
            return companyQueryManager.isCompanyPublic(metaInfo.companyId)
        }

        /**
         * Retrieves a single data point from the internal storage
         * @param dataId the id of the data point
         * @param correlationId the correlation id for the operation
         * @return the data point in form of a StorableDataSet
         */
        fun retrieveDataPoint(
            dataId: String,
            correlationId: String,
        ): UploadedDataPoint {
            val metaInfo = metaDataManager.getDataPointMetaInformationByDataId(dataId)
            if (!metaInfo.isDatasetViewableByUser(DatalandAuthentication.fromContextOrNull())) {
                throw AccessDeniedException(logMessageBuilder.generateAccessDeniedExceptionMessage(metaInfo.qaStatus))
            }
            val dataPointIdentifier = metaInfo.dataPointIdentifier
            logger.info("Retrieving $dataPointIdentifier data point with id $dataId (correlation ID: $correlationId).")
            dataPointValidator.validateDataPointIdentifierExists(dataPointIdentifier)

            val storedDataPoint = storageClient.selectDataPointById(dataId, correlationId)
            return UploadedDataPoint(
                dataPointContent = storedDataPoint.dataPointContent,
                dataPointIdentifier = storedDataPoint.dataPointIdentifier,
                companyId = storedDataPoint.companyId,
                reportingPeriod = storedDataPoint.reportingPeriod,
            )
        }

        /**
         * Update the currently active data point for specific data point dimensions
         * @param dataPointDimensions the data point dimension to update the currently active data point for
         * @param newActiveDataId the id of the new active data point
         * @param correlationId the correlation id for the operation
         */
        fun updateCurrentlyActiveDataPoint(
            dataPointDimensions: BasicDataPointDimensions,
            newActiveDataId: String?,
            correlationId: String,
        ) {
            logger.info("Updating currently active data point for $dataPointDimensions (correlation ID: $correlationId).")
            val currentlyActiveDataId = metaDataManager.getCurrentlyActiveDataId(dataPointDimensions)
            logger.info("Currently and newly active IDs are $currentlyActiveDataId and $newActiveDataId (correlation ID: $correlationId).")
            if (newActiveDataId.isNullOrEmpty() && !currentlyActiveDataId.isNullOrEmpty()) {
                logger.info("Setting data point with dataId $currentlyActiveDataId to inactive (correlation ID: $correlationId).")
                metaDataManager.updateCurrentlyActiveFlagOfDataPoint(currentlyActiveDataId, null)
            } else if (newActiveDataId != currentlyActiveDataId) {
                logger.info("Setting $newActiveDataId to active and $currentlyActiveDataId to inactive (correlation ID: $correlationId).")
                metaDataManager.updateCurrentlyActiveFlagOfDataPoint(currentlyActiveDataId, null)
                metaDataManager.updateCurrentlyActiveFlagOfDataPoint(newActiveDataId, true)
            } else {
                logger.info("No update of the currently active flag required (correlation ID: $correlationId).")
            }
        }

        /**
         * Retrieves all data point frameworks from the specification service
         * @return a list of the names of all data point frameworks
         */
        fun getAllDataPointFrameworks(): List<String> =
            specificationClient.listFrameworkSpecifications().map {
                it.frameworkSpecification.id
            }

        /**
         * Processes a data set by breaking it up and storing its data points in the internal storage
         * @param uploadedDataSet the data set to process
         * @param bypassQa whether to bypass the QA process
         * @param correlationId the correlation id for the operation
         * @return the id of the stored data set
         */
        fun processDataSet(
            uploadedDataSet: StorableDataSet,
            bypassQa: Boolean,
            correlationId: String,
        ): String {
            val frameworkSpecification = getFrameworkSpecification(uploadedDataSet.dataType.toString())
            val frameworkTemplate = JsonOperations.getJsonNodeFromString(frameworkSpecification.schema)
            val companyId = uploadedDataSet.companyId

            val dataContent =
                JsonSpecificationUtils
                    .dehydrateJsonSpecification(
                        frameworkTemplate as ObjectNode,
                        objectMapper.readTree(uploadedDataSet.data) as ObjectNode,
                        frameworkSpecification.referencedReportJsonPath,
                    ).toMutableMap()

            val fileReferenceToPublicationDateMapping = mutableMapOf<String, LocalDate>()
            if (dataContent.containsKey(JsonSpecificationUtils.REFERENCED_REPORTS_ID)) {
                fileReferenceToPublicationDateMapping +=
                    JsonOperations.getFileReferenceToPublicationDateMapping(dataContent[JsonSpecificationUtils.REFERENCED_REPORTS_ID])
                dataContent.remove(JsonSpecificationUtils.REFERENCED_REPORTS_ID)
            }

            valideDataSet(dataContent, correlationId)

            val datasetId = IdUtils.generateUUID()
            dataManager.storeMetaDataFrom(datasetId, uploadedDataSet, correlationId)
            messageQueuePublications.publishDataSetQaRequiredMessage(datasetId, bypassQa, correlationId)

            logger.info("Processing data set with id $datasetId for framework ${uploadedDataSet.dataType}")

            val createdDataIds = mutableMapOf<String, String>()
            dataContent.forEach { (dataPointIdentifier, dataPointJsonLeaf) ->
                val dataPointContent = dataPointJsonLeaf.content
                if (dataPointContent.isEmpty) return@forEach

                JsonOperations.updatePublicationDateInJsonNode(
                    dataPointContent,
                    fileReferenceToPublicationDateMapping,
                    "dataSource",
                )
                logger.info(
                    "Storing value found for $dataPointIdentifier " +
                        "under ${dataPointJsonLeaf.jsonPath} (correlation ID: $correlationId)",
                )

                val dataId = IdUtils.generateUUID()
                createdDataIds[dataPointIdentifier] = dataId
                storeDataPoint(
                    UploadedDataPoint(
                        dataPointContent = JsonOperations.objectMapper.writeValueAsString(dataPointContent),
                        dataPointIdentifier = dataPointIdentifier,
                        companyId = companyId,
                        reportingPeriod = uploadedDataSet.reportingPeriod,
                    ),
                    dataId,
                    uploadedDataSet.uploaderUserId,
                    correlationId,
                )
                messageQueuePublications.publishDataPointUploadedMessage(dataId, bypassQa, correlationId)
            }
            this.datasetDatapointRepository.save(
                DatasetDatapointEntity(datasetId = datasetId, dataPoints = createdDataIds),
            )
            logger.info("Completed processing data set (correlation ID: $correlationId).")
            return datasetId
        }

        private fun valideDataSet(
            datasetContent: Map<String, JsonSpecificationLeaf>,
            correlationId: String,
        ) {
            datasetContent.forEach { (dataPointIdentifier, dataPointJsonLeaf) ->
                val dataPointContent = JsonOperations.objectMapper.writeValueAsString(dataPointJsonLeaf.content)
                if (dataPointContent.isEmpty()) return@forEach
                dataPointValidator.validateDataPoint(dataPointIdentifier, dataPointContent, correlationId)
            }
        }

        private fun getFrameworkSpecification(framework: String): FrameworkSpecificationDto =
            try {
                specificationClient.getFrameworkSpecification(framework)
            } catch (clientException: ClientException) {
                logger.error("Framework $framework not found: ${clientException.message}.")
                throw InvalidInputApiException(
                    "Framework $framework not found.",
                    "The specified framework $framework is not known to the specification service.",
                )
            }

        /**
         * Retrieves a data set by assembling the data points from the internal storage
         * @param datasetId the id of the data set
         * @param framework the type of data set
         * @param correlationId the correlation id for the operation
         * @return the data set in form of a JSON string
         */
        @Transactional(readOnly = true)
        fun getDataSetFromId(
            datasetId: String,
            framework: String,
            correlationId: String,
        ): String {
            val dataPoints = getDataPointIdsForDataSet(datasetId)
            return assembleDataSetFromDataPoints(dataPoints.values.toList(), framework, correlationId)
        }

        /**
         * Retrieves a key value map containing the IDs of the data points contained in a dataset mapped to their technical IDs
         * @param datasetId the ID of the dataset
         * @return a map of data point IDs to the corresponding technical IDs that are contained in the dataset
         */
        @Transactional(readOnly = true)
        fun getDataPointIdsForDataSet(datasetId: String): Map<String, String> {
            val dataPoints =
                datasetDatapointRepository
                    .findById(datasetId)
                    .getOrNull()
                    ?.dataPoints
                    ?: emptyMap()
            return dataPoints
        }

        /**
         * Assembles a data set by retrieving the data points from the internal storage
         * and filling their content into the framework template
         * @param dataIds a list of all required data points
         * @param framework the type of data set
         * @param correlationId the correlation id for the operation
         * @return the data set in form of a JSON string
         */
        private fun assembleDataSetFromDataPoints(
            dataIds: List<String>,
            framework: String,
            correlationId: String,
        ): String {
            val frameworkSpecification = getFrameworkSpecification(framework)
            val frameworkTemplate = JsonOperations.getJsonNodeFromString(frameworkSpecification.schema)
            val referencedReportsPath = frameworkSpecification.referencedReportJsonPath

            val referencedReports = mutableMapOf<String, CompanyReport>()
            val allDataPoints = mutableMapOf<String, JsonNode>()

            dataIds.forEach { dataId ->
                val dataPoint = retrieveDataPoint(dataId, correlationId)
                allDataPoints[dataPoint.dataPointIdentifier] = JsonOperations.getJsonNodeFromString(dataPoint.dataPointContent)
                val companyReport = JsonOperations.getCompanyReportFromDataSource(dataPoint.dataPointContent)
                if (companyReport != null) {
                    referencedReports[companyReport.fileName ?: companyReport.fileReference] = companyReport
                }
            }

            val datasetAsJsonNode = JsonSpecificationUtils.hydrateJsonSpecification(frameworkTemplate as ObjectNode) { allDataPoints[it] }
            if (referencedReportsPath != null && datasetAsJsonNode != null) {
                logger.info("Inserting referenced reports (correlation ID $correlationId).")
                JsonOperations.insertReferencedReports(datasetAsJsonNode, referencedReportsPath, referencedReports)
            }

            return datasetAsJsonNode.toString()
        }
    }
