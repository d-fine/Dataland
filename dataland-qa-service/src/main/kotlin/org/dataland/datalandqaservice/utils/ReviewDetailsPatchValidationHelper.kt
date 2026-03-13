package org.dataland.datalandqaservice.org.dataland.datalandqaservice.utils

import org.dataland.datalandbackendutils.exceptions.InsufficientRightsApiException
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.utils.ValidationUtils
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.DataPointReviewEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.DatasetReviewEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.QaReportDataPointWithReporterEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.reports.ReviewDetailsPatch
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import java.util.UUID

/**
 * Utility class to support operations on a dataset review object.
 */
object ReviewDetailsPatchValidationHelper {
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
     * Ensures a custom data point value is present on the given data point review entity.
     *
     * @param dataPoint The data point review entity to validate.
     * @throws org.dataland.datalandbackendutils.exceptions.InvalidInputApiException If the custom value is missing.
     */
    fun validateCustomDataPointIsSet(dataPoint: DataPointReviewEntity) {
        if (dataPoint.customValue == null) {
            throw InvalidInputApiException(
                "Missing custom data point.",
                "Custom data point has to be provided when acceptedSource is Custom.",
            )
        }
    }

    /**
     * Validates the accepted QA report user ID for a data point.
     *
     * Ensures the user ID is correctly provided or omitted and checks for the existence
     * of a QA report from the specified user.
     * Throws an exception if validation fails for absence, or existence conditions.
     *
     * @param qaReports The list of QA reports for the data point.
     * @param reporterUserIdOfAcceptedQaReport The user ID to validate as accepted QA report source.
     * @throws InvalidInputApiException If the user ID is missing or does not correspond to a valid QA report.
     */
    fun validateReporterUserIdOfAcceptedQaReport(
        qaReports: List<QaReportDataPointWithReporterEntity>,
        reporterUserIdOfAcceptedQaReport: String?,
    ) {
        if (reporterUserIdOfAcceptedQaReport == null) {
            throw InvalidInputApiException(
                "Missing reporterUserIdOfAcceptedQaReport.",
                "reporterUserIdOfAcceptedQaReport must be provided when acceptedSource is Qa.",
            )
        }
        if (qaReports.none { it.reporterUserId == ValidationUtils.convertToUUID(reporterUserIdOfAcceptedQaReport) }) {
            throw InvalidInputApiException(
                "QA report not found.",
                "No QA report from company with id $reporterUserIdOfAcceptedQaReport found for this data point.",
            )
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
