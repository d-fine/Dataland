package org.dataland.datalandqaservice.org.dataland.datalandqaservice.services

import org.dataland.datalandbackend.openApiClient.infrastructure.ClientException
import org.dataland.datalandbackendutils.exceptions.ConflictApiException
import org.dataland.datalandbackendutils.exceptions.InsufficientRightsApiException
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.utils.ValidationUtils
import org.dataland.datalandqaservice.model.reports.AcceptedDataPointSource
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.DatasetReviewEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.QaReportDataPointWithReporterEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.reports.ReviewDetailsPatch
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.UUID

/**
 * Service class to support operations on a dataset review object.
 */
@Service
class ReviewDetailsPatchValidationHelper
    @Autowired
    constructor(
        private val datasetReviewSupportService: DatasetReviewSupportService,
    ) {
        /**
         * Ensures a patch provides at least one of customDataPoint or acceptedSource.
         *
         * @param patch The patch payload to validate.
         * @throws org.dataland.datalandbackendutils.exceptions.InvalidInputApiException If both values are missing.
         */
        fun validatePatchContainsCustomDataPointOrAcceptedSource(patch: ReviewDetailsPatch) {
            if (patch.customDataPoint == null && patch.acceptedSource == null) {
                throw InvalidInputApiException(
                    "Invalid input.",
                    "Custom value or accepted source have to be specified.",
                )
            }
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
         * @throws org.dataland.datalandbackendutils.exceptions.ConflictApiException If a required custom value is missing.
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
                datasetReviewSupportService.validateCustomDataPoint(newCustomDataPoint, dataPointType)
            } catch (e: ClientException) {
                throw InvalidInputApiException(
                    "Custom datapoint not valid.",
                    "Custom datapoint given does not match the specification of $dataPointType.",
                    e,
                )
            }
            return newCustomDataPoint
        }

        /**
         * Validates the accepted QA report user ID for a data point.
         *
         * Ensures the user ID is correctly provided or omitted based on the accepted source and checks for the existence
         * of a QA report from the specified user.
         * Throws an exception if validation fails for presence, absence, or existence conditions.
         *
         * @param acceptedDataPoint The selected data point source.
         * @param qaReports The list of QA reports for the data point.
         * @param reporterUserIdOfAcceptedQaReport The user ID to validate as accepted QA report source.
         * @throws InvalidInputApiException If the user ID is missing, incorrectly provided, or does not correspond to a valid QA report.
         */
        fun validateReporterUserIdOfAcceptedQaReport(
            acceptedDataPoint: AcceptedDataPointSource?,
            qaReports: List<QaReportDataPointWithReporterEntity>,
            reporterUserIdOfAcceptedQaReport: String?,
        ) {
            var errorSummary: String? = null
            var errorMessage: String? = null

            if (acceptedDataPoint == AcceptedDataPointSource.Qa) {
                when {
                    reporterUserIdOfAcceptedQaReport == null -> {
                        errorSummary = "Missing reporterUserIdOfAcceptedQaReport."
                        errorMessage = "reporterUserIdOfAcceptedQaReport must be provided when acceptedSource is Qa."
                    }
                    qaReports.none { it.reporterUserId == ValidationUtils.convertToUUID(reporterUserIdOfAcceptedQaReport) } -> {
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
        fun getCompanyIdOfAcceptedQaReport(
            reporterUserIdOfAcceptedQaReport: String?,
            datasetReview: DatasetReviewEntity,
        ) = reporterUserIdOfAcceptedQaReport
            ?.let {
                ValidationUtils.convertToUUID(it)
            }?.let { reporterUserId ->
                datasetReview.qaReporters.firstOrNull { it.reporterUserId == reporterUserId }
            }?.reporterCompanyId

        /**
         * Throws InsufficientRightsApiException if user is not reviewer.
         *
         * @param reviewerUserId Expected reviewer user id for the dataset review.
         * @throws org.dataland.datalandbackendutils.exceptions.InsufficientRightsApiException If the current user is not the reviewer.
         */
        fun validateUserIsReviewer(reviewerUserId: UUID) {
            if (DatalandAuthentication.Companion.fromContext().userId != reviewerUserId.toString()) {
                throw InsufficientRightsApiException(
                    summary = "Only the reviewer is allowed to patch this dataset review object.",
                    message = "Please patch yourself as the reviewer before patching this object.",
                ) as Throwable
            }
        }
    }
