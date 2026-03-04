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
         * Helper method to validate and return the custom data point value to be set for a data point in a dataset review.
         * If the new custom data point is null, the old custom data point will be returned, otherwise the new custom
         * data point will be validated and returned.
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
         * Helper method to validate that the company id of the accepted QA report is correctly provided according to the accepted source.
         * If the accepted source is QA, a company id of the accepted QA report must be provided, otherwise it must not be provided.
         */
        fun getCompanyIdOfAcceptedQaReportIfValid(
            acceptedDataPoint: AcceptedDataPointSource?,
            qaReports: MutableList<QaReportDataPointWithReporterDetailsEntity>,
            companyIdOfAcceptedQaReport: String?,
        ): String? {
            if (acceptedDataPoint == AcceptedDataPointSource.Qa) {
                if (companyIdOfAcceptedQaReport == null) {
                    throw InvalidInputApiException(
                        "Missing companyIdOfAcceptedQaReport.",
                        "companyIdOfAcceptedQaReport must be provided when acceptedSource is Qa.",
                    )
                }
                val hasQaReportForCompany =
                    qaReports.any {
                        it.reporterCompanyId == convertToUUID(companyIdOfAcceptedQaReport)
                    }
                if (!hasQaReportForCompany) {
                    throw InvalidInputApiException(
                        "QA report not found.",
                        "No QA report from company with id $companyIdOfAcceptedQaReport found for this data point.",
                    )
                }
            } else {
                if (companyIdOfAcceptedQaReport != null) {
                    throw InvalidInputApiException(
                        "Invalid input.",
                        "companyIdOfAcceptedQaReport must be null when acceptedSource is not Qa.",
                    )
                }
            }
            return companyIdOfAcceptedQaReport
        }

    /**
     * Helper method to determine the accepted source to be set for a data point in a dataset review. If the new accepted
     * source is null, the old accepted source will be returned, otherwise the new accepted source will be returned.
     */
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
         * Method to set the accepted source for a data point in a dataset review.
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
                    datasetReview.dataPoints[dataPointIndex].qaReports,
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
