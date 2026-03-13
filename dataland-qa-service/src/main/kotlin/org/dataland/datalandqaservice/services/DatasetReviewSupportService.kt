package org.dataland.datalandqaservice.org.dataland.datalandqaservice.services

import org.dataland.datalandbackend.openApiClient.api.DataPointControllerApi
import org.dataland.datalandbackend.openApiClient.api.MetaDataControllerApi
import org.dataland.datalandbackend.openApiClient.infrastructure.ClientException
import org.dataland.datalandbackend.openApiClient.model.DataMetaInformation
import org.dataland.datalandbackend.openApiClient.model.DataPointToValidate
import org.dataland.datalandbackendutils.exceptions.ConflictApiException
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandqaservice.model.reports.AcceptedDataPointSource
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.DataPointQaReportEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.DatasetReviewEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.repositories.DataPointQaReportRepository
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.repositories.DatasetReviewRepository
import org.dataland.datalandspecificationservice.openApiClient.api.SpecificationControllerApi
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.UUID

/**
 * Service to support data point review operations.
 */
@Service
class DatasetReviewSupportService
    @Autowired
    constructor(
        private val dataPointControllerApi: DataPointControllerApi,
        private val metaDataControllerApi: MetaDataControllerApi,
        private val specificationControllerApi: SpecificationControllerApi,
        private val dataPointQaReportRepository: DataPointQaReportRepository,
        private val datasetReviewRepository: DatasetReviewRepository,
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
         * Retrieves a dataset review entity by its identifier.
         *
         * @param datasetReviewId The unique identifier of the dataset review to load.
         * @return The dataset review entity for the given id.
         * @throws ResourceNotFoundApiException If no dataset review exists for the given id.
         */
        fun getDatasetReviewEntityById(datasetReviewId: UUID): DatasetReviewEntity =
            datasetReviewRepository.findById(datasetReviewId).orElseThrow {
                ResourceNotFoundApiException(
                    "Dataset review object not found",
                    "No Dataset review object with the id: $datasetReviewId could be found.",
                )
            }

        /**
         * Determines the valid custom value for a data point type.
         *
         * Validates a new custom data point, falls back to the old value where appropriate, and checks specification compliance.
         * Throws an exception if a required custom value is missing or invalid for the selected source.
         * Returns the new or existing custom value, or null if not applicable.
         *
         * @param dataPointType The type identifier for the data point.
         * @param newCustomDataPoint The proposed new custom value, if any.
         * @param oldCustomDataPoint The existing custom value, if any.
         * @param acceptedSource The selected source for data point acceptance.
         * @return The valid custom value, or null.
         * @throws ConflictApiException If a required custom value is missing.
         * @throws InvalidInputApiException If the custom value does not match the specification.
         */
        fun getCustomDataPoint(
            dataPointType: String,
            newCustomDataPoint: String?,
            oldCustomDataPoint: String?,
            acceptedSource: AcceptedDataPointSource?,
        ): String? {
            if (newCustomDataPoint == null) {
                if (acceptedSource == AcceptedDataPointSource.Custom && oldCustomDataPoint == null) {
                    throw ConflictApiException(
                        "Missing custom data point.",
                        "Custom data point has to exist or be provided when acceptedSource is Custom.",
                    )
                }
                return oldCustomDataPoint
            }
            try {
                validateCustomDataPoint(newCustomDataPoint, dataPointType)
            } catch (e: ClientException) {
                throw InvalidInputApiException(
                    "Custom datapoint not valid.",
                    "Custom datapoint given does not match the specification of $dataPointType.",
                    e,
                )
            }
            return newCustomDataPoint
        }
    }
