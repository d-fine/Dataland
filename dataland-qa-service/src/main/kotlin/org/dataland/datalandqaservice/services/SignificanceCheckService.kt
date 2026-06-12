package org.dataland.datalandqaservice.org.dataland.datalandqaservice.services

import com.fasterxml.jackson.databind.JsonNode
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode

/**
 * Pure service for checking whether the change between two data point values is considered
 * significant according to hardcoded thresholds per value type.
 *
 * A significant change suppresses automatic pre-approval for that data point, requiring manual
 * review by a QA judge instead.
 *
 * Thresholds are hardcoded per value type and can only be changed via redeployment:
 * - Boolean: any change is significant.
 * - Decimal: a relative change of more than 50% is significant.
 * - Integer: an absolute change of more than 5 is significant.
 *
 * Individual per-data-point threshold overrides per framework can be registered via
 * [individualDecimalThresholds] and [individualIntegerThresholds] if needed.
 */
@Service
class SignificanceCheckService {
    /**
     * The value type category of a data point, derived from its data point base type specification.
     */
    enum class ValueType { BOOLEAN, DECIMAL, INTEGER, UNSUPPORTED }

    companion object {
        /** Relative change threshold for Decimal fields: a change of more than 50% is significant. */
        const val DECIMAL_RELATIVE_THRESHOLD = 0.5

        /** Absolute change threshold for Integer fields: a change of more than 5 is significant. */
        val INTEGER_ABSOLUTE_THRESHOLD: BigInteger = BigInteger.valueOf(5)

        private val DECIMAL_BASE_TYPE_IDS = setOf("extendedDecimal")
        private val INTEGER_BASE_TYPE_IDS = setOf("extendedInteger")
        private val BOOLEAN_BASE_TYPE_IDS = setOf("extendedEnumYesNo")

        /** Scale used for the intermediate division when computing relative decimal change. */
        private const val DECIMAL_DIVISION_SCALE = 10
    }

    /**
     * Per-data-point relative threshold overrides for Decimal fields, keyed by framework and
     * data point type. If absent, [DECIMAL_RELATIVE_THRESHOLD] is used.
     */
    private val individualDecimalThresholds: Map<DataTypeEnum, Map<String, Double>> = emptyMap()

    /**
     * Per-data-point absolute threshold overrides for Integer fields, keyed by framework and
     * data point type. If absent, [INTEGER_ABSOLUTE_THRESHOLD] is used.
     */
    private val individualIntegerThresholds: Map<DataTypeEnum, Map<String, BigInteger>> = emptyMap()

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
     * @param originalValue The value node of the data point in the dataset under review.
     * @param liveValue The value node of the same data point in the currently live dataset.
     * @param valueType The value type category of the data point.
     * @param dataPointType The data point type identifier (used for per-data-point threshold lookups).
     * @param framework The framework of the dataset (used for per-data-point threshold lookups).
     * @return true if the change is significant and auto pre-approval should be suppressed; false otherwise.
     */
    fun checkForSignificantChange(
        originalValue: JsonNode?,
        liveValue: JsonNode?,
        valueType: ValueType,
        dataPointType: String,
        framework: DataTypeEnum,
    ): Boolean {
        if (isNullOrJsonNull(originalValue) || isNullOrJsonNull(liveValue)) return false

        return when (valueType) {
            ValueType.BOOLEAN -> originalValue!!.asText() != liveValue!!.asText()
            ValueType.DECIMAL -> isDecimalChangeSignificant(originalValue!!, liveValue!!, dataPointType, framework)
            ValueType.INTEGER -> isIntegerChangeSignificant(originalValue!!, liveValue!!, dataPointType, framework)
            ValueType.UNSUPPORTED -> false
        }
    }

    private fun isNullOrJsonNull(value: JsonNode?): Boolean = value == null || value.isNull

    private fun isDecimalChangeSignificant(
        originalValue: JsonNode,
        liveValue: JsonNode,
        dataPointType: String,
        framework: DataTypeEnum,
    ): Boolean {
        if (!originalValue.isNumber || !liveValue.isNumber) return false
        val original = originalValue.decimalValue()
        val live = liveValue.decimalValue()
        val threshold =
            BigDecimal.valueOf(
                individualDecimalThresholds[framework]?.get(dataPointType) ?: DECIMAL_RELATIVE_THRESHOLD,
            )
        return if (live.compareTo(BigDecimal.ZERO) == 0) {
            original.compareTo(BigDecimal.ZERO) != 0
        } else {
            (original - live).abs().divide(live.abs(), DECIMAL_DIVISION_SCALE, RoundingMode.HALF_UP) > threshold
        }
    }

    private fun isIntegerChangeSignificant(
        originalValue: JsonNode,
        liveValue: JsonNode,
        dataPointType: String,
        framework: DataTypeEnum,
    ): Boolean {
        if (!originalValue.isIntegralNumber || !liveValue.isIntegralNumber) return false
        val original = originalValue.bigIntegerValue()
        val live = liveValue.bigIntegerValue()
        val threshold = individualIntegerThresholds[framework]?.get(dataPointType) ?: INTEGER_ABSOLUTE_THRESHOLD
        return (original - live).abs() > threshold
    }
}
