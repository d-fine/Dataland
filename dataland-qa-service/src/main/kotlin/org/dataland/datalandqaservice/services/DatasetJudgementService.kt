package org.dataland.datalandqaservice.org.dataland.datalandqaservice.services

import org.dataland.datalandbackendutils.exceptions.ConflictApiException
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandbackendutils.utils.ValidationUtils.convertToUUID
import org.dataland.datalandqaservice.model.reports.AcceptedDataPointSource
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.DataPointJudgementEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.DatasetJudgementEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.DatasetJudgementResponse
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.DatasetReviewState
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.reports.ReviewDetailsPatch
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.repositories.DatasetReviewRepository
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.utils.DatasetJudgementValidationHelper
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
class DatasetJudgementService
    @Autowired
    constructor(
        private val datasetReviewRepository: DatasetReviewRepository,
        private val datasetJudgementSupportService: DatasetJudgementSupportService,
        private val datasetReviewCreationService: DatasetReviewCreationService,
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
        fun postDatasetReview(datasetId: UUID): DatasetJudgementResponse {
            lateinit var datatypeToDatapointIds: Map<String, String>
            try {
                datatypeToDatapointIds = datasetJudgementSupportService.getContainedDataPoints(datasetId.toString())
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
                datasetReviewCreationService.createDatasetReviewEntity(
                    datasetJudgementSupportService.getDataMetaInfo(datasetId.toString()),
                    datasetId,
                    datatypeToDatapointIds,
                )

            return datasetReviewRepository.save(datasetReviewEntity).toDatasetJudgementResponse()
        }

        /**
         * Method to set reviewer to current user.
         *
         * @param datasetReviewId The UUID of the dataset review to update.
         * @return DatasetReviewResponse The API response with updated review details.
         * @throws ResourceNotFoundApiException If the dataset review does not exist.
         */
        @Transactional
        fun setReviewer(datasetReviewId: UUID): DatasetJudgementResponse {
            val datasetReview = getDatasetReviewOrThrow(datasetReviewId)
            datasetReview.qaJudgeUserId = convertToUUID(DatalandAuthentication.fromContext().userId)
            datasetReview.qaJudgeUserName = DatalandAuthentication.fromContext().name
            return datasetReviewRepository.save(datasetReview).toDatasetJudgementResponse()
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
         */
        @Transactional
        fun setReviewState(
            datasetReviewId: UUID,
            state: DatasetReviewState,
        ): DatasetJudgementResponse {
            val datasetReview = getDatasetReviewOrThrow(datasetReviewId)
            DatasetJudgementValidationHelper.validateUserIsReviewer(datasetReview.qaJudgeUserId)
            datasetReview.reviewState = state
            return datasetReviewRepository.save(datasetReview).toDatasetJudgementResponse()
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
        ): DatasetJudgementResponse {
            val datasetReview = getDatasetReviewOrThrow(datasetReviewId)
            DatasetJudgementValidationHelper.validateUserIsReviewer(datasetReview.qaJudgeUserId)
            DatasetJudgementValidationHelper.validatePatchContainsCustomDataPointOrAcceptedSource(patch)
            val dataPoint =
                datasetReview.dataPoints
                    .find { it.dataPointType == dataPointType }
                    ?: throw InvalidInputApiException(
                        "Invalid input.",
                        "Data point with type '$dataPointType' not found.",
                    )

            applyCustomDataPoint(dataPointType, patch.customDataPoint, dataPoint)
            applyAcceptedSource(dataPoint, patch)

            return datasetReviewRepository.save(datasetReview).toDatasetJudgementResponse()
        }

        /**
         * Validates and applies the custom data point value to the given data point.
         *
         * @param dataPointType The type identifier used for validation.
         * @param customDataPoint The custom value from the patch, or null.
         * @param dataPoint The data point entity to update.
         * @throws InvalidInputApiException If the custom value fails validation.
         */
        private fun applyCustomDataPoint(
            dataPointType: String,
            customDataPoint: String?,
            dataPoint: DataPointJudgementEntity,
        ) {
            if (customDataPoint != null) {
                try {
                    datasetJudgementSupportService.validateCustomDataPoint(customDataPoint, dataPointType)
                } catch (e: BackendClientException) {
                    throw InvalidInputApiException(
                        "Custom datapoint not valid.",
                        "Custom datapoint given does not match the specification of $dataPointType.",
                        e,
                    )
                }
                dataPoint.customValue = customDataPoint
            }
        }

        /**
         * Applies the accepted source and related fields based on the patch.
         *
         * @param dataPoint The data point entity to update.
         * @param patch The patch containing the accepted source and related data.
         */
        private fun applyAcceptedSource(
            dataPoint: DataPointJudgementEntity,
            patch: ReviewDetailsPatch,
        ) {
            when (patch.acceptedSource) {
                AcceptedDataPointSource.Original -> {
                    dataPoint.apply {
                        this.acceptedSource = AcceptedDataPointSource.Original
                        this.reporterUserIdOfAcceptedQaReport = null
                    }
                }
                AcceptedDataPointSource.Qa -> {
                    DatasetJudgementValidationHelper.validateReporterUserIdOfAcceptedQaReport(
                        dataPoint.qaReports.toList(),
                        patch.reporterUserIdOfAcceptedQaReport,
                    )
                    dataPoint.apply {
                        this.acceptedSource = AcceptedDataPointSource.Qa
                        this.reporterUserIdOfAcceptedQaReport =
                            convertToUUID(patch.reporterUserIdOfAcceptedQaReport!!)
                    }
                }
                AcceptedDataPointSource.Custom -> {
                    DatasetJudgementValidationHelper.validateCustomDataPointIsSet(dataPoint)
                    dataPoint.apply {
                        this.acceptedSource = AcceptedDataPointSource.Custom
                        this.reporterUserIdOfAcceptedQaReport = null
                    }
                }
                null -> return
            }
        }

        /**
         * Method to get a dataset review entity by id and convert to response.
         *
         * @param datasetReviewId The UUID of the dataset review to fetch.
         * @return DatasetReviewResponse API response for the requested review.
         * @throws ResourceNotFoundApiException If the dataset review does not exist.
         */
        @Transactional(readOnly = true)
        fun getDatasetReviewById(datasetReviewId: UUID): DatasetJudgementResponse {
            val datasetReview = getDatasetReviewOrThrow(datasetReviewId)
            return datasetReview.toDatasetJudgementResponse()
        }

        /**
         * Method to get dataset review objects by dataset id.
         *
         * @param datasetId The UUID of the dataset whose reviews should be fetched.
         * @return List of DatasetReviewResponse for the given dataset.
         */
        @Transactional(readOnly = true)
        fun getDatasetReviewsByDatasetId(datasetId: UUID): List<DatasetJudgementResponse> =
            datasetReviewRepository.findAllByDatasetId(datasetId).map {
                it.toDatasetJudgementResponse()
            }

        /**
         * Loads the dataset review entity or throws if it does not exist.
         *
         * @param datasetReviewId The UUID of the dataset review to load.
         * @return The dataset review entity for the given id.
         * @throws ResourceNotFoundApiException If no dataset review exists for the given id.
         */
        private fun getDatasetReviewOrThrow(datasetReviewId: UUID): DatasetJudgementEntity {
            val datasetReview = datasetJudgementSupportService.getDatasetReviewEntityById(datasetReviewId)
            DatasetJudgementValidationHelper.validateIfDatasetExists(
                datasetReviewId,
                datasetReview,
            )
            return datasetReview
        }
    }
