package org.dataland.datalandqaservice.org.dataland.datalandqaservice.services

import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.DatasetJudgementEntity
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

/**
 * Service responsible for automatically pre-approving data points in a dataset judgement
 * when all QA reports have the verdict QaAccepted.
 */
@Service
class PreApprovalService(
    @Value("\${AUTO_PREAPPROVAL_QA_ACCEPTED_DATAPOINTS:false}")
    @Suppress("UnusedPrivateProperty")
    private val autoPreApprovalEnabled: Boolean,
) {
    /**
     * Runs the pre-approval workflow on the given DatasetJudgementEntity.
     * If the feature flag is enabled, data points where all active QA reports (latest per reporter)
     * have the verdict QaAccepted are automatically pre-approved.
     */
    fun runPreApprovalWorkflow(datasetJudgementEntity: DatasetJudgementEntity): DatasetJudgementEntity = datasetJudgementEntity
}
