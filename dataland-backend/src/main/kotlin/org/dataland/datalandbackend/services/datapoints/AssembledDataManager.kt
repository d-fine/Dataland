package org.dataland.datalandbackend.services.datapoints

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import org.dataland.datalandbackend.entities.DatasetDatapointEntity
import org.dataland.datalandbackend.model.StorableDataset
import org.dataland.datalandbackend.model.datapoints.UploadedDataPoint
import org.dataland.datalandbackend.model.documents.CompanyReport
import org.dataland.datalandbackend.model.metainformation.DataMetaInformation
import org.dataland.datalandbackend.model.metainformation.PlainDataAndMetaInformation
import org.dataland.datalandbackend.repositories.DatasetDatapointRepository
import org.dataland.datalandbackend.repositories.utils.DataMetaInformationSearchFilter
import org.dataland.datalandbackend.services.DataManager
import org.dataland.datalandbackend.services.DatasetStorageService
import org.dataland.datalandbackend.services.LogMessageBuilder
import org.dataland.datalandbackend.services.MessageQueuePublications
import org.dataland.datalandbackend.utils.DataPointValidator
import org.dataland.datalandbackend.utils.IdUtils
import org.dataland.datalandbackend.utils.ReferencedReportsUtilities
import org.dataland.datalandbackend.utils.ReferencedReportsUtilities.Companion.REFERENCED_REPORTS_ID
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandbackendutils.model.BasicDataDimensions
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandbackendutils.utils.JsonSpecificationLeaf
import org.dataland.datalandbackendutils.utils.JsonSpecificationUtils
import org.dataland.datalandbackendutils.utils.QaBypass
import org.dataland.specificationservice.openApiClient.api.SpecificationControllerApi
import org.dataland.specificationservice.openApiClient.infrastructure.ClientException
import org.dataland.specificationservice.openApiClient.model.FrameworkSpecification
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
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
        private val metaDataManager: DataPointMetaInformationManager,
    ) : DatasetStorageService {
        private val logger = LoggerFactory.getLogger(javaClass)
        private val logMessageBuilder = LogMessageBuilder()

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
            val (dataContent, referencedReports, fileReferenceToPublicationDateMapping) =
                splitDatasetIntoDataPoints(uploadedDataset.data, uploadedDataset.dataType.toString())
            dataPointValidator.validateDataset(dataContent, referencedReports, correlationId)
            return storeSplitDataset(uploadedDataset, correlationId, bypassQa, dataContent, fileReferenceToPublicationDateMapping)
        }

        /**
         * Data class to store a dataset intermediately after it has been split into datapoints
         */
        data class SplitDataset(
            val dataContent: Map<String, JsonSpecificationLeaf>,
            val referencedReports: Map<String, CompanyReport>?,
            val fileReferenceToPublicationDateMapping: Map<String, LocalDate>,
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
            val frameworkSpecification = getFrameworkSpecification(dataType)
            val frameworkSchema = objectMapper.readTree(frameworkSpecification.schema) as ObjectNode
            val frameworkUsesReferencedReports = frameworkSpecification.referencedReportJsonPath != null

            referencedReportsUtilities
                .insertReferencedReportsIntoFrameworkSchema(frameworkSchema, frameworkSpecification.referencedReportJsonPath)

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

            return SplitDataset(dataContent, referencedReports, fileReferenceToPublicationDateMapping)
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
            dataPointType: String,
            correlationId: String,
            uploadedDataset: StorableDataset,
        ): String? {
            val dataPoint = dataPointJsonLeaf.content
            if (dataPoint.isEmpty) return null

            referencedReportsUtilities.updatePublicationDateInJsonNode(
                dataPoint,
                fileReferenceToPublicationDateMapping,
                "dataSource",
            )
            logger.info(
                "Storing value found for $dataPointType " +
                    "under ${dataPointJsonLeaf.jsonPath} (correlation ID: $correlationId)",
            )

            val dataPointId = IdUtils.generateUUID()
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
            return dataPointId
        }

        /**
         * Creates all individual datapoints and associated them with the datapoints
         */
        @Transactional
        fun storeDataPointsForDataset(
            datasetId: String,
            dataContent: Map<String, JsonSpecificationLeaf>,
            fileReferenceToPublicationDateMapping: Map<String, LocalDate>,
            uploadedDataset: StorableDataset,
            correlationId: String,
            initialQaStatus: QaStatus,
            initialQaComment: String?,
        ) {
            logger.info("Processing dataset with id $datasetId (correlation ID: $correlationId).")

            val createdDataIds = mutableMapOf<String, String>()
            dataContent.forEach { (dataPointType, dataPointJsonLeaf) ->
                storeIndividualDataPoint(
                    dataPointJsonLeaf = dataPointJsonLeaf,
                    fileReferenceToPublicationDateMapping = fileReferenceToPublicationDateMapping,
                    dataPointType = dataPointType,
                    correlationId = correlationId,
                    uploadedDataset = uploadedDataset,
                )?.let {
                    messageQueuePublications.publishDataPointUploadedMessage(
                        dataPointId = it,
                        initialQaStatus = initialQaStatus,
                        initialQaComment = initialQaComment,
                        correlationId = correlationId,
                    )
                    createdDataIds[dataPointType] = it
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
        ): String {
            val datasetId = IdUtils.generateUUID()
            dataManager.storeMetaDataFrom(datasetId, uploadedDataset, correlationId)
            messageQueuePublications.publishDatasetQaRequiredMessage(datasetId, bypassQa, correlationId)

            val (qaStatus, comment) = QaBypass.getCommentAndStatusForBypass(bypassQa)

            storeDataPointsForDataset(
                datasetId = datasetId,
                dataContent = dataContent,
                fileReferenceToPublicationDateMapping = fileReferenceToPublicationDateMapping,
                uploadedDataset = uploadedDataset,
                correlationId = correlationId,
                initialQaStatus = qaStatus,
                initialQaComment = comment,
            )

            return datasetId
        }

        /**
         * Retrieve a framework specification from the specification service
         * @param framework the name of the framework to retrieve the specification for
         * @return the FrameworkSpecification object
         */
        private fun getFrameworkSpecification(framework: String): FrameworkSpecification =
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
                val storedDataPoint = dataPointManager.retrieveDataPoint(dataId, correlationId)
                allDataPoints[storedDataPoint.dataPointType] = objectMapper.readTree(storedDataPoint.dataPoint)
                val companyReport = referencedReportsUtilities.getCompanyReportFromDataSource(storedDataPoint.dataPoint)
                if (companyReport != null) {
                    referencedReports[companyReport.fileName ?: companyReport.fileReference] = companyReport
                }
            }
            allDataPoints[REFERENCED_REPORTS_ID] =
                objectMapper.valueToTree(referencedReports)

            val datasetAsJsonNode = JsonSpecificationUtils.hydrateJsonSpecification(frameworkTemplate as ObjectNode) { allDataPoints[it] }

            return datasetAsJsonNode.toString()
        }

        override fun getDatasetData(
            dataDimensions: BasicDataDimensions,
            correlationId: String,
        ): String? {
            val framework = dataDimensions.dataType
            val relevantDataPointTypes = getRelevantDataPointTypes(framework)
            val relevantDataPointDimensions = relevantDataPointTypes.map { dataDimensions.toBasicDataPointDimensions(it) }
            val dataPointIds = dataPointManager.getAssociatedDataPointIds(relevantDataPointDimensions)

            if (dataPointIds.isEmpty()) {
                throw ResourceNotFoundApiException(
                    summary = logMessageBuilder.dynamicDatasetNotFoundSummary,
                    message = logMessageBuilder.getDynamicDatasetNotFoundMessage(dataDimensions),
                )
            }
            return assembleDatasetFromDataPoints(dataPointIds, framework, correlationId)
        }

        /**
         * Retrieves the relevant data point types for a specific framework
         * @param framework the name of the framework
         * @return a set of all relevant data point types
         */
        fun getRelevantDataPointTypes(framework: String): Set<String> {
            val frameworkSpecification = getFrameworkSpecification(framework)
            val frameworkTemplate = objectMapper.readTree(frameworkSpecification.schema) as ObjectNode
            return JsonSpecificationUtils.dehydrateJsonSpecification(frameworkTemplate, frameworkTemplate).keys
        }

        /**
         * Retrieves all reporting periods with at least on active data point for a specific company and framework
         */
        fun getAllReportingPeriodsWithActiveDataPoints(
            companyId: String,
            framework: String,
        ): Set<String> =
            try {
                metaDataManager.getReportingPeriodsWithActiveDataPoints(
                    dataPointTypes = getRelevantDataPointTypes(framework),
                    companyId = companyId,
                )
            } catch (ignore: InvalidInputApiException) {
                emptySet()
            }

        override fun getAllDatasetsAndMetaInformation(
            searchFilter: DataMetaInformationSearchFilter,
            correlationId: String,
        ): List<PlainDataAndMetaInformation> {
            requireNotNull(searchFilter.dataType) { "Framework must be specified." }
            requireNotNull(searchFilter.companyId) { "Company ID must be specified." }
            val companyId = searchFilter.companyId
            val framework = searchFilter.dataType.toString()
            val reportingPeriods = getAllReportingPeriodsWithActiveDataPoints(companyId = companyId, framework = framework)
            if (reportingPeriods.isEmpty()) {
                throw ResourceNotFoundApiException(
                    "No data available.",
                    "No data found for company $companyId and framework $framework.",
                )
            }

            return reportingPeriods.map {
                val data =
                    getDatasetData(BasicDataDimensions(companyId, framework, it), correlationId)
                        ?: throw IllegalStateException(
                            "Data expected for $it, $companyId and ${searchFilter.dataType}" +
                                " but not found. Correlation ID: $correlationId",
                        )
                PlainDataAndMetaInformation(
                    metaInfo =
                        DataMetaInformation(
                            dataId = "not available",
                            companyId = companyId,
                            dataType = searchFilter.dataType,
                            reportingPeriod = it,
                            currentlyActive = true,
                            uploadTime = Instant.now().toEpochMilli(),
                            qaStatus = QaStatus.Accepted,
                        ),
                    data = data,
                )
            }
        }
    }
