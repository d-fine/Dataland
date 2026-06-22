package org.dataland.datalandqaservice.org.dataland.datalandqaservice.services

import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandqaservice.configurations.PreApprovalExemptFieldsConfig
import org.dataland.datalandqaservice.model.reports.AcceptedDataPointSource
import org.dataland.datalandqaservice.model.reports.QaReportDataPointVerdict
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.DataPointJudgementEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.DatasetJudgementEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.PreApprovalCheckResults
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.PreApprovalConfig
import org.slf4j.LoggerFactory
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
    private val significanceCheckService: SignificanceCheckService,
    private val datasetJudgementSupportService: DatasetJudgementSupportService,
) {
    companion object {
        private val logger = LoggerFactory.getLogger(PreApprovalService::class.java)
    }

    private var _config: PreApprovalConfig = PreApprovalConfig()
    val config: PreApprovalConfig
        get() = _config

    /**
     * Updates the pre-approval configuration with the given patch and returns the updated config.
     */
    fun patchConfig(newConfig: PreApprovalConfig): PreApprovalConfig {
        _config = newConfig
        return _config
    }

    /**
     * Pre-approves data points of a given DatasetJudgementEntity.
     *
     * If the feature flag is disabled, the given DatasetJudgementEntity is returned unchanged.
     * If the feature flag is enabled, data points that pass all of the following checks are
     * pre-approved by setting their acceptedSource to Original:
     * - All QA reports for the data point have the verdict QaAccepted.
     * - The data point is not on the exempt fields list for the framework.
     * - The data point is not excluded by random sampling.
     * - The change in value compared to the currently live dataset is not significant.
     */
    fun preApproveDataPoints(datasetJudgementEntity: DatasetJudgementEntity): DatasetJudgementEntity {
        if (!autoPreApprovalEnabled) return datasetJudgementEntity

        val liveDataPoints =
            datasetJudgementSupportService.getDataPointsOfLatestActiveDataset(
                datasetJudgementEntity.companyId,
                datasetJudgementEntity.dataType,
            )

        datasetJudgementEntity.dataPoints.forEach { dataPointJudgement ->
            val allQaReportsAccepted = areAllQaReportsAccepted(dataPointJudgement)
            val dataPointEligible = isDataPointEligible(dataPointJudgement, datasetJudgementEntity.dataType)
            val passesRandomSampling = !isRandomDrawBelowSamplingProbability()
            val passesSignificanceCheck =
                passesSignificanceCheck(dataPointJudgement, datasetJudgementEntity.dataType, liveDataPoints)

            dataPointJudgement.preApprovalCheckResults =
                PreApprovalCheckResults(
                    areAllQaReportsAccepted = allQaReportsAccepted,
                    isDataPointEligible = dataPointEligible,
                    passesRandomSampling = passesRandomSampling,
                    passesSignificanceCheck = passesSignificanceCheck,
                )

            dataPointJudgement.preApprovalCheckResults
                ?.takeIf { it.passes() }
                ?.let { dataPointJudgement.acceptedSource = AcceptedDataPointSource.Original }
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
    private fun isDataPointEligible(
        dataPoint: DataPointJudgementEntity,
        dataType: DataTypeEnum,
    ): Boolean =
        !exemptFieldsConfig.exemptFields
            .getOrDefault(dataType, emptySet())
            .contains(dataPoint.dataPointType)

    /**
     * Checks whether a random draw is below the configured sampling probability.
     *
     * @return `true` if a random number between 0 and 1 is smaller than the configured sampling probability,
     *         `false` otherwise
     */
    private fun isRandomDrawBelowSamplingProbability(): Boolean {
        val samplingProbability = _config.samplingProbability
        return Random.nextDouble() < samplingProbability
    }

    /**
     * Checks whether the change in a data point's value compared to the currently live dataset
     * is not significant enough to suppress pre-approval.
     *
     * Returns true (allow pre-approval) in any of the following cases:
     * - No live dataset exists for the company and framework.
     * - The live dataset does not contain this data point type.
     * - Either the original or the live value is null.
     * - The change is below the significance threshold for the data point's value type.
     *
     * Returns false (suppress pre-approval) only when the change is considered significant.
     *
     * @param dataPoint the data point under review
     * @param dataType the framework of the dataset being reviewed
     * @param liveDataPoints map of data point type to data point id for the live dataset, or null
     * @return `true` if pre-approval should be allowed, `false` if it should be suppressed
     */
    private fun passesSignificanceCheck(
        dataPoint: DataPointJudgementEntity,
        dataType: DataTypeEnum,
        liveDataPoints: Map<String, String>?,
    ): Boolean {
        val liveDataPointId = liveDataPoints?.get(dataPoint.dataPointType)
        if (liveDataPointId == null) {
            logger.info(
                "Automatic preapproval significance check skipped. " +
                    "dataType={}, dataPointType={}, dataPointId={}, liveDatasetPresent={}",
                dataType,
                dataPoint.dataPointType,
                dataPoint.dataPointId,
                liveDataPoints != null,
            )
            return true
        }

        val newValue = datasetJudgementSupportService.getDataPointValueNode(dataPoint.dataPointId)
        val liveValue = datasetJudgementSupportService.getDataPointValueNode(liveDataPointId)

        val baseTypeId = datasetJudgementSupportService.resolveBaseTypeId(dataPoint.dataPointType)
        val valueType = significanceCheckService.resolveValueType(baseTypeId)

        val hasSignificantChange =
            significanceCheckService.hasSignificantChange(
                newValue = newValue,
                liveValue = liveValue,
                valueType = valueType,
                dataPointType = dataPoint.dataPointType,
                framework = dataType,
            )

        val passesSignificanceCheck = !hasSignificantChange
        return passesSignificanceCheck
    }
}
