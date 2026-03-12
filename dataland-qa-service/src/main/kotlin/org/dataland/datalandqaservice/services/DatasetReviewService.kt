package org.dataland.datalandqaservice.org.dataland.datalandqaservice.services

import org.dataland.datalandbackendutils.exceptions.ConflictApiException
import org.dataland.datalandbackendutils.exceptions.InsufficientRightsApiException
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandbackendutils.utils.ValidationUtils.convertToUUID
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.DatasetReviewResponse
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.DatasetReviewState
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.reports.ReviewDetailsPatch
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.repositories.DatasetReviewRepository
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.utils.DatasetReviewCreationService
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.utils.DatasetReviewHelper
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
        private val datasetReviewCreationService: DatasetReviewCreationService,
        private val datasetReviewHelper: DatasetReviewHelper,
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
                datasetReviewCreationService.createDatasetReviewEntity(
                    datasetReviewSupportService.getDataMetaInfo(datasetId.toString()),
                    datasetId,
                    datatypeToDatapointIds,
                )

            return datasetReviewRepository.save(datasetReviewEntity).toDatasetReviewResponse()
        }

        /**
         * Method to set reviewer to current user.
         *
         * @param datasetReviewId The UUID of the dataset review to update.
         * @return DatasetReviewResponse The API response with updated review details.
         * @throws ResourceNotFoundApiException If the dataset review does not exist.
         */
        @Transactional
        fun setReviewer(datasetReviewId: UUID): DatasetReviewResponse {
            val datasetReview = datasetReviewHelper.getDatasetReview(datasetReviewId)
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
            val datasetReview = datasetReviewHelper.getDatasetReview(datasetReviewId)
            datasetReviewHelper.isUserReviewer(datasetReview.reviewerUserId)
            datasetReview.reviewState = state
            return datasetReviewRepository.save(datasetReview).toDatasetReviewResponse()
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
            val datasetReview = datasetReviewHelper.getDatasetReview(datasetReviewId)
            datasetReviewHelper.isUserReviewer(datasetReview.reviewerUserId)
            if (patch.customDataPoint == null && patch.acceptedSource == null) {
                throw InvalidInputApiException(
                    "Invalid input.",
                    "Custom value or accepted source have to be specified.",
                )
            }
            val dataPointIndex = datasetReviewHelper.getIndexOfDataPointByDataPointType(datasetReview, dataPointType)
            val modifiedPatch = ReviewDetailsPatch()

            modifiedPatch.reporterUserIdOfAcceptedQaReport =
                datasetReviewHelper.getReporterUserIdOfAcceptedQaReportIfValid(
                    patch.acceptedSource,
                    datasetReview.dataPoints[dataPointIndex].qaReports.toList(),
                    patch.reporterUserIdOfAcceptedQaReport,
                )

            modifiedPatch.customDataPoint =
                datasetReviewHelper.getCustomDataPoint(
                    dataPointType,
                    patch.customDataPoint,
                    datasetReview.dataPoints[dataPointIndex].customValue,
                    patch.acceptedSource,
                )

            modifiedPatch.acceptedSource =
                datasetReviewHelper.getAcceptedSourceOfDataPoint(
                    patch.acceptedSource,
                    datasetReview.dataPoints[dataPointIndex].acceptedSource,
                )

            datasetReview.dataPoints[dataPointIndex].acceptedSource = modifiedPatch.acceptedSource
            datasetReview.dataPoints[dataPointIndex].reporterUserIdOfAcceptedQaReport =
                modifiedPatch.reporterUserIdOfAcceptedQaReport?.let { convertToUUID(it) }
            datasetReview.dataPoints[dataPointIndex].companyIdOfAcceptedQaReport =
                datasetReviewHelper.getCompanyIdOfAcceptedQaReport(
                    modifiedPatch.reporterUserIdOfAcceptedQaReport,
                    datasetReview,
                )
            datasetReview.dataPoints[dataPointIndex].customValue = modifiedPatch.customDataPoint

            return datasetReviewRepository.save(datasetReview).toDatasetReviewResponse()
        }

        /**
         * Method to get a dataset review entity by id and convert to response.
         *
         * @param datasetReviewId The UUID of the dataset review to fetch.
         * @return DatasetReviewResponse API response for the requested review.
         * @throws ResourceNotFoundApiException If the dataset review does not exist.
         */
        @Transactional(readOnly = true)
        fun getDatasetReviewById(datasetReviewId: UUID): DatasetReviewResponse =
            datasetReviewHelper.getDatasetReview(datasetReviewId).toDatasetReviewResponse()

        /**
         * Method to get dataset review objects by dataset id.
         *
         * @param datasetId The UUID of the dataset whose reviews should be fetched.
         * @return List of DatasetReviewResponse for the given dataset.
         */
        @Transactional(readOnly = true)
        fun getDatasetReviewsByDatasetId(datasetId: UUID): List<DatasetReviewResponse> =
            datasetReviewRepository.findAllByDatasetId(datasetId).map {
                it.toDatasetReviewResponse()
            }
    }
