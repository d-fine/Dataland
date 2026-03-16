package org.dataland.datalandqaservice.org.dataland.datalandqaservice.controller

import org.dataland.datalandbackendutils.utils.ValidationUtils.convertToUUID
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.api.DatasetReviewApi
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.DatasetJudgementResponse
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.DatasetReviewState
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.reports.ReviewDetailsPatch
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.services.DatasetJudgementService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

/**
 * Controller for the dataset review service.
 */
@RestController
class DatasetReviewController(
    @Autowired private val datasetJudgementService: DatasetJudgementService,
) : DatasetReviewApi {
    override fun getDatasetReview(datasetReviewId: String): ResponseEntity<DatasetJudgementResponse> =
        ResponseEntity
            .ok(
                datasetJudgementService
                    .getDatasetReviewById(convertToUUID(datasetReviewId)),
            )

    override fun postDatasetReview(datasetId: String): ResponseEntity<DatasetJudgementResponse> =
        ResponseEntity
            .status(HttpStatus.CREATED)
            .body(datasetJudgementService.postDatasetReview(convertToUUID(datasetId)))

    override fun getDatasetReviewsByDatasetId(datasetId: String): ResponseEntity<List<DatasetJudgementResponse>> =
        ResponseEntity.ok(datasetJudgementService.getDatasetReviewsByDatasetId(convertToUUID(datasetId)))

    override fun setReviewer(datasetReviewId: String): ResponseEntity<DatasetJudgementResponse> =
        ResponseEntity.ok(datasetJudgementService.setReviewer(convertToUUID(datasetReviewId)))

    override fun setReviewState(
        datasetReviewId: String,
        state: DatasetReviewState,
    ): ResponseEntity<DatasetJudgementResponse> =
        ResponseEntity
            .ok(
                datasetJudgementService.setReviewState(
                    convertToUUID(datasetReviewId),
                    state,
                ),
            )

    override fun patchReviewDetails(
        datasetReviewId: String,
        dataPointType: String,
        patch: ReviewDetailsPatch,
    ): ResponseEntity<DatasetJudgementResponse> =
        ResponseEntity
            .ok(
                datasetJudgementService.patchReviewDetails(
                    convertToUUID(datasetReviewId),
                    dataPointType,
                    patch,
                ),
            )
}
