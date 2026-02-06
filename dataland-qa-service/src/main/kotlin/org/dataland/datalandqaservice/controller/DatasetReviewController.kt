package org.dataland.datalandqaservice.org.dataland.datalandqaservice.controller

import org.dataland.datalandbackendutils.utils.ValidationUtils.convertToUUID
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.api.DatasetReviewApi
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.DatasetReviewResponse
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.DatasetReviewState
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.services.DatasetReviewService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

/**
 * Controller for the dataset review service.
 */
@RestController
class DatasetReviewController(
    private val datasetReviewService: DatasetReviewService,
) : DatasetReviewApi {
    override fun getDatasetReview(datasetReviewId: String): ResponseEntity<DatasetReviewResponse> =
        ResponseEntity.ok(
            datasetReviewService
                .getDatasetReviewById(convertToUUID(datasetReviewId))
                .toDatasetReviewResponse(),
        )

    override fun postDatasetReview(datasetId: String): ResponseEntity<DatasetReviewResponse> =
        ResponseEntity.ok(datasetReviewService.createDatasetReview(convertToUUID(datasetId)))

    override fun getDatasetReviewsByDatasetId(datasetId: String): ResponseEntity<List<DatasetReviewResponse>> =
        ResponseEntity.ok(datasetReviewService.getDatasetReviewsByDatasetId(convertToUUID(datasetId)))

    override fun setReviewer(datasetReviewId: String): ResponseEntity<DatasetReviewResponse> =
        ResponseEntity.ok(datasetReviewService.setReviewer(convertToUUID(datasetReviewId)))

    override fun setReviewState(
        datasetReviewId: String,
        state: DatasetReviewState,
    ): ResponseEntity<DatasetReviewResponse> =
        ResponseEntity
            .ok(
                datasetReviewService.setReviewState(
                    convertToUUID(datasetReviewId),
                    state,
                ),
            )

    override fun acceptOriginalDatapoint(
        datasetReviewId: String,
        dataPointId: String,
    ): ResponseEntity<DatasetReviewResponse> =
        ResponseEntity
            .ok(
                datasetReviewService.acceptOriginalDatapoint(
                    convertToUUID(datasetReviewId),
                    convertToUUID(dataPointId),
                ),
            )

    override fun acceptQaReport(
        datasetReviewId: String,
        qaReportId: String,
    ): ResponseEntity<DatasetReviewResponse> =
        ResponseEntity
            .ok(
                datasetReviewService.acceptQaReport(
                    convertToUUID(datasetReviewId),
                    convertToUUID(qaReportId),
                ),
            )

    override fun acceptCustomDataPoint(
        datasetReviewId: String,
        dataPoint: String,
        dataPointType: String,
    ): ResponseEntity<DatasetReviewResponse> =
        ResponseEntity
            .ok(
                datasetReviewService.acceptCustomDataPoint(
                    convertToUUID(datasetReviewId),
                    dataPoint, dataPointType,
                ),
            )
}
