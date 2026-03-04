package org.dataland.datalandqaservice.org.dataland.datalandqaservice.services

import org.dataland.datalandbackendutils.exceptions.ConflictApiException
import org.dataland.datalandbackendutils.exceptions.InsufficientRightsApiException
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandbackendutils.utils.ValidationUtils.convertToUUID
import org.dataland.datalandqaservice.model.reports.AcceptedDataPointSource
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.DatasetReviewEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.QaReportDataPointWithReporterDetailsEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.DatasetReviewResponse
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.DatasetReviewState
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.reports.ReviewDetailsPatch
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.repositories.DatasetReviewRepository
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.utils.DatasetReviewCreationUtils
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID
import org.dataland.datalandbackend.openApiClient.infrastructure.ClientException as BackendClientException

/**
 * Service class for dataset review objects.
 */
@Service
class DatasetReviewService
    @Autowired
    constructor(
        private val datasetReviewRepository: DatasetReviewRepository,
        private val datasetReviewSupportService: DatasetReviewSupportService,
        private val datasetReviewCreationUtils: DatasetReviewCreationUtils,
    ) {
        /**
         * Creates and stores a new dataset review for the given dataset ID.
         *
         * Retrieves associated metadata and data points and checks for existing pending reviews.
         * Throws an exception if the dataset does not exist or a pending review is already present.
         * Returns the persisted review entity as API response.
         *
         * @param datasetId The UUID of the dataset to review.
         * @return DatasetReviewResponse API response with created review details.
         * @throws ResourceNotFoundApiException If the dataset is not found.
         * @throws ConflictApiException If a pending review exists.
         */
        @Transactional
        fun postDatasetReview(datasetId: UUID): DatasetReviewResponse {
            lateinit var datatypeToDatapointIds: Map<String, String>
            try {
                datatypeToDatapointIds = datasetReviewSupportService.getContainedDataPoints(datasetId.toString())
            } catch (_: BackendClientException) {
                throw ResourceNotFoundApiException(
                    "Dataset not found",
                    "Dataset with the id: $datasetId could not be found.",
                )
            }
            if (datasetReviewRepository.findAllByDatasetIdAndReviewState(datasetId, DatasetReviewState.Pending).isNotEmpty()) {
                throw ConflictApiException(
                    summary = "Pending dataset review entity already exists.",
                    message = "There is already a dataset review entity for this dataset which is pending.",
                )
            }

            val datasetReviewEntity =
                datasetReviewCreationUtils.createDatasetReviewEntity(
                    datasetReviewSupportService.getDataMetaInfo(datasetId.toString()),
                    datasetId,
                    datatypeToDatapointIds,
                )

            return datasetReviewRepository.save(datasetReviewEntity).toDatasetReviewResponse()
        }

        /**
         * Method to set reviewer to current user.
         */
        @Transactional
        fun setReviewer(datasetReviewId: UUID): DatasetReviewResponse {
            val datasetReview = getDatasetReview(datasetReviewId)
            datasetReview.reviewerUserId = convertToUUID(DatalandAuthentication.fromContext().userId)
            datasetReview.reviewerUserName = DatalandAuthentication.fromContext().name
            return datasetReviewRepository.save(datasetReview).toDatasetReviewResponse()
        }

        /**
         * Sets the review state for a dataset review entity.
         *
         * Validates reviewer permissions and updates the review state to the specified value.
         * Persists the change and returns the updated dataset review as an API response.
         * Throws an exception if the current user is not the reviewer.
         *
         * @param datasetReviewId The UUID of the dataset review to update.
         * @param state The new review state to apply.
         * @return DatasetReviewResponse The API response with updated review details.
         * @throws InsufficientRightsApiException If the current user is not authorized to update the review.
         */
        @Transactional
        fun setReviewState(
            datasetReviewId: UUID,
            state: DatasetReviewState,
        ): DatasetReviewResponse {
            val datasetReview = getDatasetReview(datasetReviewId)
            isUserReviewer(datasetReview.reviewerUserId)
            datasetReview.reviewState = state
            return datasetReviewRepository.save(datasetReview).toDatasetReviewResponse()
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
        @Transactional
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
                datasetReviewSupportService.validateCustomDataPoint(newCustomDataPoint, dataPointType)
            } catch (e: BackendClientException) {
                throw InvalidInputApiException(
                    "Custom datapoint not valid.",
                    "Custom datapoint given does not match the specification of $dataPointType.",
                    e,
                )
            }
            return newCustomDataPoint
        }

        /**
         * Validates and returns the accepted QA report company ID for a data point.
         *
         * Ensures the company ID is correctly provided or omitted based on the accepted source and checks for the existence
         * of a QA report from the specified company.
         * Throws an exception if validation fails for presence, absence, or existence conditions.
         * Returns the valid company ID or null if not applicable.
         *
         * @param acceptedDataPoint The selected data point source.
         * @param qaReports The list of QA reports for the data point.
         * @param companyIdOfAcceptedQaReport The company ID to validate as accepted QA report source.
         * @return The validated company ID or null.
         * @throws InvalidInputApiException If the company ID is missing, incorrectly provided, or does not correspond to a valid QA report.
         */
        @Transactional
        fun getCompanyIdOfAcceptedQaReportIfValid(
            acceptedDataPoint: AcceptedDataPointSource?,
            qaReports: List<QaReportDataPointWithReporterDetailsEntity>,
            companyIdOfAcceptedQaReport: String?,
        ): String? {
            var errorSummary: String? = null
            var errorMessage: String? = null

            if (acceptedDataPoint == AcceptedDataPointSource.Qa) {
                when {
                    companyIdOfAcceptedQaReport == null -> {
                        errorSummary = "Missing companyIdOfAcceptedQaReport."
                        errorMessage = "companyIdOfAcceptedQaReport must be provided when acceptedSource is Qa."
                    }
                    qaReports.none { it.reporterCompanyId == convertToUUID(companyIdOfAcceptedQaReport) } -> {
                        errorSummary = "QA report not found."
                        errorMessage = "No QA report from company with id $companyIdOfAcceptedQaReport found for this data point."
                    }
                }
            } else {
                if (companyIdOfAcceptedQaReport != null) {
                    errorSummary = "Invalid input."
                    errorMessage = "companyIdOfAcceptedQaReport must be null when acceptedSource is not Qa."
                }
            }
            if (errorSummary != null && errorMessage != null) {
                throw InvalidInputApiException(errorSummary, errorMessage)
            }

            return companyIdOfAcceptedQaReport
        }

        /**
         * Determines the accepted source for a data point based on provided values.
         *
         * Returns the new accepted data point source if specified; otherwise falls back to the previous source.
         * Used to maintain or update the acceptance status of a data point during review processes.
         *
         * @param newAcceptedDataPointSource The proposed new accepted source for the data point.
         * @param oldAcceptedDataPointSource The existing accepted source for the data point.
         * @return The resolved accepted data point source, or null if neither is available.
         */
        @Transactional
        fun getAcceptedSourceOfDataPoint(
            newAcceptedDataPointSource: AcceptedDataPointSource?,
            oldAcceptedDataPointSource: AcceptedDataPointSource?,
        ): AcceptedDataPointSource? {
            if (newAcceptedDataPointSource == null) {
                return oldAcceptedDataPointSource
            }
            return newAcceptedDataPointSource
        }

        /**
         * Updates review details for a specific data point in a dataset review.
         *
         * Validates and applies patch values for accepted source, custom value, and QA report company ID to the specified data point.
         * Throws exceptions for invalid input or missing required values, and persists the updated review entity.
         * Returns the modified dataset review as API response.
         *
         * @param datasetReviewId The UUID of the dataset review to update.
         * @param dataPointType The type identifier for the data point to patch.
         * @param patch The patch object containing updates for review details.
         * @return DatasetReviewResponse API response with updated review details.
         * @throws InvalidInputApiException If input values are invalid or required values are missing.
         */
        @Transactional
        fun patchReviewDetails(
            datasetReviewId: UUID,
            dataPointType: String,
            patch: ReviewDetailsPatch,
        ): DatasetReviewResponse {
            val datasetReview = getDatasetReview(datasetReviewId)
            isUserReviewer(datasetReview.reviewerUserId)
            if (patch.customDataPoint == null && patch.acceptedSource == null) {
                throw InvalidInputApiException(
                    "Invalid input.",
                    "Custom value or accepted source have to be specified.",
                )
            }
            val dataPointIndex = getIndexOfDataPointByDataPointType(datasetReview, dataPointType)
            val modifiedPatch = ReviewDetailsPatch()

            modifiedPatch.companyIdOfAcceptedQaReport =
                getCompanyIdOfAcceptedQaReportIfValid(
                    patch.acceptedSource,
                    datasetReview.dataPoints[dataPointIndex].qaReports.toList(),
                    patch.companyIdOfAcceptedQaReport,
                )

            modifiedPatch.customDataPoint =
                getCustomDataPoint(
                    dataPointType,
                    patch.customDataPoint,
                    datasetReview.dataPoints[dataPointIndex].customValue,
                    patch.acceptedSource,
                )

            modifiedPatch.acceptedSource =
                getAcceptedSourceOfDataPoint(
                    patch.acceptedSource,
                    datasetReview.dataPoints[dataPointIndex].acceptedSource,
                )

            datasetReview.dataPoints[dataPointIndex].acceptedSource = modifiedPatch.acceptedSource
            datasetReview.dataPoints[dataPointIndex].companyIdOfAcceptedQaReport =
                convertToUUID(modifiedPatch.companyIdOfAcceptedQaReport!!)
            datasetReview.dataPoints[dataPointIndex].customValue = modifiedPatch.customDataPoint

            return datasetReviewRepository.save(datasetReview).toDatasetReviewResponse()
        }

        /**
         * Method to get a dataset review entity by id and convert to response.
         */
        @Transactional(readOnly = true)
        fun getDatasetReviewById(datasetReviewId: UUID): DatasetReviewResponse = getDatasetReview(datasetReviewId).toDatasetReviewResponse()

        /**
         * Method to get dataset review objects by dataset id.
         */
        @Transactional(readOnly = true)
        fun getDatasetReviewsByDatasetId(datasetId: UUID): List<DatasetReviewResponse> =
            datasetReviewRepository.findAllByDatasetId(datasetId).map {
                it.toDatasetReviewResponse()
            }

        /**
         * Helper method to get a dataset review entity by id including exception handling.
         */
        @Transactional(readOnly = true)
        fun getDatasetReview(datasetReviewId: UUID): DatasetReviewEntity =
            datasetReviewRepository.findById(datasetReviewId).orElseThrow {
                ResourceNotFoundApiException(
                    "Dataset review object not found",
                    "No Dataset review object with the id: $datasetReviewId could be found.",
                )
            }

        /**
         * Method to find a data point by its type, throws ResourceNotFoundApiException if not found.
         */
        private fun getIndexOfDataPointByDataPointType(
            datasetReview: DatasetReviewEntity,
            dataPointType: String,
        ): Int {
            val index =
                datasetReview.dataPoints.indexOfFirst {
                    it.dataPointType == dataPointType
                }
            if (index == -1) {
                throw ResourceNotFoundApiException(
                    "Datapoint not found.",
                    "No datapoint with type $dataPointType in dataset review.",
                )
            }
            return index
        }

        /**
         * Throws InsufficientRightsApiException if user is not reviewer.
         */
        private fun isUserReviewer(reviewerUserId: UUID) {
            if (DatalandAuthentication.fromContext().userId != reviewerUserId.toString()) {
                throw InsufficientRightsApiException(
                    summary = "Only the reviewer is allowed to patch this dataset review object.",
                    message = "Please patch yourself as the reviewer before patching this object.",
                ) as Throwable
            }
        }
    }
