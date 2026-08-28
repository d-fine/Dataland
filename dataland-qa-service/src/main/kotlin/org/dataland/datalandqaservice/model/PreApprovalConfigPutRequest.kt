package org.dataland.datalandqaservice.org.dataland.datalandqaservice.model

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.QaServiceOpenApiDescriptionsAndExamples

/**
 * A full replacement of the pre-approval configuration. All fields are required (no defaults).
 *
 * This request model intentionally does not include `submitUserId`, which is always set server-side from the
 * authenticated principal and can never be modified via this request.
 */
data class PreApprovalConfigPutRequest(
    @field:Schema(
        description = QaServiceOpenApiDescriptionsAndExamples.PRE_APPROVAL_EXEMPT_FIELDS_DESCRIPTION,
        example = QaServiceOpenApiDescriptionsAndExamples.PRE_APPROVAL_EXEMPT_FIELDS_EXAMPLE,
    )
    val exemptFields: Map<DataTypeEnum, Set<String>>,
    @field:DecimalMin(value = "0.0", message = "samplingProbability must be >= 0.0")
    @field:DecimalMax(value = "1.0", message = "samplingProbability must be <= 1.0")
    @field:Schema(
        description = QaServiceOpenApiDescriptionsAndExamples.PRE_APPROVAL_SAMPLING_PROBABILITY_DESCRIPTION,
        example = QaServiceOpenApiDescriptionsAndExamples.PRE_APPROVAL_SAMPLING_PROBABILITY_EXAMPLE,
    )
    val samplingProbability: Double,
    @field:Schema(
        description = QaServiceOpenApiDescriptionsAndExamples.PRE_APPROVAL_DECIMAL_RELATIVE_THRESHOLD_DESCRIPTION,
        example = QaServiceOpenApiDescriptionsAndExamples.PRE_APPROVAL_DECIMAL_RELATIVE_THRESHOLD_EXAMPLE,
    )
    val decimalRelativeThreshold: Double,
    @field:Schema(
        description = QaServiceOpenApiDescriptionsAndExamples.PRE_APPROVAL_INTEGER_ABSOLUTE_THRESHOLD_DESCRIPTION,
        example = QaServiceOpenApiDescriptionsAndExamples.PRE_APPROVAL_INTEGER_ABSOLUTE_THRESHOLD_EXAMPLE,
    )
    val integerAbsoluteThreshold: Long,
    @field:Schema(
        description = QaServiceOpenApiDescriptionsAndExamples.PRE_APPROVAL_INDIVIDUAL_DECIMAL_THRESHOLDS_DESCRIPTION,
        example = QaServiceOpenApiDescriptionsAndExamples.PRE_APPROVAL_INDIVIDUAL_DECIMAL_THRESHOLDS_EXAMPLE,
    )
    val individualDecimalThresholds: Map<DataTypeEnum, Map<String, Double>>,
    @field:Schema(
        description = QaServiceOpenApiDescriptionsAndExamples.PRE_APPROVAL_INDIVIDUAL_INTEGER_THRESHOLDS_DESCRIPTION,
        example = QaServiceOpenApiDescriptionsAndExamples.PRE_APPROVAL_INDIVIDUAL_INTEGER_THRESHOLDS_EXAMPLE,
    )
    val individualIntegerThresholds: Map<DataTypeEnum, Map<String, Long>>,
    @field:Schema(
        description = QaServiceOpenApiDescriptionsAndExamples.PRE_APPROVAL_AUTO_PRE_APPROVAL_ENABLED_DESCRIPTION,
        example = QaServiceOpenApiDescriptionsAndExamples.PRE_APPROVAL_AUTO_PRE_APPROVAL_ENABLED_EXAMPLE,
    )
    val autoPreApprovalEnabled: Boolean,
)
