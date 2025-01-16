package org.dataland.datalandbackend.services.datapoints

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import org.dataland.datalandbackend.entities.DatasetDatapointEntity
import org.dataland.datalandbackend.model.StorableDataset
import org.dataland.datalandbackend.model.datapoints.UploadedDataPoint
import org.dataland.datalandbackend.model.documents.CompanyReport
import org.dataland.datalandbackend.repositories.DatasetDatapointRepository
import org.dataland.datalandbackend.services.DataManager
import org.dataland.datalandbackend.services.DatasetStorageService
import org.dataland.datalandbackend.services.MessageQueuePublications
import org.dataland.datalandbackend.utils.DataPointValidator
import org.dataland.datalandbackend.utils.IdUtils
import org.dataland.datalandbackend.utils.ReferencedReportsUtilities
import org.dataland.datalandbackend.utils.ReferencedReportsUtilities.Companion.REFERENCED_REPORTS_ID
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
import org.springframework.transaction.support.TransactionSynchronization
import org.springframework.transaction.support.TransactionSynchronizationManager
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
        private val referencedReportsUtilities: ReferencedReportsUtilities,
    ) : DatasetStorageService {
        private val logger = LoggerFactory.getLogger(javaClass)

        /**
         * Processes a dataset by breaking it up and storing its data points in the internal storage
         * @param uploadedDataset the dataset to process
         * @param bypassQa whether to bypass the QA process
         * @param correlationId the correlation id for the operation
         * @return the id of the stored dataset
         */
        @Transactional
        override fun storeDataset(
            uploadedDataset: StorableDataset,
            bypassQa: Boolean,
            correlationId: String,
        ): String {
            val (dataContent, referencedReports, fileReferenceToPublicationDateMapping) = splitDatasetIntoDataPoints(uploadedDataset)
            validateDataset(dataContent, referencedReports, correlationId)
            return storeSplitDataset(uploadedDataset, correlationId, bypassQa, dataContent, fileReferenceToPublicationDateMapping)
        }

        private data class SplitDataset(
            val dataContent: Map<String, JsonSpecificationLeaf>,
            val referencedReports: Map<String, CompanyReport>?,
            val fileReferenceToPublicationDateMapping: Map<String, LocalDate>,
        )

        private fun splitDatasetIntoDataPoints(uploadedDataset: StorableDataset): SplitDataset {
            val frameworkSpecification = getFrameworkSpecification(uploadedDataset.dataType.toString())
            val frameworkSchema = objectMapper.readTree(frameworkSpecification.schema) as ObjectNode
            val frameworkUsesReferencedReports = frameworkSpecification.referencedReportJsonPath != null

            referencedReportsUtilities
                .insertReferencedReportsIntoFrameworkSchema(frameworkSchema, frameworkSpecification.referencedReportJsonPath)

            val dataContent =
                JsonSpecificationUtils
                    .dehydrateJsonSpecification(
                        frameworkSchema,
                        objectMapper.readTree(uploadedDataset.data) as ObjectNode,
                    ).toMutableMap()

            val referencedReports =
                if (frameworkUsesReferencedReports) {
                    referencedReportsUtilities.validateReferencedReportConsistency(
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

            return SplitDataset(dataContent, referencedReports, fileReferenceToPublicationDateMapping)
        }

        private fun storeIndividualDataPoint(
            dataPointJsonLeaf: JsonSpecificationLeaf,
            fileReferenceToPublicationDateMapping: Map<String, LocalDate>,
            dataPointIdentifier: String,
            correlationId: String,
            uploadedDataset: StorableDataset,
            bypassQa: Boolean,
        ): String? {
            val dataPointContent = dataPointJsonLeaf.content
            if (dataPointContent.isEmpty) return null

            referencedReportsUtilities.updatePublicationDateInJsonNode(
                dataPointContent,
                fileReferenceToPublicationDateMapping,
                "dataSource",
            )
            logger.info(
                "Storing value found for $dataPointIdentifier " +
                    "under ${dataPointJsonLeaf.jsonPath} (correlation ID: $correlationId)",
            )

            val dataId = IdUtils.generateUUID()
            dataPointManager.storeDataPoint(
                UploadedDataPoint(
                    dataPointContent = objectMapper.writeValueAsString(dataPointContent),
                    dataPointIdentifier = dataPointIdentifier,
                    companyId = uploadedDataset.companyId,
                    reportingPeriod = uploadedDataset.reportingPeriod,
                ),
                dataId,
                uploadedDataset.uploaderUserId,
                correlationId,
            )

            TransactionSynchronizationManager.registerSynchronization(
                object : TransactionSynchronization {
                    override fun afterCommit() {
                        messageQueuePublications.publishDataPointUploadedMessage(dataId, bypassQa, correlationId)
                    }
                },
            )
            return dataId
        }

        private fun storeSplitDataset(
            uploadedDataset: StorableDataset,
            correlationId: String,
            bypassQa: Boolean,
            dataContent: Map<String, JsonSpecificationLeaf>,
            fileReferenceToPublicationDateMapping: Map<String, LocalDate>,
        ): String {
            val datasetId = IdUtils.generateUUID()
            dataManager.storeMetaDataFrom(datasetId, uploadedDataset, correlationId)

            TransactionSynchronizationManager.registerSynchronization(
                object : TransactionSynchronization {
                    override fun afterCommit() {
                        messageQueuePublications.publishDatasetQaRequiredMessage(datasetId, bypassQa, correlationId)
                    }
                },
            )

            logger.info("Processing dataset with id $datasetId for framework ${uploadedDataset.dataType}")

            val createdDataIds = mutableMapOf<String, String>()
            dataContent.forEach { (dataPointIdentifier, dataPointJsonLeaf) ->
                storeIndividualDataPoint(
                    dataPointJsonLeaf,
                    fileReferenceToPublicationDateMapping,
                    dataPointIdentifier,
                    correlationId,
                    uploadedDataset,
                    bypassQa,
                )?.let { createdDataIds[dataPointIdentifier] = it }
            }
            this.datasetDatapointRepository.save(
                DatasetDatapointEntity(datasetId = datasetId, dataPoints = createdDataIds),
            )
            logger.info("Completed processing dataset (correlation ID: $correlationId).")
            return datasetId
        }

        private fun validateDataset(
            datasetContent: Map<String, JsonSpecificationLeaf>,
            referencedReports: Map<String, CompanyReport>?,
            correlationId: String,
        ) {
            val observedDocumentReferences = mutableSetOf<String>()

            datasetContent.forEach { (dataPointIdentifier, dataPointJsonLeaf) ->
                val dataPointContent = objectMapper.writeValueAsString(dataPointJsonLeaf.content)
                if (dataPointContent.isEmpty()) return@forEach
                dataPointValidator.validateDataPoint(dataPointIdentifier, dataPointContent, correlationId)

                val companyReport = referencedReportsUtilities.getCompanyReportFromDataSource(dataPointContent)
                if (companyReport != null && referencedReports != null) {
                    observedDocumentReferences.add(companyReport.fileReference)
                    referencedReportsUtilities.validateReportConsistencyWithGlobalList(
                        companyReport,
                        referencedReports,
                    )
                }
            }

            if (referencedReports != null) {
                val expectedObservedReferences = referencedReports.values.map { it.fileReference }.toSet()
                val unusedReferences = expectedObservedReferences - observedDocumentReferences
                if (unusedReferences.isNotEmpty()) {
                    throw InvalidInputApiException(
                        "Mismatching document references",
                        "The following document references were not used " +
                            "but listed in the referenced report field: $unusedReferences",
                    )
                }
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
         * Retrieves a dataset by assembling the data points from the internal storage
         * @param datasetId the id of the dataset
         * @param dataType the type of dataset
         * @param correlationId the correlation id for the operation
         * @return the dataset in form of a JSON string
         */
        @Transactional(readOnly = true)
        override fun getDatasetData(
            datasetId: String,
            dataType: String,
            correlationId: String,
        ): String {
            val dataPoints = getDataPointIdsForDataset(datasetId)
            return assembleDatasetFromDataPoints(dataPoints.values.toList(), dataType, correlationId)
        }

        /**
         * Retrieves a key value map containing the IDs of the data points contained in a dataset mapped to their technical IDs
         * @param datasetId the ID of the dataset
         * @return a map of data point IDs to the corresponding technical IDs that are contained in the dataset
         */
        @Transactional(readOnly = true)
        fun getDataPointIdsForDataset(datasetId: String): Map<String, String> {
            val dataPoints =
                datasetDatapointRepository
                    .findById(datasetId)
                    .getOrNull()
                    ?.dataPoints
                    ?: emptyMap()
            return dataPoints
        }

        /**
         * Assembles a dataset by retrieving the data points from the internal storage
         * and filling their content into the framework template
         * @param dataIds a list of all required data points
         * @param framework the type of dataset
         * @param correlationId the correlation id for the operation
         * @return the dataset in form of a JSON string
         */
        private fun assembleDatasetFromDataPoints(
            dataIds: List<String>,
            framework: String,
            correlationId: String,
        ): String {
            val frameworkSpecification = getFrameworkSpecification(framework)
            val frameworkTemplate = objectMapper.readTree(frameworkSpecification.schema)
            referencedReportsUtilities
                .insertReferencedReportsIntoFrameworkSchema(frameworkTemplate, frameworkSpecification.referencedReportJsonPath)

            val referencedReports = mutableMapOf<String, CompanyReport>()
            val allDataPoints = mutableMapOf<String, JsonNode>()

            dataIds.forEach { dataId ->
                val dataPoint = dataPointManager.retrieveDataPoint(dataId, correlationId)
                allDataPoints[dataPoint.dataPointIdentifier] = objectMapper.readTree(dataPoint.dataPointContent)
                val companyReport = referencedReportsUtilities.getCompanyReportFromDataSource(dataPoint.dataPointContent)
                if (companyReport != null) {
                    referencedReports[companyReport.fileName ?: companyReport.fileReference] = companyReport
                }
            }
            allDataPoints[REFERENCED_REPORTS_ID] =
                objectMapper.valueToTree(referencedReports)

            val datasetAsJsonNode = JsonSpecificationUtils.hydrateJsonSpecification(frameworkTemplate as ObjectNode) { allDataPoints[it] }

            return datasetAsJsonNode.toString()
        }
    }
