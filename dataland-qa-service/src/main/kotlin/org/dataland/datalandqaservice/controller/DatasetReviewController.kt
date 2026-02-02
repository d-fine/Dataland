package org.dataland.datalandqaservice.org.dataland.datalandqaservice.controller

import org.dataland.datalandqaservice.org.dataland.datalandqaservice.api.DatasetReviewApi
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.DatasetReviewResponse
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.DatasetReviewState
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.org.dataland.datalandqaservice.model.DatasetReview
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.services.DatasetReviewService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

/**
 * Controller for the dataset review service.
 */
@RestController
class DatasetReviewController(
    private val datasetReviewService: DatasetReviewService,
) : DatasetReviewApi {
    override fun postDatasetReview(datasetReview: DatasetReview): ResponseEntity<DatasetReviewResponse> {
        TODO("Not yet implemented")
    }

    override fun setReviewer(datasetReviewId: String): ResponseEntity<DatasetReviewResponse> =
        ResponseEntity.ok(datasetReviewService.setReviewer(UUID.fromString(datasetReviewId)))

    override fun setState(
        datasetReviewId: String,
        state: DatasetReviewState,
    ): ResponseEntity<DatasetReviewResponse> =
        ResponseEntity
            .ok(
                datasetReviewService.setState(
                    UUID.fromString(datasetReviewId),
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
                    UUID.fromString(datasetReviewId),
                    UUID.fromString(dataPointId),
                ),
            )

    override fun acceptQaReport(
        datasetReviewId: String,
        qaReportId: String,
    ): ResponseEntity<DatasetReviewResponse> =
        ResponseEntity
            .ok(
                datasetReviewService.acceptQaReport(
                    UUID.fromString(datasetReviewId),
                    UUID.fromString(qaReportId),
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
                    UUID.fromString(datasetReviewId),
                    dataPoint, dataPointType,
                ),
            )
}
