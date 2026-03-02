package org.dataland.datalandqaservice.org.dataland.datalandqaservice.controller

import org.dataland.datalandbackendutils.utils.ValidationUtils.convertToUUID
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.api.DatasetReviewApi
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.DatasetReviewResponse
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.DatasetReviewState
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.reports.AcceptedDataPointSource
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.services.DatasetReviewService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

/**
 * Controller for the dataset review service.
 */
@RestController
class DatasetReviewController(
    @Autowired private val datasetReviewService: DatasetReviewService,
) : DatasetReviewApi {
    override fun getDatasetReview(datasetReviewId: String): ResponseEntity<DatasetReviewResponse> =
        ResponseEntity
            .ok(
                datasetReviewService
                    .getDatasetReviewById(convertToUUID(datasetReviewId)),
            )

    override fun postDatasetReview(datasetId: String): ResponseEntity<DatasetReviewResponse> =
        ResponseEntity
            .status(HttpStatus.CREATED)
            .body(datasetReviewService.postDatasetReview(convertToUUID(datasetId)))

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

    override fun setAcceptedSource(
        datasetReviewId: String,
        dataPointType: String,
        acceptedSource: AcceptedDataPointSource,
        companyIdOfAcceptedQaReport: String?,
        customValue: String?,
    ): ResponseEntity<DatasetReviewResponse> =
        ResponseEntity
            .ok(
                datasetReviewService.setAcceptedSource(
                    convertToUUID(datasetReviewId),
                    dataPointType,
                    acceptedSource,
                    companyIdOfAcceptedQaReport,
                    customValue,
                ),
            )
}
