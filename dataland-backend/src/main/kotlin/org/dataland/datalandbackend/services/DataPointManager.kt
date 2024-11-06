package org.dataland.datalandbackend.services

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.dataland.datalandbackend.entities.DatasetDatapointEntity
import org.dataland.datalandbackend.model.StorableDataSet
import org.dataland.datalandbackend.model.datapoints.UploadableDataPoint
import org.dataland.datalandbackend.model.documents.CompanyReport
import org.dataland.datalandbackend.model.metainformation.DataPointMetaInformation
import org.dataland.datalandbackend.repositories.DatasetDatapointRepository
import org.dataland.datalandbackend.utils.IdUtils
import org.dataland.datalandbackend.utils.JsonOperations.extractDataPointsFromFrameworkTemplate
import org.dataland.datalandbackend.utils.JsonOperations.getCompanyReportFromDataSource
import org.dataland.datalandbackend.utils.JsonOperations.getFileReferenceToPublicationDateMapping
import org.dataland.datalandbackend.utils.JsonOperations.getJsonNodeFromString
import org.dataland.datalandbackend.utils.JsonOperations.getValueFromJsonNode
import org.dataland.datalandbackend.utils.JsonOperations.insertReferencedReports
import org.dataland.datalandbackend.utils.JsonOperations.objectMapper
import org.dataland.datalandbackend.utils.JsonOperations.replaceFieldInTemplate
import org.dataland.datalandbackend.utils.JsonOperations.updatePublicationDateInJsonNode
import org.dataland.datalandbackend.utils.JsonOperations.validateConsistency
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.specificationservice.openApiClient.api.SpecificationControllerApi
import org.dataland.specificationservice.openApiClient.infrastructure.ClientException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.UUID
import kotlin.jvm.optionals.getOrNull

/**
 * Class for managing data points and associated validations
 * @param dataManager service for handling data storage
 * @param metaDataManager service for handling data meta information
 * @param specificationManager service for handling data point specifications
 * @param datasetDatapointRepository repository for storing the mapping between data sets and data points
 */
@Component("DataPointManager")
class DataPointManager(
    @Autowired private val dataManager: DataManager,
    @Autowired private val metaDataManager: DataMetaInformationManager,
    @Autowired private val specificationManager: SpecificationControllerApi,
    @Autowired private val datasetDatapointRepository: DatasetDatapointRepository,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Stores a single data point in the internal storage
     * @param uploadedDataPoint the data point to store
     * @param uploaderUserId the user id of the user who uploaded the data point
     * @param bypassQa whether to bypass the QA process
     * @param correlationId the correlation id for the operation
     * @return the id of the stored data point
     */
    fun storeDataPoint(
        uploadedDataPoint: UploadableDataPoint,
        uploaderUserId: String,
        bypassQa: Boolean,
        correlationId: String,
    ): DataPointMetaInformation {
        logger.info("Executing check for '${uploadedDataPoint.dataPointIdentifier}' data point (correlation ID: $correlationId).")
        validateDataPoint(uploadedDataPoint.dataPointIdentifier, uploadedDataPoint.dataPointContent, correlationId)
        logger.info("Storing '${uploadedDataPoint.dataPointIdentifier}' data point.")
        val uploadTime = Instant.now().toEpochMilli()
        val storableDataSet = uploadedDataPoint.toStorableDataSet(uploaderUserId, uploadTime)

        val dataId = IdUtils.generateUUID()
        dataManager.storeMetaDataFrom(dataId, storableDataSet, correlationId)
        dataManager.storeDataSetInTemporaryStoreAndSendMessage(dataId, storableDataSet, bypassQa, correlationId)

        return DataPointMetaInformation(
            dataId = dataId,
            dataPointIdentifier = uploadedDataPoint.dataPointIdentifier,
            companyId = uploadedDataPoint.companyId,
            reportingPeriod = storableDataSet.reportingPeriod,
            uploaderUserId = uploaderUserId,
            uploadTime = uploadTime,
        )
    }

    /**
     * Validates a single data point by casting it to the correct class and running the validations
     * @param dataPointIdentifier the identifier of the data point
     * @param dataPointContent the content of the data point
     * @param correlationId the correlation id for the operation
     */
    private fun validateDataPoint(
        dataPointIdentifier: String,
        dataPointContent: String,
        correlationId: String,
    ) {
        logger.info("Validating data point $dataPointIdentifier (correlation ID: $correlationId)")
        validateDataPointIdentifierExists(dataPointIdentifier)
        val dataPointType = specificationManager.getDataPointSpecification(dataPointIdentifier).validatedBy.id
        val validationClass = specificationManager.getDataPointTypeSpecification(dataPointType).validatedBy
        validateConsistency(dataPointContent, validationClass, correlationId)
    }

    /**
     * Retrieves a single data point from the internal storage
     * @param dataId the id of the data point
     * @param dataPointIdentifier the identifier of the data point
     * @param correlationId the correlation id for the operation
     * @return the data point in form of a StorableDataSet
     */
    fun retrieveDataPoint(
        dataId: String,
        dataPointIdentifier: String,
        correlationId: String,
    ): StorableDataSet {
        logger.info("Retrieving $dataPointIdentifier data point with id $dataId (correlation ID: $correlationId).")
        validateDataPointIdentifierExists(dataPointIdentifier)
        val storedDataPoint = dataManager.getPublicDataSet(dataId, dataPointIdentifier, correlationId)
        return storedDataPoint
    }

    private fun validateDataPointIdentifierExists(dataPointIdentifier: String) {
        try {
            specificationManager.getDataPointSpecification(dataPointIdentifier)
        } catch (clientException: ClientException) {
            logger.error("Data point identifier $dataPointIdentifier not found: ${clientException.message}.")
            throw InvalidInputApiException(
                "Specified data point identifier $dataPointIdentifier is not valid.",
                "The specified data point identifier $dataPointIdentifier is not known to the specification service.",
            )
        }
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
        val frameworkTemplate = getFrameworkTemplate(uploadedDataSet.dataType)

        val expectedDataPoints = extractDataPointsFromFrameworkTemplate(frameworkTemplate, "")
        val companyId = UUID.fromString(uploadedDataSet.companyId)
        val dataSetContent = getJsonNodeFromString(uploadedDataSet.data)

        val dataSetId = IdUtils.generateUUID()
        dataManager.storeMetaDataFrom(dataSetId, uploadedDataSet, correlationId)

        logger.info("Processing data set with id $dataSetId for framework ${uploadedDataSet.dataType}")

        val fileReferenceToPublicationDateMapping =
            getFileReferenceToPublicationDateMapping(
                dataSetContent = dataSetContent,
                jsonPath = "general.general.referencedReports",
            )

        val createdDataIds = mutableListOf<String>()
        expectedDataPoints.forEach {
            val dataPointJsonPath = it.key
            val dataPointIdentifier = it.value
            val dataPointContent = getValueFromJsonNode(dataSetContent, dataPointJsonPath)
            if (dataPointContent.isEmpty()) return@forEach
            val contentJsonNode = objectMapper.readTree(dataPointContent)

            updatePublicationDateInJsonNode(
                contentJsonNode,
                fileReferenceToPublicationDateMapping,
                "dataSource",
            )
            logger.info("Storing value found for $dataPointIdentifier under $dataPointJsonPath (correlation ID: $correlationId)")

            createdDataIds +=
                storeDataPoint(
                    UploadableDataPoint(
                        dataPointContent = objectMapper.writeValueAsString(contentJsonNode),
                        dataPointIdentifier = dataPointIdentifier,
                        companyId = companyId,
                        reportingPeriod = uploadedDataSet.reportingPeriod,
                    ),
                    uploadedDataSet.uploaderUserId,
                    bypassQa,
                    correlationId,
                ).dataId
        }
        this.datasetDatapointRepository.save(
            DatasetDatapointEntity(dataId = dataSetId, dataPoints = createdDataIds.joinToString(",")),
        )
        logger.info("Completed processing data set (correlation ID: $correlationId).")
        return dataSetId
    }

    private fun getFrameworkTemplate(framework: String): JsonNode =
        try {
            getJsonNodeFromString(specificationManager.getFrameworkSpecification(framework).schema)
        } catch (clientException: ClientException) {
            logger.error("Framework $framework not found: ${clientException.message}.")
            throw InvalidInputApiException(
                "Framework $framework not found.",
                "The specified framework $framework is not known to the specification service.",
            )
        }

    /**
     * Retrieves a data set by assembling the data points from the internal storage
     * @param dataSetId the id of the data set
     * @param framework the type of data set
     * @param correlationId the correlation id for the operation
     * @return the data set in form of a JSON string
     */
    fun getDataSetFromId(
        dataSetId: String,
        framework: String,
        correlationId: String,
    ): String {
        val dataPoints =
            datasetDatapointRepository
                .findById(dataSetId)
                .getOrNull()
                ?.dataPoints
                ?.split(",")
                ?: throw InvalidInputApiException(
                    "Data set not found.",
                    "There is no record of a data set of type $framework and ID $dataSetId.",
                )
        return assembleDataSetFromDataPoints(dataPoints, framework, correlationId)
    }

    /**
     * Assembles a data set by retrieving the data points from the internal storage and filling their content into the framework template
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
        val dataPoints = mutableListOf<String>()
        val frameworkTemplate = getFrameworkTemplate(framework)
        val allDataPointsInTemplate = extractDataPointsFromFrameworkTemplate(frameworkTemplate, "")
        val referencedReports = mutableMapOf<String, CompanyReport>()
        val objectMapper = jacksonObjectMapper().findAndRegisterModules()
        objectMapper.dateFormat = SimpleDateFormat("yyyy-MM-dd")

        logger.info("Filling template with stored data (correlation ID: $correlationId).")
        dataIds.forEach { dataId ->
            val currentDataPoint = metaDataManager.getDataMetaInformationByDataId(dataId).dataType
            if (!allDataPointsInTemplate.containsValue(currentDataPoint)) {
                throw IllegalArgumentException(
                    "Data point $currentDataPoint is not part of the framework template for $framework " +
                        "(correlation ID $correlationId).",
                )
            }
            dataPoints.add(currentDataPoint)
            val dataPointContent = retrieveDataPoint(dataId, currentDataPoint, correlationId).data
            val replacementValue = getJsonNodeFromString(dataPointContent)
            val companyReport = getCompanyReportFromDataSource(dataPointContent)
            if (companyReport != null) {
                referencedReports[companyReport.fileName ?: companyReport.fileReference] = companyReport
            }

            val jsonPaths = allDataPointsInTemplate.filterValues { it == currentDataPoint }.keys
            jsonPaths.forEach {
                replaceFieldInTemplate(frameworkTemplate, it, "", replacementValue)
            }
        }

        insertReferencedReports(frameworkTemplate, "general.general.referencedReports", referencedReports)
        logger.info("Removing fields from the template where no data was provided (correlation ID $correlationId).")
        allDataPointsInTemplate.forEach {
            if (!dataPoints.contains(it.value)) {
                replaceFieldInTemplate(frameworkTemplate, it.key, "", getJsonNodeFromString("null"))
            }
        }
        logger.info("Completed framework assembly from data points (correlation ID $correlationId)")
        return frameworkTemplate.toString()
    }
}
