package org.dataland.datalandbackend.services.datapoints

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import org.dataland.datalandbackend.entities.DatasetDatapointEntity
import org.dataland.datalandbackend.model.StorableDataSet
import org.dataland.datalandbackend.model.datapoints.UploadedDataPoint
import org.dataland.datalandbackend.model.documents.CompanyReport
import org.dataland.datalandbackend.repositories.DatasetDatapointRepository
import org.dataland.datalandbackend.services.DataManager
import org.dataland.datalandbackend.services.DatasetStorageService
import org.dataland.datalandbackend.services.MessageQueuePublications
import org.dataland.datalandbackend.utils.DataPointValidator
import org.dataland.datalandbackend.utils.IdUtils
import org.dataland.datalandbackend.utils.ReferencedReportsUtilities
import org.dataland.datalandbackend.utils.ReferencedReportsUtilities.REFERENCED_REPORTS_ID
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.utils.JsonSpecificationLeaf
import org.dataland.datalandbackendutils.utils.JsonSpecificationUtils
import org.dataland.specificationservice.openApiClient.api.SpecificationControllerApi
import org.dataland.specificationservice.openApiClient.infrastructure.ClientException
import org.dataland.specificationservice.openApiClient.model.FrameworkSpecificationDto
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import kotlin.jvm.optionals.getOrNull

/**
 * Manages datasets assembled from multiple data points
 */
@Suppress("LongParameterList")
@Service("AssembledDataManager")
class AssembledDataManager
    @Autowired
    constructor(
        private val dataManager: DataManager,
        private val messageQueuePublications: MessageQueuePublications,
        private val dataPointValidator: DataPointValidator,
        private val objectMapper: ObjectMapper,
        private val specificationClient: SpecificationControllerApi,
        private val datasetDatapointRepository: DatasetDatapointRepository,
        private val dataPointManager: DataPointManager,
    ) : DatasetStorageService {
        private val logger = LoggerFactory.getLogger(javaClass)

        /**
         * Processes a data set by breaking it up and storing its data points in the internal storage
         * @param uploadedDataSet the data set to process
         * @param bypassQa whether to bypass the QA process
         * @param correlationId the correlation id for the operation
         * @return the id of the stored data set
         */
        override fun storeDataset(
            uploadedDataSet: StorableDataSet,
            bypassQa: Boolean,
            correlationId: String,
        ): String {
            val frameworkSpecification = getFrameworkSpecification(uploadedDataSet.dataType.toString())
            val frameworkTemplate = ReferencedReportsUtilities.getJsonNodeFromString(frameworkSpecification.schema)
            ReferencedReportsUtilities.insertReferencedReports(frameworkTemplate, frameworkSpecification.referencedReportJsonPath)
            val companyId = uploadedDataSet.companyId

            val dataContent =
                JsonSpecificationUtils
                    .dehydrateJsonSpecification(
                        frameworkTemplate as ObjectNode,
                        objectMapper.readTree(uploadedDataSet.data) as ObjectNode,
                    ).toMutableMap()

            val fileReferenceToPublicationDateMapping = mutableMapOf<String, LocalDate>()
            if (dataContent.containsKey(REFERENCED_REPORTS_ID)) {
                fileReferenceToPublicationDateMapping +=
                    ReferencedReportsUtilities.getFileReferenceToPublicationDateMapping(dataContent[REFERENCED_REPORTS_ID])
                dataContent.remove(REFERENCED_REPORTS_ID)
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

                ReferencedReportsUtilities.updatePublicationDateInJsonNode(
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
                dataPointManager.storeDataPoint(
                    UploadedDataPoint(
                        dataPointContent = ReferencedReportsUtilities.objectMapper.writeValueAsString(dataPointContent),
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
                val dataPointContent = ReferencedReportsUtilities.objectMapper.writeValueAsString(dataPointJsonLeaf.content)
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
         * @param dataType the type of data set
         * @param correlationId the correlation id for the operation
         * @return the data set in form of a JSON string
         */
        @Transactional(readOnly = true)
        override fun getDatasetData(
            datasetId: String,
            dataType: String,
            correlationId: String,
        ): String {
            val dataPoints = getDataPointIdsForDataSet(datasetId)
            return assembleDataSetFromDataPoints(dataPoints.values.toList(), dataType, correlationId)
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
            val frameworkTemplate = ReferencedReportsUtilities.getJsonNodeFromString(frameworkSpecification.schema)
            ReferencedReportsUtilities.insertReferencedReports(frameworkTemplate, frameworkSpecification.referencedReportJsonPath)

            val referencedReports = mutableMapOf<String, CompanyReport>()
            val allDataPoints = mutableMapOf<String, JsonNode>()

            dataIds.forEach { dataId ->
                val dataPoint = dataPointManager.retrieveDataPoint(dataId, correlationId)
                allDataPoints[dataPoint.dataPointIdentifier] = ReferencedReportsUtilities.getJsonNodeFromString(dataPoint.dataPointContent)
                val companyReport = ReferencedReportsUtilities.getCompanyReportFromDataSource(dataPoint.dataPointContent)
                if (companyReport != null) {
                    referencedReports[companyReport.fileName ?: companyReport.fileReference] = companyReport
                }
            }
            allDataPoints[REFERENCED_REPORTS_ID] =
                ReferencedReportsUtilities.objectMapper.valueToTree(referencedReports)

            val datasetAsJsonNode = JsonSpecificationUtils.hydrateJsonSpecification(frameworkTemplate as ObjectNode) { allDataPoints[it] }

            return datasetAsJsonNode.toString()
        }
    }
