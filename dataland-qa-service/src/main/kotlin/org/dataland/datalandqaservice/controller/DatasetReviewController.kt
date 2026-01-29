package org.dataland.datalandqaservice.org.dataland.datalandqaservice.controller

import org.dataland.datalandqaservice.org.dataland.datalandqaservice.api.DatasetReviewApi
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
    override fun setReviewer(datasetReviewId: String): ResponseEntity<String> {
        datasetReviewService.setReviewer(UUID.fromString(datasetReviewId))
        return ResponseEntity.ok(datasetReviewId)
    }
}
