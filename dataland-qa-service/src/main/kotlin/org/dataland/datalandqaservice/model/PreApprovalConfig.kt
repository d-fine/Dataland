package org.dataland.datalandqaservice.org.dataland.datalandqaservice.model

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.QaServiceOpenApiDescriptionsAndExamples

/**
 * Holds all pre-approval configurations.
 *
 * @property exemptFields map of framework to the set of data point type identifiers that are exempt from
 * automatic pre-approval for that framework
 * @property samplingProbability the probability with which an eligible data point is randomly selected for
 * automatic pre-approval
 * @property decimalRelativeThreshold the global relative change threshold for decimal data points
 * @property integerAbsoluteThreshold the global absolute change threshold for integer data points
 * @property individualDecimalThresholds per-data-point relative threshold overrides for decimal fields
 * @property individualIntegerThresholds per-data-point absolute threshold overrides for integer fields
 * @property autoPreApprovalEnabled whether automatic pre-approval of QA-accepted data points is enabled
 * @property submitUserId the unique user ID of the reviewer or admin who last submitted this configuration
 */
data class PreApprovalConfig(
    @field:Schema(
        description = QaServiceOpenApiDescriptionsAndExamples.PRE_APPROVAL_EXEMPT_FIELDS_DESCRIPTION,
        example = QaServiceOpenApiDescriptionsAndExamples.PRE_APPROVAL_EXEMPT_FIELDS_EXAMPLE,
    )
    val exemptFields: Map<DataTypeEnum, Set<String>> = emptyMap(),
    @field:DecimalMin(value = "0.0", message = "samplingProbability must be >= 0.0")
    @field:DecimalMax(value = "1.0", message = "samplingProbability must be <= 1.0")
    @field:Schema(
        description = QaServiceOpenApiDescriptionsAndExamples.PRE_APPROVAL_SAMPLING_PROBABILITY_DESCRIPTION,
        example = QaServiceOpenApiDescriptionsAndExamples.PRE_APPROVAL_SAMPLING_PROBABILITY_EXAMPLE,
    )
    val samplingProbability: Double = 0.0,
    @field:Schema(
        description = QaServiceOpenApiDescriptionsAndExamples.PRE_APPROVAL_DECIMAL_RELATIVE_THRESHOLD_DESCRIPTION,
        example = QaServiceOpenApiDescriptionsAndExamples.PRE_APPROVAL_DECIMAL_RELATIVE_THRESHOLD_EXAMPLE,
    )
    val decimalRelativeThreshold: Double = 0.5,
    @field:Schema(
        description = QaServiceOpenApiDescriptionsAndExamples.PRE_APPROVAL_INTEGER_ABSOLUTE_THRESHOLD_DESCRIPTION,
        example = QaServiceOpenApiDescriptionsAndExamples.PRE_APPROVAL_INTEGER_ABSOLUTE_THRESHOLD_EXAMPLE,
    )
    val integerAbsoluteThreshold: Long = 5,
    @field:Schema(
        description = QaServiceOpenApiDescriptionsAndExamples.PRE_APPROVAL_INDIVIDUAL_DECIMAL_THRESHOLDS_DESCRIPTION,
        example = QaServiceOpenApiDescriptionsAndExamples.PRE_APPROVAL_INDIVIDUAL_DECIMAL_THRESHOLDS_EXAMPLE,
    )
    val individualDecimalThresholds: Map<DataTypeEnum, Map<String, Double>> = emptyMap(),
    @field:Schema(
        description = QaServiceOpenApiDescriptionsAndExamples.PRE_APPROVAL_INDIVIDUAL_INTEGER_THRESHOLDS_DESCRIPTION,
        example = QaServiceOpenApiDescriptionsAndExamples.PRE_APPROVAL_INDIVIDUAL_INTEGER_THRESHOLDS_EXAMPLE,
    )
    val individualIntegerThresholds: Map<DataTypeEnum, Map<String, Long>> = emptyMap(),
    @field:Schema(
        description = QaServiceOpenApiDescriptionsAndExamples.PRE_APPROVAL_AUTO_PRE_APPROVAL_ENABLED_DESCRIPTION,
        example = QaServiceOpenApiDescriptionsAndExamples.PRE_APPROVAL_AUTO_PRE_APPROVAL_ENABLED_EXAMPLE,
    )
    val autoPreApprovalEnabled: Boolean = true,
    @field:Schema(
        description = QaServiceOpenApiDescriptionsAndExamples.PRE_APPROVAL_SUBMIT_USER_ID_DESCRIPTION,
        example = QaServiceOpenApiDescriptionsAndExamples.PRE_APPROVAL_SUBMIT_USER_ID_EXAMPLE,
    )
    val submitUserId: String? = null,
)
