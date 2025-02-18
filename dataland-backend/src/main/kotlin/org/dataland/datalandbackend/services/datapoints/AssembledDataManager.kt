package org.dataland.datalandbackend.services.datapoints

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import org.dataland.datalandbackend.entities.DatasetDatapointEntity
import org.dataland.datalandbackend.model.StorableDataset
import org.dataland.datalandbackend.model.datapoints.DataPointWithDocumentReference
import org.dataland.datalandbackend.model.datapoints.UploadedDataPoint
import org.dataland.datalandbackend.model.documents.CompanyReport
import org.dataland.datalandbackend.model.metainformation.DataPointMetaInformation
import org.dataland.datalandbackend.repositories.DatasetDatapointRepository
import org.dataland.datalandbackend.services.CompanyQueryManager
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
import org.dataland.datalandbackendutils.utils.JsonSpecificationUtils
import org.dataland.datalandbackendutils.utils.QaBypass
import org.dataland.specificationservice.openApiClient.infrastructure.ClientException
import org.dataland.specificationservice.openApiClient.model.FrameworkSpecification
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
        private val specificationClient: CachingSpecificationServiceClient,
        private val datasetDatapointRepository: DatasetDatapointRepository,
        private val dataPointManager: DataPointManager,
        private val referencedReportsUtilities: ReferencedReportsUtilities,
        private val companyManager: CompanyQueryManager,
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
            val splitDataset = splitDatasetIntoDataPoints(uploadedDataset.data, uploadedDataset.dataType.toString(), correlationId)
            validateDataset(splitDataset, correlationId)
            return storeSplitDataset(uploadedDataset, correlationId, bypassQa, splitDataset)
        }

        /**
         * Data class to store a dataset intermediately after it has been split into datapoints
         */
        data class SplitDataset(
            val dataContent: Map<String, DataPointValidator.IntermediateDataPoint>,
            val referencedReports: Map<String, CompanyReport>?,
        ) {
            val fileReferenceToPublicationDateMapping: Map<String, LocalDate> by lazy {
                referencedReports
                    ?.values
                    ?.filter { it.publicationDate != null }
                    ?.associate { it.fileReference to it.publicationDate!! }
                    ?: emptyMap()
            }
        }

        /**
         * Splits the dataset into individual data points
         * @param data the dataset to split
         * @param dataType the type of the dataset
         * @return the split dataset
         */
        fun splitDatasetIntoDataPoints(
            data: String,
            dataType: String,
            correlationId: String,
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
            val intermediateDataPoints =
                dataContent.entries
                    .mapNotNull { (dataPointType, dataPointLeaf) ->
                        if (dataPointLeaf.content.isNull || (dataPointLeaf.content.isObject && dataPointLeaf.content.isEmpty)) {
                            null
                        } else {
                            dataPointType to
                                dataPointValidator.getIntermediateDataPointFromJsonNode(dataPointType, dataPointLeaf.content, correlationId)
                        }
                    }.toMap()

            return SplitDataset(intermediateDataPoints, referencedReports)
        }

        /**
         * Stores an individual data point in the internal storage
         * @param intermediateDataPoint the data point to store
         * @param fileReferenceToPublicationDateMapping a mapping of file references to publication dates
         * @param correlationId the correlation id for the operation
         * @param uploadedDataset the dataset the data point belongs to
         * @return the id of the stored data point
         */
        private fun storeIndividualDataPoint(
            intermediateDataPoint: DataPointValidator.IntermediateDataPoint,
            fileReferenceToPublicationDateMapping: Map<String, LocalDate>,
            correlationId: String,
            uploadedDataset: StorableDataset,
        ): DataPointMetaInformation {
            val dataPointObjectValue = intermediateDataPoint.objectValue

            if (dataPointObjectValue is DataPointWithDocumentReference) {
                dataPointObjectValue.getAllDocumentReferences().forEach { report ->
                    val reportPublicationDate = fileReferenceToPublicationDateMapping[report.fileReference]
                    report.publicationDate = reportPublicationDate
                }
            }

            logger.info(
                "Storing value found for ${intermediateDataPoint.dataPointType} (correlation ID: $correlationId)",
            )

            val dataPointId = IdUtils.generateUUID()
            val metaInfo =
                dataPointManager.storeDataPoint(
                    uploadedDataPoint =
                        UploadedDataPoint(
                            dataPoint = objectMapper.writeValueAsString(dataPointObjectValue),
                            dataPointType = intermediateDataPoint.dataPointType,
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
         * Creates all individual datapoints and associated them with the datapoints
         */
        @Transactional
        fun storeDataPointsForDataset(
            datasetId: String,
            splitDataset: SplitDataset,
            uploadedDataset: StorableDataset,
            correlationId: String,
            initialQaStatus: QaStatus,
            initialQaComment: String?,
        ) {
            logger.info("Processing dataset with id $datasetId (correlation ID: $correlationId).")
            val companyInformation = companyManager.getCompanyById(uploadedDataset.companyId).toApiModel()

            val createdDataIds = mutableMapOf<String, String>()
            splitDataset.dataContent.forEach { (dataPointType, intermeidateDatapoint) ->
                val dataPointMetaInformation =
                    storeIndividualDataPoint(
                        intermediateDataPoint = intermeidateDatapoint,
                        fileReferenceToPublicationDateMapping = splitDataset.fileReferenceToPublicationDateMapping,
                        correlationId = correlationId,
                        uploadedDataset = uploadedDataset,
                    )
                messageQueuePublications.publishDataPointUploadedMessage(
                    dataPointMetaInformation = dataPointMetaInformation,
                    companyInformation = companyInformation,
                    initialQaStatus = initialQaStatus,
                    initialQaComment = initialQaComment,
                    correlationId = correlationId,
                )
                createdDataIds[dataPointType] = dataPointMetaInformation.dataPointId
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
         * @return the id of the dataset
         */
        private fun storeSplitDataset(
            uploadedDataset: StorableDataset,
            correlationId: String,
            bypassQa: Boolean,
            splitDataset: SplitDataset,
        ): String {
            val datasetId = IdUtils.generateUUID()
            dataManager.storeMetaDataFrom(datasetId, uploadedDataset, correlationId)
            messageQueuePublications.publishDatasetQaRequiredMessage(datasetId, bypassQa, correlationId)

            val (qaStatus, comment) = QaBypass.getCommentAndStatusForBypass(bypassQa)

            storeDataPointsForDataset(
                datasetId = datasetId,
                splitDataset = splitDataset,
                uploadedDataset = uploadedDataset,
                correlationId = correlationId,
                initialQaStatus = qaStatus,
                initialQaComment = comment,
            )

            return datasetId
        }

        /**
         * Validates the dataset by checking the data points and referenced reports
         * @param splitDataset the split dataset to validate
         * @param correlationId the correlation id for the operation
         */
        private fun validateDataset(
            splitDataset: SplitDataset,
            correlationId: String,
        ) {
            referencedReportsUtilities.validateReferencedReportConsistency(splitDataset.referencedReports ?: emptyMap())
            val observedDocumentReferences = mutableSetOf<String>()

            splitDataset.dataContent.forEach { (dataPointType, intermediateDataPoint) ->
                dataPointValidator.validateDataPoint(dataPointType, intermediateDataPoint.jsonValue, correlationId)

                if (intermediateDataPoint.objectValue is DataPointWithDocumentReference && splitDataset.referencedReports != null) {
                    val companyReports = intermediateDataPoint.objectValue.getAllDocumentReferences().mapNotNull { it.toCompanyReport() }
                    for (companyReport in companyReports) {
                        observedDocumentReferences.add(companyReport.fileReference)
                        referencedReportsUtilities.validateReportConsistencyWithGlobalList(
                            companyReport,
                            splitDataset.referencedReports,
                        )
                    }
                }
            }

            if (splitDataset.referencedReports != null) {
                val expectedObservedReferences =
                    splitDataset.referencedReports.values
                        .map { it.fileReference }
                        .toSet()
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

            val allStoredDatapoints = dataPointManager.retrieveDataPoints(dataIds, correlationId)
            val allDataPoints =
                allStoredDatapoints.entries
                    .associate {
                        it.value.dataPointType to objectMapper.readTree(it.value.dataPoint)
                    }.toMutableMap()

            val allReferencedReports =
                allDataPoints.entries.flatMap {
                    val intermediate =
                        dataPointValidator.getIntermediateDataPointFromJsonNode(
                            it.key,
                            it.value,
                            correlationId,
                        )
                    if (intermediate.objectValue is DataPointWithDocumentReference) {
                        intermediate.objectValue.getAllDocumentReferences().mapNotNull { report -> report.toCompanyReport() }
                    } else {
                        emptyList()
                    }
                }
            val referencedReports = allReferencedReports.associateBy { it.fileName ?: it.fileReference }

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
            val frameworkSpecification = getFrameworkSpecification(framework)
            val frameworkTemplate = objectMapper.readTree(frameworkSpecification.schema) as ObjectNode
            val relevantDataPointTypes = JsonSpecificationUtils.dehydrateJsonSpecification(frameworkTemplate, frameworkTemplate).keys
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
    }
