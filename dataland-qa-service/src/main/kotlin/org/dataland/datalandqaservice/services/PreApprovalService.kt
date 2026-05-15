package org.dataland.datalandqaservice.org.dataland.datalandqaservice.services

import org.dataland.datalandqaservice.model.reports.AcceptedDataPointSource
import org.dataland.datalandqaservice.model.reports.QaReportDataPointVerdict
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.DataPointJudgementEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.DatasetJudgementEntity
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

/**
 * Service responsible for automatically pre-approving data points in a dataset judgement
 * when all QA reports have the verdict QaAccepted.
 */
@Service
class PreApprovalService(
    @Value("\${dataland.qa-service.auto-preapproval-qa-accepted-datapoints}")
    private val autoPreApprovalEnabled: Boolean,
) {
    /**
     * Runs the pre-approval workflow on the given DatasetJudgementEntity.
     * If the feature flag is enabled, data points where all active QA reports
     * have the verdict QaAccepted are automatically pre-approved.
     *
     * The logic is structured so that for each data point multiple checks can be added easily:
     *  - each check sets a Boolean
     *  - at the end all Booleans are combined
     */
    fun runPreApprovalWorkflow(datasetJudgementEntity: DatasetJudgementEntity): DatasetJudgementEntity {
        if (!autoPreApprovalEnabled) return datasetJudgementEntity

        datasetJudgementEntity.dataPoints.forEach { dataPoint ->

            val allQaReportsAccepted = areAllQaReportsAccepted(dataPoint)

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

    /**
     * A helper function that checks whether a given datapoint qualifies for QA-based pre-approval.
     *
     * A data point qualifies if:
     * - it has at least one QA report, and
     * - all QA reports for this data point have the verdict QaAccepted.
     *
     * @param dataPoint the data point whose QA reports should be evaluated
     * @return `true` if all QA reports are QaAccepted and there is at least one report,
     *         `false` otherwise
     */
    private fun areAllQaReportsAccepted(dataPoint: DataPointJudgementEntity): Boolean {
        val qaReportsForDataPoint = dataPoint.qaReports.filter { it.active }

        return qaReportsForDataPoint.isNotEmpty() &&
            qaReportsForDataPoint.all { it.verdict == QaReportDataPointVerdict.QaAccepted }
    }
}
