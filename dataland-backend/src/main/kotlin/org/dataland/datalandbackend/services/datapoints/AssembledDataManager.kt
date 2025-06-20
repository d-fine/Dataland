package org.dataland.datalandbackend.services.datapoints

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import org.dataland.datalandbackend.entities.DatasetDatapointEntity
import org.dataland.datalandbackend.model.StorableDataset
import org.dataland.datalandbackend.model.datapoints.UploadedDataPoint
import org.dataland.datalandbackend.model.documents.CompanyReport
import org.dataland.datalandbackend.model.metainformation.DataMetaInformation
import org.dataland.datalandbackend.model.metainformation.DataPointMetaInformation
import org.dataland.datalandbackend.model.metainformation.PlainDataAndMetaInformation
import org.dataland.datalandbackend.repositories.DatasetDatapointRepository
import org.dataland.datalandbackend.repositories.utils.DataMetaInformationSearchFilter
import org.dataland.datalandbackend.services.CompanyQueryManager
import org.dataland.datalandbackend.services.DataManager
import org.dataland.datalandbackend.services.DatasetStorageService
import org.dataland.datalandbackend.services.MessageQueuePublications
import org.dataland.datalandbackend.utils.DataAvailabilityIgnoredFieldsUtils
import org.dataland.datalandbackend.utils.DataPointUtils
import org.dataland.datalandbackend.utils.DataPointValidator
import org.dataland.datalandbackend.utils.IdUtils
import org.dataland.datalandbackend.utils.ReferencedReportsUtilities
import org.dataland.datalandbackend.utils.ReferencedReportsUtilities.Companion.REFERENCED_REPORTS_ID
import org.dataland.datalandbackendutils.model.BasicDataDimensions
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandbackendutils.utils.JsonComparator
import org.dataland.datalandbackendutils.utils.JsonSpecificationLeaf
import org.dataland.datalandbackendutils.utils.JsonSpecificationUtils
import org.dataland.datalandbackendutils.utils.QaBypass
import org.dataland.datalandmessagequeueutils.messages.data.InitialQaStatus
import org.dataland.datalandmessagequeueutils.messages.data.PresetQaStatus
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
        private val datasetDatapointRepository: DatasetDatapointRepository,
        private val dataPointManager: DataPointManager,
        private val referencedReportsUtilities: ReferencedReportsUtilities,
        private val companyManager: CompanyQueryManager,
        private val dataPointUtils: DataPointUtils,
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
            val (dataContent, referencedReports, fileReferenceToPublicationDateMapping, fileReferenceToFileNameMapping) =
                splitDatasetIntoDataPoints(uploadedDataset.data, uploadedDataset.dataType.toString())
            dataPointValidator.validateDataset(dataContent, referencedReports, correlationId)
            return storeSplitDataset(
                uploadedDataset, correlationId, bypassQa, dataContent, fileReferenceToPublicationDateMapping,
                fileReferenceToFileNameMapping,
            )
        }

        /**
         * Data class to store a dataset intermediately after it has been split into datapoints
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
         * Creates all individual datapoints and associated them with the datapoints
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
         * Retrieves a dataset by assembling the data points from the internal storage
         * @param datasetId the id of the dataset
         * @param dataType the type of dataset
         * @param correlationId the correlation id for the operation
         * @return the dataset in the form of a JSON string
         */
        @Transactional(readOnly = true)
        override fun getDatasetData(
            datasetId: String,
            dataType: String,
            correlationId: String,
        ): String {
            val dataPoints = getDataPointIdsForDataset(datasetId)
            return dataPointUtils.assembleSingleDataSet(
                dataPointManager.retrieveDataPoints(dataPoints.values, correlationId).values,
                dataPointUtils.getFrameworkTemplate(dataType),
            )
        }

        /**
         * Retrieves a key value map containing the IDs of the data points contained in a dataset mapped to their technical IDs
         * @param datasetId the ID of the dataset
         * @return a map of data point IDs to the corresponding technical UUIDs that are contained in the dataset
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
         * Assembles datasets by retrieving the data points from the internal storage
         * and filling their content into the framework template
         *
         * This function processes multiple datasets at once. Each dataset in the input is identified by a
         * BasicDataDimensions object.
         *
         * @param dataDimensionsToDataPointIdMap a map of all required data point IDs grouped by data set
         * @param correlationId the correlation ID for the operation
         * @return the dataset in the form of a JSON string
         */
        private fun assembleDatasetsFromDataPointIds(
            dataDimensionsToDataPointIdMap: Map<BasicDataDimensions, List<String>>,
            correlationId: String,
        ): Map<BasicDataDimensions, String> {
            val allStoredDatapoints =
                dataPointManager.retrieveDataPoints(dataDimensionsToDataPointIdMap.flatMap { it.value }, correlationId)

            val frameworkToTemplate =
                dataDimensionsToDataPointIdMap.keys
                    .map { it.dataType }
                    .toSet()
                    .associateWith { dataPointUtils.getFrameworkTemplate(it) }

            val dataDimensionsToUploadedDataPoints =
                dataDimensionsToDataPointIdMap
                    .mapValues { (_, dataPointIds) -> dataPointIds.mapNotNull { allStoredDatapoints[it] } }

            return dataDimensionsToUploadedDataPoints.mapValues { (dataDimensions, dataPoints) ->
                dataPointUtils.assembleSingleDataSet(
                    dataPoints,
                    frameworkToTemplate[dataDimensions.dataType]
                        ?: throw IllegalArgumentException("Framework not found for data dimensions: $dataDimensions"),
                )
            }
        }

        @Transactional(readOnly = true)
        override fun getDatasetData(
            dataDimensionList: Set<BasicDataDimensions>,
            correlationId: String,
        ): Map<BasicDataDimensions, String> {
            val dataPointDimensions = dataPointUtils.getBasicDataPointDimensionsForDataDimensions(dataDimensionList, correlationId)
            val dataPointIds =
                dataPointDimensions.entries
                    .associate { (dataDimension, dataPointDimensionList) ->
                        val availableDataPointIds = dataPointManager.getAssociatedDataPointIds(dataPointDimensionList)
                        dataDimension to
                            if (availableDataPointIds.keys
                                    .map { it.dataPointType }
                                    .subtract(DataAvailabilityIgnoredFieldsUtils.getIgnoredFields())
                                    .isNotEmpty()
                            ) {
                                availableDataPointIds.values.toList()
                            } else {
                                emptyList()
                            }
                    }.filterNot { it.value.isEmpty() }

            return assembleDatasetsFromDataPointIds(dataPointIds, correlationId)
        }

        @Transactional(readOnly = true)
        override fun getAllDatasetsAndMetaInformation(
            searchFilter: DataMetaInformationSearchFilter,
            correlationId: String,
        ): List<PlainDataAndMetaInformation> {
            val companyId = searchFilter.companyId
            requireNotNull(searchFilter.dataType) { "Framework must be specified." }
            requireNotNull(companyId) { "Company ID must be specified." }
            companyManager.assertCompanyIdExists(companyId)

            val framework = searchFilter.dataType.toString()
            val reportingPeriods =
                dataPointUtils
                    .getAllReportingPeriodsWithActiveDataPoints(companyId = companyId, framework = framework)
                    .filter { searchFilter.reportingPeriod.isNullOrBlank() || it == searchFilter.reportingPeriod }

            return getDatasetData(
                reportingPeriods.mapTo(mutableSetOf()) { BasicDataDimensions(companyId, framework, it) },
                correlationId,
            ).map { (dataDimensions, dataString) ->
                PlainDataAndMetaInformation(
                    metaInfo =
                        DataMetaInformation(
                            dataId = "not available",
                            companyId = companyId,
                            dataType = searchFilter.dataType,
                            reportingPeriod = dataDimensions.reportingPeriod,
                            currentlyActive = true,
                            uploadTime = dataPointUtils.getLatestUploadTime(dataDimensions),
                            qaStatus = QaStatus.Accepted,
                        ),
                    data = dataString,
                )
            }
        }
    }
