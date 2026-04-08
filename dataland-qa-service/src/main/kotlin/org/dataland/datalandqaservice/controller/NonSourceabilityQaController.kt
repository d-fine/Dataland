package org.dataland.datalandqaservice.controller

import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandqaservice.api.NonSourceabilityQaApi
import org.dataland.datalandqaservice.model.NonSourceableQaReviewInformation
import org.dataland.datalandqaservice.services.NonSourceabilityQaReviewManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

/**
 * Controller for non-sourceability QA review endpoints.
 */
@RestController
class NonSourceabilityQaController(
    @Autowired private val nonSourceabilityQaReviewManager: NonSourceabilityQaReviewManager,
) : NonSourceabilityQaApi {
    override fun getNonSourceabilityReviews(
        companyId: String?,
        dataType: String?,
        reportingPeriod: String?,
        qaStatus: QaStatus?,
        showOnlyActive: Boolean,
        chunkSize: Int,
        chunkIndex: Int,
    ): ResponseEntity<List<NonSourceableQaReviewInformation>> =
        ResponseEntity.ok(
            nonSourceabilityQaReviewManager.getNonSourceabilityReviews(
                NonSourceabilityQaReviewManager.NonSourceabilityReviewQueryParams(
                    companyId = companyId,
                    dataType = dataType,
                    reportingPeriod = reportingPeriod,
                    qaStatus = qaStatus,
                    showOnlyActive = showOnlyActive,
                    chunkSize = chunkSize,
                    chunkIndex = chunkIndex,
                ),
            ),
        )

    override fun getPendingNonSourceabilityQueue(): ResponseEntity<List<NonSourceableQaReviewInformation>> =
        ResponseEntity.ok(nonSourceabilityQaReviewManager.getPendingNonSourceabilityQueue())

    override fun decideNonSourceability(
        nonSourceabilityId: String,
        qaStatus: QaStatus,
        qaComment: String?,
    ): ResponseEntity<NonSourceableQaReviewInformation> =
        ResponseEntity.ok(
            nonSourceabilityQaReviewManager.decideNonSourceability(
                nonSourceabilityId = nonSourceabilityId,
                qaStatus = qaStatus,
                qaComment = qaComment,
            ),
        )
}
