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
         * Validates and returns the accepted QA report user ID for a data point.
         *
         * Ensures the user ID is correctly provided or omitted based on the accepted source and checks for the existence
         * of a QA report from the specified user.
         * Throws an exception if validation fails for presence, absence, or existence conditions.
         * Returns the valid user ID or null if not applicable.
         *
         * @param acceptedDataPoint The selected data point source.
         * @param qaReports The list of QA reports for the data point.
         * @param reporterUserIdOfAcceptedQaReport The user ID to validate as accepted QA report source.
         * @return The validated user ID or null.
         * @throws InvalidInputApiException If the user ID is missing, incorrectly provided, or does not correspond to a valid QA report.
         */
        @Transactional
        fun getReporterUserIdOfAcceptedQaReportIfValid(
            acceptedDataPoint: AcceptedDataPointSource?,
            qaReports: List<QaReportDataPointWithReporterDetailsEntity>,
            reporterUserIdOfAcceptedQaReport: String?,
        ): String? {
            var errorSummary: String? = null
            var errorMessage: String? = null

            if (acceptedDataPoint == AcceptedDataPointSource.Qa) {
                when {
                    reporterUserIdOfAcceptedQaReport == null -> {
                        errorSummary = "Missing reporterUserIdOfAcceptedQaReport."
                        errorMessage = "reporterUserIdOfAcceptedQaReport must be provided when acceptedSource is Qa."
                    }
                    qaReports.none { it.reporterUserId == convertToUUID(reporterUserIdOfAcceptedQaReport) } -> {
                        errorSummary = "QA report not found."
                        errorMessage = "No QA report from company with id $reporterUserIdOfAcceptedQaReport found for this data point."
                    }
                }
            } else {
                if (reporterUserIdOfAcceptedQaReport != null) {
                    errorSummary = "Invalid input."
                    errorMessage = "reporterUserIdOfAcceptedQaReport must be null when acceptedSource is not Qa."
                }
            }
            if (errorSummary != null && errorMessage != null) {
                throw InvalidInputApiException(errorSummary, errorMessage)
            }

            return reporterUserIdOfAcceptedQaReport
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
         * Resolves the reporter company ID for the accepted QA report user, if available.
         *
         * Looks up the matching QA reporter in the given dataset review and returns its company ID.
         * Returns null when the accepted reporter user ID is null or no matching reporter exists.
         *
         * @param reporterUserIdOfAcceptedQaReport The user ID of the accepted QA report (string form), or null.
         * @param datasetReview The dataset review containing QA reporters to search.
         * @return The company ID of the accepted QA report reporter, or null if not found.
         */
        @Transactional
        fun getCompanyIdOfAcceptedQaReport(
            reporterUserIdOfAcceptedQaReport: String?,
            datasetReview: DatasetReviewEntity,
        ): UUID? {
            if (reporterUserIdOfAcceptedQaReport == null) {
                return null
            }
            val reporterUserId = convertToUUID(reporterUserIdOfAcceptedQaReport)
            val qaReport = datasetReview.qaReporters.firstOrNull { it.reporterUserId == reporterUserId }
            return qaReport?.reporterCompanyId
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
