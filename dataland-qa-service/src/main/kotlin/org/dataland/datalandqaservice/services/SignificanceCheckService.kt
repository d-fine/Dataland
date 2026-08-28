package org.dataland.datalandqaservice.org.dataland.datalandqaservice.services

import com.fasterxml.jackson.databind.JsonNode
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode

/**
 * Pure service for checking whether the change between two data point values is considered
 * significant according to configurable thresholds per value type.
 *
 * A significant change suppresses automatic pre-approval for that data point, requiring manual
 * review by a QA judge instead.
 *
 * Thresholds are passed in per call, backed by the persisted
 * [org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.PreApprovalConfig]:
 * - Boolean: any change is significant.
 * - Decimal: a relative change of more than the given decimal threshold is significant.
 * - Integer: an absolute change of more than the given integer threshold is significant.
 *
 * Individual per-data-point threshold overrides per framework can be supplied via
 * [SignificanceThresholds.individualDecimalThresholds] and [SignificanceThresholds.individualIntegerThresholds]
 * if needed.
 */
@Service
class SignificanceCheckService {
    /**
     * Categorizes data point value types for significance threshold evaluation.
     *
     * - BOOLEAN: Any change is considered significant.
     * - DECIMAL: Relative change is evaluated against the configured decimal threshold.
     * - INTEGER: Absolute change is evaluated against the configured integer threshold.
     * - UNSUPPORTED: Unknown types that are never considered significant.
     */
    enum class ValueType { BOOLEAN, DECIMAL, INTEGER, UNSUPPORTED }

    /**
     * Bundles the significance thresholds needed to evaluate whether a change in a data point's
     * value is significant. Grouped into a single type because these values are always sourced
     * and passed together (from [org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.PreApprovalConfig]).
     *
     * @property decimalRelativeThreshold the global relative change threshold for decimal data points
     * @property integerAbsoluteThreshold the global absolute change threshold for integer data points
     * @property individualDecimalThresholds per-data-point relative threshold overrides for decimal fields,
     * keyed by framework and data point type. If absent for a given field, [decimalRelativeThreshold] is used.
     * @property individualIntegerThresholds per-data-point absolute threshold overrides for integer fields,
     * keyed by framework and data point type. If absent for a given field, [integerAbsoluteThreshold] is used.
     */
    data class SignificanceThresholds(
        val decimalRelativeThreshold: Double,
        val integerAbsoluteThreshold: Long,
        val individualDecimalThresholds: Map<DataTypeEnum, Map<String, Double>>,
        val individualIntegerThresholds: Map<DataTypeEnum, Map<String, Long>>,
    )

    companion object {
        private val DECIMAL_BASE_TYPE_IDS = setOf("extendedDecimal")
        private val INTEGER_BASE_TYPE_IDS = setOf("extendedInteger")
        private val BOOLEAN_BASE_TYPE_IDS = setOf("extendedEnumYesNo")

        private const val DECIMAL_DIVISION_SCALE = 10
    }

    /**
     * Resolves a data point base type id (from the specification service) to a [ValueType] category.
     *
     * @param baseTypeId The id of the data point base type (e.g. "extendedDecimal").
     * @return The corresponding [ValueType].
     */
    fun resolveValueType(baseTypeId: String): ValueType =
        when (baseTypeId) {
            in DECIMAL_BASE_TYPE_IDS -> ValueType.DECIMAL
            in INTEGER_BASE_TYPE_IDS -> ValueType.INTEGER
            in BOOLEAN_BASE_TYPE_IDS -> ValueType.BOOLEAN
            else -> ValueType.UNSUPPORTED
        }

    /**
     * Checks whether the change between the original and live value of a data point is significant.
     *
     * Returns false (not significant — allow pre-approval) in the following cases:
     * - Either value is null or an explicit JSON null.
     * - The value type is [ValueType.UNSUPPORTED].
     *
     * @param newValue The value node of the data point in the dataset under review.
     * @param liveValue The value node of the same data point in the currently live dataset.
     * @param valueType The value type category of the data point.
     * @param dataPointType The data point type identifier (used for per-data-point threshold lookups).
     * @param framework The framework of the dataset (used for per-data-point threshold lookups).
     * @param thresholds The significance thresholds to evaluate the change against.
     * @return true if the change is significant and auto pre-approval should be suppressed; false otherwise.
     */
    fun hasSignificantChange(
        newValue: JsonNode?,
        liveValue: JsonNode?,
        valueType: ValueType,
        dataPointType: String,
        framework: DataTypeEnum,
        thresholds: SignificanceThresholds,
    ): Boolean {
        val newVal = newValue?.takeUnless { it.isNull }
        val live = liveValue?.takeUnless { it.isNull }
        if (newVal == null || live == null) return false

        return when (valueType) {
            ValueType.BOOLEAN -> newVal.asText() != live.asText()
            ValueType.DECIMAL -> isDecimalChangeSignificant(newVal, live, dataPointType, framework, thresholds)
            ValueType.INTEGER -> isIntegerChangeSignificant(newVal, live, dataPointType, framework, thresholds)
            ValueType.UNSUPPORTED -> false
        }
    }

    private fun isDecimalChangeSignificant(
        newValue: JsonNode,
        liveValue: JsonNode,
        dataPointType: String,
        framework: DataTypeEnum,
        thresholds: SignificanceThresholds,
    ): Boolean {
        val original = newValue.decimalValueOrNull()
        val live = liveValue.decimalValueOrNull()
        if (original == null || live == null) {
            return false
        }
        val threshold = getDecimalThreshold(dataPointType, framework, thresholds)

        return if (live.compareTo(BigDecimal.ZERO) == 0) {
            original.compareTo(BigDecimal.ZERO) != 0
        } else {
            val relativeChange =
                original
                    .subtract(live)
                    .abs()
                    .divide(live.abs(), DECIMAL_DIVISION_SCALE, RoundingMode.HALF_UP)
            relativeChange > threshold
        }
    }

    private fun isIntegerChangeSignificant(
        newValue: JsonNode,
        liveValue: JsonNode,
        dataPointType: String,
        framework: DataTypeEnum,
        thresholds: SignificanceThresholds,
    ): Boolean {
        val original = newValue.bigIntegerValueOrNull()
        val live = liveValue.bigIntegerValueOrNull()
        if (original == null || live == null) return false
        val threshold = getIntegerThreshold(dataPointType, framework, thresholds)

        return original.subtract(live).abs() > threshold
    }

    private fun JsonNode.decimalValueOrNull(): BigDecimal? = if (isNumber) decimalValue() else null

    private fun JsonNode.bigIntegerValueOrNull(): BigInteger? = if (isIntegralNumber) bigIntegerValue() else null

    private fun getDecimalThreshold(
        dataPointType: String,
        framework: DataTypeEnum,
        thresholds: SignificanceThresholds,
    ): BigDecimal =
        BigDecimal.valueOf(
            thresholds.individualDecimalThresholds[framework]?.get(dataPointType)
                ?: thresholds.decimalRelativeThreshold,
        )

    private fun getIntegerThreshold(
        dataPointType: String,
        framework: DataTypeEnum,
        thresholds: SignificanceThresholds,
    ): BigInteger =
        BigInteger.valueOf(
            thresholds.individualIntegerThresholds[framework]?.get(dataPointType)
                ?: thresholds.integerAbsoluteThreshold,
        )
}
