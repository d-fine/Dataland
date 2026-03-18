package org.dataland.datalandqaservice.org.dataland.datalandqaservice.controller

import org.dataland.datalandbackendutils.utils.ValidationUtils.convertToUUID
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.api.DatasetJudgementApi
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.DatasetJudgementResponse
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.DatasetJudgementState
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.reports.JudgementDetailsPatch
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.services.DatasetJudgementService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

/**
 * Controller for the dataset judgement service.
 */
@RestController
class DatasetJudgementController(
    @Autowired private val datasetJudgementService: DatasetJudgementService,
) : DatasetJudgementApi {
    override fun getDatasetJudgement(datasetJudgementId: String): ResponseEntity<DatasetJudgementResponse> =
        ResponseEntity
            .ok(
                datasetJudgementService
                    .getDatasetJudgementById(convertToUUID(datasetJudgementId)),
            )

    override fun postDatasetJudgement(datasetId: String): ResponseEntity<DatasetJudgementResponse> =
        ResponseEntity
            .status(HttpStatus.CREATED)
            .body(datasetJudgementService.postDatasetJudgement(convertToUUID(datasetId)))

    override fun getDatasetJudgementsByDatasetId(datasetId: String): ResponseEntity<List<DatasetJudgementResponse>> =
        ResponseEntity.ok(datasetJudgementService.getDatasetJudgementsByDatasetId(convertToUUID(datasetId)))

    override fun setJudge(datasetJudgementId: String): ResponseEntity<DatasetJudgementResponse> =
        ResponseEntity.ok(datasetJudgementService.setJudge(convertToUUID(datasetJudgementId)))

    override fun setJudgementState(
        datasetJudgementId: String,
        state: DatasetJudgementState,
    ): ResponseEntity<DatasetJudgementResponse> =
        ResponseEntity
            .ok(
                datasetJudgementService.setJudgementState(
                    convertToUUID(datasetJudgementId),
                    state,
                ),
            )

    override fun patchJudgementDetails(
        datasetJudgementId: String,
        dataPointType: String,
        patch: JudgementDetailsPatch,
    ): ResponseEntity<DatasetJudgementResponse> {
        patch.reporterUserIdOfAcceptedQaReport?.let { convertToUUID(it) }
        return ResponseEntity
            .ok(
                datasetJudgementService.patchJudgementDetails(
                    convertToUUID(datasetJudgementId),
                    dataPointType,
                    patch,
                ),
            )
    }
}
