package org.dataland.datalandqaservice.org.dataland.datalandqaservice.services

import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandqaservice.configurations.PreApprovalExemptFieldsConfig
import org.dataland.datalandqaservice.model.reports.AcceptedDataPointSource
import org.dataland.datalandqaservice.model.reports.QaReportDataPointVerdict
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.DataPointJudgementEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.DatasetJudgementEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.PreApprovalConfig
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import kotlin.random.Random

/**
 * Service responsible for automatically pre-approving data points in a dataset judgement
 * when all QA reports have the verdict QaAccepted.
 */
@Service
class PreApprovalService(
    @Value("\${dataland.qa-service.auto-preapproval-qa-accepted-datapoints}")
    private val autoPreApprovalEnabled: Boolean,
    private val exemptFieldsConfig: PreApprovalExemptFieldsConfig,
) {
    private var config: PreApprovalConfig = PreApprovalConfig()

    /**
     * Returns the current pre-approval configuration.
     */
    fun getConfig(): PreApprovalConfig = config

    /**
     * Updates the pre-approval configuration with the given patch and returns the updated config.
     * @throws IllegalArgumentException if samplingProbability is outside [0.0, 1.0]
     */
    fun patchConfig(newConfig: PreApprovalConfig): PreApprovalConfig {
        require(newConfig.samplingProbability >= 0.0) {
            "samplingProbability must be >= 0.0, but was ${newConfig.samplingProbability}"
        }
        require(newConfig.samplingProbability <= 1.0) {
            "samplingProbability must be <= 1.0, but was ${newConfig.samplingProbability}"
        }
        config = newConfig
        return config
    }

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
            val isNotExemptField = isDataPointNotExempt(dataPoint, datasetJudgementEntity.dataType)
            val isNotSelectedBySampling = !isSelectedBySampling()

            val allChecksPass =
                listOf(
                    allQaReportsAccepted,
                    isNotExemptField,
                    isNotSelectedBySampling,
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

    /**
     * Checks whether the given data point is not on the exempt fields list for its framework.
     *
     * @param dataPoint the data point to check
     * @param dataType the framework (data type) of the current review
     * @return `true` if the data point is not exempt, `false` if it is exempt
     */
    private fun isDataPointNotExempt(
        dataPoint: DataPointJudgementEntity,
        dataType: DataTypeEnum,
    ): Boolean {
        val exemptFieldsForFramework = exemptFieldsConfig.exemptFields[dataType] ?: emptySet()
        return dataPoint.dataPointType !in exemptFieldsForFramework
    }

    /**
     * Determines whether a data point is selected by the sampling algorithm.
     * A data point is selected (and thus excluded from auto pre-approval) if a random number
     * between 0 and 1 is less than or equal to the configured sampling probability.
     *
     * @return `true` if the data point is selected by sampling (should NOT be pre-approved),
     *         `false` otherwise
     */
    private fun isSelectedBySampling(): Boolean {
        val samplingProbability = getConfig().samplingProbability
        return Random.nextDouble() <= samplingProbability
    }
}
