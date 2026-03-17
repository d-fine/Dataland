package org.dataland.datalandqaservice.org.dataland.datalandqaservice.utils

import org.dataland.datalandbackendutils.exceptions.ConflictApiException
import org.dataland.datalandbackendutils.exceptions.InsufficientRightsApiException
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.DataPointJudgementEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.DataPointQaReportEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.DatasetJudgementEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.reports.JudgementDetailsPatch
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import java.util.UUID

/**
 * Utility class to support operations on a dataset review object.
 */
object DatasetJudgementValidationHelper {
    /**
     * Ensures a patch provides at least one of customDataPoint or acceptedSource.
     *
     * @param patch The patch payload to validate.
     * @throws InvalidInputApiException If both values are missing.
     */
    fun validatePatchContainsCustomDataPointOrAcceptedSource(patch: JudgementDetailsPatch) {
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
     * @throws ConflictApiException If the custom value is missing.
     */
    fun validateCustomDataPointIsSet(dataPoint: DataPointJudgementEntity) {
        if (dataPoint.customValue == null) {
            throw ConflictApiException(
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
        qaReports: List<DataPointQaReportEntity>,
        reporterUserIdOfAcceptedQaReport: String?,
    ) {
        if (reporterUserIdOfAcceptedQaReport == null) {
            throw InvalidInputApiException(
                "Missing reporterUserIdOfAcceptedQaReport.",
                "reporterUserIdOfAcceptedQaReport must be provided when acceptedSource is Qa.",
            )
        }
        if (qaReports.none { it.reporterUserId == reporterUserIdOfAcceptedQaReport }) {
            throw InvalidInputApiException(
                "QA report not found.",
                "No QA report from company with id $reporterUserIdOfAcceptedQaReport found for this data point.",
            )
        }
    }

    /**
     * Throws InsufficientRightsApiException if user is not reviewer.
     *
     * @param reviewerUserId Expected reviewer user id for the dataset review.
     * @throws InsufficientRightsApiException If the current user is not the reviewer.
     */
    fun validateUserIsJudge(reviewerUserId: UUID) {
        if (DatalandAuthentication.fromContext().userId != reviewerUserId.toString()) {
            throw InsufficientRightsApiException(
                summary = "Only the reviewer is allowed to patch this dataset review object.",
                message = "Please patch yourself as the reviewer before patching this object.",
            ) as Throwable
        }
    }

    /**
     * Validates that a dataset review exists for the given id.
     *
     * @param datasetReviewId The id used to locate the dataset review entity.
     * @param datasetJudgementEntity The dataset review entity to check, or null if not found.
     * @throws ResourceNotFoundApiException If the dataset review entity is null.
     */
    fun validateIfJudgementEntityExists(
        datasetReviewId: UUID,
        datasetJudgementEntity: DatasetJudgementEntity?,
    ) {
        if (datasetJudgementEntity == null) {
            throw ResourceNotFoundApiException(
                "Dataset review object not found",
                "No Dataset review object with the id: $datasetReviewId could be found.",
            )
        }
    }
}
