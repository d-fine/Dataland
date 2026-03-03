package org.dataland.datalandqaservice.org.dataland.datalandqaservice.services

import org.dataland.datalandbackendutils.exceptions.ConflictApiException
import org.dataland.datalandbackendutils.exceptions.InsufficientRightsApiException
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandbackendutils.utils.ValidationUtils.convertToUUID
import org.dataland.datalandqaservice.model.reports.AcceptedDataPointSource
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.DatasetReviewEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.DatasetReviewResponse
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.DatasetReviewState
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
         * Create a dataset review object associated to the given dataset.
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
         * Method to set state of dataset review object.
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
         * Method to accept the original data point as the accepted value for a data point in the dataset review.
         */
        @Transactional
        fun acceptOriginalDataPoint(
            datasetReview: DatasetReviewEntity,
            dataPointIndex: Int,
        ): DatasetReviewResponse {
            datasetReview.dataPoints[dataPointIndex].companyIdOfAcceptedQaReport = null
            return datasetReviewRepository.save(datasetReview).toDatasetReviewResponse()
        }

        /**
         * Method to accept a QA report data point as the accepted value for a data point in the dataset review.
         */
        @Transactional
        fun acceptQaReportDataPoint(
            datasetReview: DatasetReviewEntity,
            dataPointIndex: Int,
            companyIdOfAcceptedQaReport: UUID,
        ): DatasetReviewResponse {
            datasetReview.dataPoints[dataPointIndex].companyIdOfAcceptedQaReport = companyIdOfAcceptedQaReport
            return datasetReviewRepository.save(datasetReview).toDatasetReviewResponse()
        }

        /**
         * Method to accept a custom value for a data point, including validation against the data point type spec.
         */
        @Transactional
        fun acceptCustomDataPoint(
            datasetReview: DatasetReviewEntity,
            dataPointIndex: Int,
            dataPointType: String,
            customValue: String,
        ): DatasetReviewResponse {
            try {
                datasetReviewSupportService.validateCustomDataPoint("""{\"value\":\"""" + customValue + """No\"""", dataPointType)
            } catch (e: BackendClientException) {
                throw InvalidInputApiException(
                    "Datapoint not valid.",
                    "Datapoint given does not match the specification of $dataPointType.",
                    e,
                )
            }
            datasetReview.dataPoints[dataPointIndex].companyIdOfAcceptedQaReport = null
            datasetReview.dataPoints[dataPointIndex].customValue = customValue

            return datasetReviewRepository.save(datasetReview).toDatasetReviewResponse()
        }

        /**
         * Method to set the accepted source for a data point in a dataset review.
         */
        @Transactional
        fun setAcceptedSource(
            datasetReviewId: UUID,
            dataPointType: String,
            acceptedSource: AcceptedDataPointSource,
            companyIdOfAcceptedQaReport: String?,
            customValue: String?,
        ): DatasetReviewResponse {
            val datasetReview = getDatasetReview(datasetReviewId)
            isUserReviewer(datasetReview.reviewerUserId)
            val dataPointIndex = getIndexOfDataPointByDataPointType(datasetReview, dataPointType)
            datasetReview.dataPoints[dataPointIndex].acceptedSource = acceptedSource

            return when (acceptedSource) {
                AcceptedDataPointSource.Original -> {
                    acceptOriginalDataPoint(datasetReview, dataPointIndex)
                }

                AcceptedDataPointSource.Qa -> {
                    if (companyIdOfAcceptedQaReport == null) {
                        throw InvalidInputApiException(
                            "Missing companyIdOfAcceptedQaReport.",
                            "companyIdOfAcceptedQaReport must be provided when acceptedSource is Qa.",
                        )
                    }
                    acceptQaReportDataPoint(datasetReview, dataPointIndex, convertToUUID(companyIdOfAcceptedQaReport))
                }

                AcceptedDataPointSource.Custom -> {
                    if (customValue == null) {
                        throw InvalidInputApiException(
                            "Missing customValue.",
                            "customValue must be provided when acceptedSource is Custom.",
                        )
                    }
                    acceptCustomDataPoint(datasetReview, dataPointIndex, dataPointType, customValue)
                }
            }
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
