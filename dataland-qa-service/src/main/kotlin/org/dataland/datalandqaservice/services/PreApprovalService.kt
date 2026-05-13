package org.dataland.datalandqaservice.org.dataland.datalandqaservice.services

import org.dataland.datalandqaservice.model.reports.AcceptedDataPointSource
import org.dataland.datalandqaservice.model.reports.QaReportDataPointVerdict
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.DatasetJudgementEntity
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

/**
 * Service responsible for automatically pre-approving data points in a dataset judgement
 * when all QA reports have the verdict QaAccepted.
 */
@Service
class PreApprovalService(
    @Value("\${AUTO_PREAPPROVAL_QA_ACCEPTED_DATAPOINTS:true}")
    private val autoPreApprovalEnabled: Boolean,
) {
    /**
     * Runs the pre-approval workflow on the given DatasetJudgementEntity.
     * If the feature flag is enabled, data points where all active QA reports (latest per reporter)
     * have the verdict QaAccepted are automatically pre-approved.
     *
     * The logic is structured so that for each data point multiple checks can be added easily:
     *  - each check sets a Boolean
     *  - at the end all Booleans are combined
     */
    fun runPreApprovalWorkflow(datasetJudgementEntity: DatasetJudgementEntity): DatasetJudgementEntity {
        if (!autoPreApprovalEnabled) return datasetJudgementEntity

        datasetJudgementEntity.dataPoints.forEach { dataPoint ->

            // Check if all QA reports are QaAccepted
            val qaReportsForDataPoint = dataPoint.qaReports

            val allQaReportsAccepted =
                qaReportsForDataPoint.isNotEmpty() &&
                    qaReportsForDataPoint.all { it.verdict == QaReportDataPointVerdict.QaAccepted }

            val allChecksPass =
                listOf(
                    allQaReportsAccepted,
                ).all { it }

            if (allChecksPass) {
                dataPoint.acceptedSource = AcceptedDataPointSource.Original
            }
        }

        return datasetJudgementEntity
    }
}
