package org.dataland.datalandqaservice.org.dataland.datalandqaservice.utils

import org.dataland.datalandbackendutils.exceptions.ConflictApiException
import org.dataland.datalandbackendutils.exceptions.InsufficientRightsApiException
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandqaservice.model.reports.AcceptedDataPointSource
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.DataPointJudgementEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.DatasetJudgementEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.DatasetJudgementState
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
     * Ensures a QA report ID is provided when the accepted source is QA.
     *
     * @param patch The patch payload to validate.
     * @throws InvalidInputApiException If acceptedSource is Qa but acceptedQaReportId is missing.
     */
    fun validateAcceptedQaReportIdIsSetIfAcceptedSourceIsQa(patch: JudgementDetailsPatch) {
        if (patch.acceptedSource == AcceptedDataPointSource.Qa && patch.acceptedQaReportId == null) {
            throw InvalidInputApiException(
                summary = "Missing accepted QA report ID.",
                message = "Accepted QA report ID must be provided when accepted source is QA.",
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
     * Ensures that all data points in the dataset judgement have an accepted source.
     *
     * @param dataPoints The collection of data point judgement entities to validate.
     * @throws InvalidInputApiException If any data point has no accepted source set.
     */
    fun validateAllDataPointsHaveAcceptedSource(dataPoints: Collection<DataPointJudgementEntity>) {
        val unreviewed = dataPoints.filter { it.acceptedSource == null }.map { it.dataPointType }
        if (unreviewed.isNotEmpty()) {
            throw InvalidInputApiException(
                summary = "Not all data points have an accepted source.",
                message = "The following data points are missing an accepted source: ${unreviewed.joinToString()}.",
            )
        }
    }

    /**
     * Ensures the dataset judgement is in pending state.
     */
    fun validateDatasetJudgementIsPending(datasetJudgment: DatasetJudgementEntity) {
        if (datasetJudgment.judgementState != DatasetJudgementState.Pending) {
            throw ConflictApiException(
                summary = "Dataset judgement is not in pending state.",
                message = "Only dataset judgements in pending state can be patched.",
            )
        }
    }
}
