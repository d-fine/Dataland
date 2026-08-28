package org.dataland.datalandqaservice.org.dataland.datalandqaservice.services

import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackendutils.exceptions.InternalServerErrorApiException
import org.dataland.datalandqaservice.model.reports.AcceptedDataPointSource
import org.dataland.datalandqaservice.model.reports.QaReportDataPointVerdict
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.DataPointJudgementEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.DatasetJudgementEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.entities.QaConfigEntity
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.PreApprovalCheckResults
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.PreApprovalConfig
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.PreApprovalConfigPatchRequest
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.PreApprovalConfigPutRequest
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.repositories.QaConfigRepository
import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import kotlin.random.Random

/**
 * Service responsible for automatically pre-approving data points in a dataset judgement.
 */
@Service
class PreApprovalService(
    private val qaConfigRepository: QaConfigRepository,
    private val significanceCheckService: SignificanceCheckService,
    private val datasetJudgementSupportService: DatasetJudgementSupportService,
) {
    companion object {
        private val logger = LoggerFactory.getLogger(PreApprovalService::class.java)
    }

    @Volatile
    private var _config: PreApprovalConfig? = null

    /**
     * The persisted pre-approval configuration.
     *
     * @throws InternalServerErrorApiException if the configuration could not be loaded from the database
     *         (see [initializeConfig]). Reading/writing the configuration itself (via the `/qa/pre-approval/config`
     *         endpoints) is an explicit admin action, so callers should be told clearly that it is unavailable
     *         rather than being handed a stale or default value.
     */
    val config: PreApprovalConfig
        get() = _config ?: throw configUnavailableException()

    /**
     * Loads the persisted pre-approval configuration from the database once the application context is
     * fully ready.
     *
     * The qa_config table is guaranteed by its Flyway migration to always contain exactly one row. If no row
     * is found, this indicates a broken persistence guarantee, logged as an error with full context so the underlying
     * problem is not missed. [_config] is simply left unset; [preApproveDataPoints] and the config read/write
     * operations each check for this and degrade or fail only within their own, narrow scope -
     * everything else in the QA service keeps working unaffected.
     *
     * This is deliberately hooked into [ApplicationReadyEvent] rather than `@PostConstruct`: it needs to
     * run after the whole application context (including the datasource/schema) has finished
     * initializing, not merely after this bean's own dependencies have been constructed.
     */
    @EventListener(ApplicationReadyEvent::class)
    fun initializeConfig() {
        val entity = qaConfigRepository.findById(QaConfigEntity.QA_CONFIG_SINGLETON_ID).orElse(null)
        if (entity == null) {
            logger.error(
                "No qa_config row found for the singleton id {}. The qa_config table is expected to always " +
                    "contain exactly one row, seeded by its Flyway migration. A missing row indicates a broken " +
                    "persistence guarantee (failed/skipped migration, deleted row, or wrong database). " +
                    "Auto pre-approval and the pre-approval config endpoints will be unavailable until this " +
                    "is fixed and the service is restarted; the rest of the QA service is unaffected.",
                QaConfigEntity.QA_CONFIG_SINGLETON_ID,
            )
            return
        }
        _config = entity.config
    }

    /**
     * Builds the exception thrown when a caller explicitly tries to read or write the pre-approval
     * configuration (e.g. via the `/qa/pre-approval/config` endpoints) while it could not be loaded from
     * the database at startup. See [initializeConfig] for why this does not crash the service.
     */
    private fun configUnavailableException() =
        InternalServerErrorApiException(
            publicSummary = "Pre-approval configuration unavailable",
            publicMessage =
                "The pre-approval configuration could not be loaded from the database.",
            internalMessage =
                "PreApprovalService._config is unset because no qa_config row was found for the singleton id " +
                    "${QaConfigEntity.QA_CONFIG_SINGLETON_ID} at startup. See the error logged by " +
                    "initializeConfig() for details.",
        )

    /**
     * Merges the provided patch onto the current configuration (only non-null fields of [patch] are applied,
     * leaving all other fields untouched), sets [submitUserId] server-side, and persists the result.
     *
     * If the resulting configuration does not actually differ from the current one (ignoring submitUserId),
     * no database write is performed, to avoid unnecessary writes and Envers revision noise.
     *
     * @throws InternalServerErrorApiException if the configuration could not be loaded from the database
     *         at startup (see [initializeConfig]) - there is no known current configuration to merge onto.
     */
    fun patchConfig(
        patch: PreApprovalConfigPatchRequest,
        submitUserId: String,
    ): PreApprovalConfig {
        val current = config
        val merged =
            current.copy(
                exemptFields = patch.exemptFields ?: current.exemptFields,
                samplingProbability = patch.samplingProbability ?: current.samplingProbability,
                decimalRelativeThreshold = patch.decimalRelativeThreshold ?: current.decimalRelativeThreshold,
                integerAbsoluteThreshold = patch.integerAbsoluteThreshold ?: current.integerAbsoluteThreshold,
                individualDecimalThresholds = patch.individualDecimalThresholds ?: current.individualDecimalThresholds,
                individualIntegerThresholds = patch.individualIntegerThresholds ?: current.individualIntegerThresholds,
                autoPreApprovalEnabled = patch.autoPreApprovalEnabled ?: current.autoPreApprovalEnabled,
                submitUserId = submitUserId,
            )
        return persistIfChanged(current, merged)
    }

    /**
     * Fully replaces the current configuration with [newConfig], sets [submitUserId] server-side, and persists
     * the result.
     *
     * If the resulting configuration does not actually differ from the current one (ignoring submitUserId),
     * no database write is performed, to avoid unnecessary writes and Envers revision noise.
     *
     * @throws InternalServerErrorApiException if the configuration could not be loaded from the database
     *         at startup (see [initializeConfig]) - there is no known current configuration to compare against.
     */
    fun putConfig(
        newConfig: PreApprovalConfigPutRequest,
        submitUserId: String,
    ): PreApprovalConfig {
        val current = config
        val replaced =
            PreApprovalConfig(
                exemptFields = newConfig.exemptFields,
                samplingProbability = newConfig.samplingProbability,
                decimalRelativeThreshold = newConfig.decimalRelativeThreshold,
                integerAbsoluteThreshold = newConfig.integerAbsoluteThreshold,
                individualDecimalThresholds = newConfig.individualDecimalThresholds,
                individualIntegerThresholds = newConfig.individualIntegerThresholds,
                autoPreApprovalEnabled = newConfig.autoPreApprovalEnabled,
                submitUserId = submitUserId,
            )
        return persistIfChanged(current, replaced)
    }

    /**
     * Persists [updated] only if it actually differs from [current] (ignoring submitUserId). Updates the
     * in-memory [_config] either way to the value that should now be considered current.
     */
    private fun persistIfChanged(
        current: PreApprovalConfig,
        updated: PreApprovalConfig,
    ): PreApprovalConfig {
        if (current.copy(submitUserId = null) == updated.copy(submitUserId = null)) {
            return current
        }

        val entity =
            qaConfigRepository.findById(QaConfigEntity.QA_CONFIG_SINGLETON_ID).orElseThrow {
                IllegalStateException(
                    "No qa_config row found for the singleton id ${QaConfigEntity.QA_CONFIG_SINGLETON_ID} " +
                        "while attempting to persist an updated configuration.",
                )
            }
        entity.config = updated
        qaConfigRepository.save(entity)
        _config = updated
        return updated
    }

    /**
     * Pre-approves data points of a given DatasetJudgementEntity.
     *
     * If the pre-approval configuration could not be loaded from the database at startup (see
     * [initializeConfig]), auto pre-approval is treated as unavailable: this method logs an error and returns
     * the given DatasetJudgementEntity unchanged, without throwing. Dataset judgement creation (the caller of
     * this method) must keep working even when the pre-approval subsystem's configuration is broken; only the
     * automatic pre-approval step is skipped, and it becomes apparent that pre-approval did not run because no
     * data points end up pre-approved.
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
        val config = _config
        if (config == null) {
            logger.error(
                "Skipping automatic pre-approval for datasetJudgementId={}: the pre-approval configuration " +
                    "is unavailable (see the error logged by initializeConfig() at startup). Dataset judgement " +
                    "creation proceeds without automatic pre-approval.",
                datasetJudgementEntity.dataSetJudgementId,
            )
        }
        if (config == null || !config.autoPreApprovalEnabled) return datasetJudgementEntity

        val liveDataPoints =
            datasetJudgementSupportService.getDataPointsOfLatestActiveDataset(
                datasetJudgementEntity.companyId,
                datasetJudgementEntity.dataType,
            )

        datasetJudgementEntity.dataPoints.forEach { dataPointJudgement ->
            val allQaReportsAccepted = areAllQaReportsAccepted(dataPointJudgement)
            val dataPointEligible = isDataPointEligible(dataPointJudgement, datasetJudgementEntity.dataType, config)
            val passesRandomSampling = !isRandomDrawBelowSamplingProbability(config)
            val passesSignificanceCheck =
                passesSignificanceCheck(dataPointJudgement, datasetJudgementEntity.dataType, liveDataPoints, config)

            dataPointJudgement.preApprovalCheckResults =
                PreApprovalCheckResults(
                    areAllQaReportsAccepted = allQaReportsAccepted,
                    dataPointEligible = dataPointEligible,
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
     * @param config the pre-approval configuration to check the exempt fields list against
     * @return `true` if the data point is not exempt, `false` if it is exempt
     */
    private fun isDataPointEligible(
        dataPoint: DataPointJudgementEntity,
        dataType: DataTypeEnum,
        config: PreApprovalConfig,
    ): Boolean =
        !config.exemptFields
            .getOrDefault(dataType, emptySet())
            .contains(dataPoint.dataPointType)

    /**
     * Checks whether a random draw is below the configured sampling probability.
     *
     * @param config the pre-approval configuration to read the sampling probability from
     * @return `true` if a random number between 0 and 1 is smaller than the configured sampling probability,
     *         `false` otherwise
     */
    private fun isRandomDrawBelowSamplingProbability(config: PreApprovalConfig): Boolean {
        val samplingProbability = config.samplingProbability
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
     * @param config the pre-approval configuration to read the significance thresholds from
     * @return `true` if pre-approval should be allowed, `false` if it should be suppressed
     */
    private fun passesSignificanceCheck(
        dataPoint: DataPointJudgementEntity,
        dataType: DataTypeEnum,
        liveDataPoints: Map<String, String>?,
        config: PreApprovalConfig,
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
                thresholds =
                    SignificanceCheckService.SignificanceThresholds(
                        decimalRelativeThreshold = config.decimalRelativeThreshold,
                        integerAbsoluteThreshold = config.integerAbsoluteThreshold,
                        individualDecimalThresholds = config.individualDecimalThresholds,
                        individualIntegerThresholds = config.individualIntegerThresholds,
                    ),
            )

        val passesSignificanceCheck = !hasSignificantChange
        return passesSignificanceCheck
    }
}
