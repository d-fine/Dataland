package org.dataland.datalandqaservice.org.dataland.datalandqaservice.services

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.openApiClient.api.DataPointControllerApi
import org.dataland.datalandbackend.openApiClient.api.MetaDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.DataMetaInformation
import org.dataland.datalandbackend.openApiClient.model.DataPointToValidate
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.DataPointQaReportEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.DatasetJudgementEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.repositories.DataPointQaReportRepository
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.repositories.DatasetJudgementRepository
import org.dataland.datalandspecificationservice.openApiClient.api.SpecificationControllerApi
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.UUID

/**
 * Service to support data point judgement operations.
 */
@Suppress("TooManyFunctions")
@Service
class DatasetJudgementSupportService
    @Autowired
    constructor(
        private val dataPointControllerApi: DataPointControllerApi,
        private val metaDataControllerApi: MetaDataControllerApi,
        private val specificationControllerApi: SpecificationControllerApi,
        private val dataPointQaReportRepository: DataPointQaReportRepository,
        private val datasetJudgementRepository: DatasetJudgementRepository,
        private val objectMapper: ObjectMapper,
    ) {
        /**
         * Retrieves meta data of a dataset.
         *
         * @param datasetId Identifier of the dataset.
         * @return Metadata of the dataset.
         */
        fun getDataMetaInfo(datasetId: String): DataMetaInformation = metaDataControllerApi.getDataMetaInfo(datasetId)

        /**
         * Retrieves the data points contained in a dataset.
         *
         * @param datasetId Identifier of the dataset.
         * @return Map of data point type to data point id contained in the dataset.
         */
        fun getContainedDataPoints(datasetId: String): Map<String, String> = metaDataControllerApi.getContainedDataPoints(datasetId)

        /**
         * Retrieves the data point type for a given data point.
         *
         * @param dataPointId Identifier of the data point.
         * @return Data point type of the specified data point.
         */
        fun getDataPointType(dataPointId: UUID): String = dataPointControllerApi.getDataPointMetaInfo(dataPointId.toString()).dataPointType

        /**
         * Validates a custom data point.
         *
         * @param dataPoint Data point value to validate.
         * @param dataPointType Type of the data point for validation.
         */
        fun validateCustomDataPoint(
            dataPoint: String,
            dataPointType: String,
        ) {
            dataPointControllerApi.validateDataPoint(DataPointToValidate(dataPoint, dataPointType))
        }

        /**
         * Retrieves the frameworks that use a given data point type.
         *
         * @param dataPointType Data point type to look up.
         * @return List of framework ids that use the data point type.
         */
        fun getFrameworksForDataPointType(dataPointType: String): List<String> =
            specificationControllerApi.getDataPointTypeSpecification(dataPointType).usedBy.map { it.id }

        /**
         * Finds QA report IDs and reporter user IDs for given data point IDs.
         *
         * @param dataPointIds List of data point ids to search for.
         * @return List of QA report entities with meta information for the given data point ids.
         */
        fun findQaReports(dataPointIds: List<String>): List<DataPointQaReportEntity> =
            dataPointQaReportRepository
                .searchQaReportMetaInformation(
                    dataPointIds = dataPointIds,
                    showInactive = true,
                    reporterUserId = null,
                )

        /**
         * Finds the data point type using a QA report ID.
         *
         * @param qaReportId Identifier of the QA report.
         * @return Data point type associated with the QA report.
         */
        fun findDataPointTypeUsingQaReportId(qaReportId: UUID): String =
            dataPointQaReportRepository.findDataPointTypeUsingId(qaReportId.toString())

        /**
         * Retrieves a dataset judgement entity by its identifier.
         *
         * @param datasetJudgementId The unique identifier of the dataset judgement to load.
         * @return The dataset judgement entity for the given id.
         */
        fun getDatasetJudgementEntityById(datasetJudgementId: UUID): DatasetJudgementEntity? =
            datasetJudgementRepository.findById(datasetJudgementId).orElse(null)

        /**
         * Retrieves the data point type to data point id map for the currently live dataset
         * of the given company and framework.
         *
         * Returns the contained data points for the latest active dataset across all reporting
         * periods, or null if no active dataset exists for that company and framework.
         *
         * @param companyId The company identifier.
         * @param dataType The framework / data type.
         * @return Map of data point type to data point id, or null if no live dataset is found.
         */
        fun getDataPointsOfLatestActiveDataset(
            companyId: UUID,
            dataType: DataTypeEnum,
        ): Map<String, String>? {
            val activeDatasets =
                metaDataControllerApi.getListOfDataMetaInfo(
                    companyId = companyId.toString(),
                    dataType = dataType,
                    showOnlyActive = true,
                )
            val latestDataset = activeDatasets.maxByOrNull { it.reportingPeriod } ?: return null
            return metaDataControllerApi.getContainedDataPoints(latestDataset.dataId)
        }

        /**
         * Fetches and returns the value node of a data point by its id.
         *
         * The stored data point JSON contains a "value" field alongside metadata such as quality
         * and comment. This method extracts and returns only the "value" node.
         *
         * Returns null if the "value" field is absent or is an explicit JSON null.
         *
         * @param dataPointId The data point id.
         * @return The value [JsonNode], or null if absent or null.
         */
        fun getDataPointValueNode(dataPointId: String): JsonNode? {
            val dataPointJson = dataPointControllerApi.getDataPoint(dataPointId).dataPoint
            val rootNode = objectMapper.readTree(dataPointJson)
            val valueNode = rootNode["value"] ?: return null
            return if (valueNode.isNull) null else valueNode
        }

        /**
         * Resolves the data point base type id of a data point type from the specification service.
         *
         * @param dataPointType The data point type identifier.
         * @return The base type id string (e.g. "extendedDecimal", "extendedInteger", "extendedEnumYesNo").
         */
        fun resolveBaseTypeId(dataPointType: String): String =
            specificationControllerApi.getDataPointTypeSpecification(dataPointType).dataPointBaseType.id
    }
