package org.dataland.datalandqaservice.org.dataland.datalandqaservice.utils

import org.dataland.datalandbackendutils.exceptions.ConflictApiException
import org.dataland.datalandbackendutils.exceptions.InsufficientRightsApiException
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandbackendutils.utils.ValidationUtils.convertToUUID
import org.dataland.datalandqaservice.model.reports.AcceptedDataPointSource
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.DatasetReviewEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.QaReportDataPointWithReporterDetailsEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.repositories.DatasetReviewRepository
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.services.DatasetReviewSupportService
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID
import org.dataland.datalandbackend.openApiClient.infrastructure.ClientException as BackendClientException

/**
 * Utility class to support operations on a dataset review object.
 */
@Service
class DatasetReviewHelper
    @Autowired
    constructor(
        private val datasetReviewRepository: DatasetReviewRepository,
        private val datasetReviewSupportService: DatasetReviewSupportService,
    ) {
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
         * Helper method to get a dataset review entity by id including exception handling.
         *
         * @param datasetReviewId Unique identifier of the dataset review.
         * @return The dataset review entity for the given id.
         * @throws ResourceNotFoundApiException If no dataset review with the given id exists.
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
         *
         * @param datasetReview The dataset review containing the data points.
         * @param dataPointType Type of the data point to locate.
         * @return Index of the matching data point in the review entity.
         * @throws ResourceNotFoundApiException If the data point type is not found in the review.
         */
        fun getIndexOfDataPointByDataPointType(
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
         *
         * @param reviewerUserId Expected reviewer user id for the dataset review.
         * @throws InsufficientRightsApiException If the current user is not the reviewer.
         */
        fun isUserReviewer(reviewerUserId: UUID) {
            if (DatalandAuthentication.fromContext().userId != reviewerUserId.toString()) {
                throw InsufficientRightsApiException(
                    summary = "Only the reviewer is allowed to patch this dataset review object.",
                    message = "Please patch yourself as the reviewer before patching this object.",
                ) as Throwable
            }
        }
    }
