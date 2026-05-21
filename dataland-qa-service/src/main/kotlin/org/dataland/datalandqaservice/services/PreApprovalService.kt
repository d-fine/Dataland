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
 * Service responsible for automatically pre-approving data points in a dataset judgement.
 */
@Service
class PreApprovalService(
    @Value("\${dataland.qa-service.auto-preapproval-qa-accepted-datapoints}")
    private val autoPreApprovalEnabled: Boolean,
    private val exemptFieldsConfig: PreApprovalExemptFieldsConfig,
) {
    private var _config: PreApprovalConfig = PreApprovalConfig()
    val config: PreApprovalConfig
        get() = _config

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
        _config = newConfig
        return _config
    }

    /**
     * Pre-approves datapoints of a given DatasetJudgementEntity.
     *
     * If the feature flag is enabled, data points where all QA reports have the verdict QaAccepted are pre-approved.
     * If the feature flag is disabled, the given DatasetJudgementEntity is returned unchanged.
     */
    fun preApproveDataPoints(datasetJudgementEntity: DatasetJudgementEntity): DatasetJudgementEntity {
        if (!autoPreApprovalEnabled) return datasetJudgementEntity

        datasetJudgementEntity.dataPoints.forEach { dataPoint ->

            val allChecksPass =
                listOf(
                    areAllQaReportsAccepted(dataPoint),
                    isDataPointNotExempt(dataPoint, datasetJudgementEntity.dataType),
                    !isSelectedBySampling(),
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
    private fun areAllQaReportsAccepted(dataPoint: DataPointJudgementEntity): Boolean =
        dataPoint.qaReports.toList().let { qaReportsForDataPoint ->
            qaReportsForDataPoint.isNotEmpty() &&
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
    ): Boolean =
        !exemptFieldsConfig.exemptFields
            .getOrDefault(dataType, emptySet())
            .contains(dataPoint.dataPointType)

    /**
     * Determines whether a data point is selected by the sampling algorithm.
     * A data point is selected (and thus excluded from auto pre-approval) if a random number
     * between 0 and 1 is less than or equal to the configured sampling probability.
     *
     * @return `true` if the data point is selected by sampling (should NOT be pre-approved),
     *         `false` otherwise
     */
    private fun isSelectedBySampling(): Boolean {
        val samplingProbability = _config.samplingProbability
        return Random.nextDouble() <= samplingProbability
    }
}
