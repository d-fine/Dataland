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
 * - Decimal: a relative change of more than the defined [DECIMAL_RELATIVE_THRESHOLD] is significant.
 * - Integer: an absolute change of more than the defined [INTEGER_ABSOLUTE_THRESHOLD] is significant.
 *
 * Individual per-data-point threshold overrides per framework can be registered via
 * [individualDecimalThresholds] and [individualIntegerThresholds] if needed.
 */
@Service
class SignificanceCheckService {
    enum class ValueType { BOOLEAN, DECIMAL, INTEGER, UNSUPPORTED }

    companion object {
        const val DECIMAL_RELATIVE_THRESHOLD = 0.5

        val INTEGER_ABSOLUTE_THRESHOLD: BigInteger = BigInteger.valueOf(5)

        private val DECIMAL_BASE_TYPE_IDS = setOf("extendedDecimal")
        private val INTEGER_BASE_TYPE_IDS = setOf("extendedInteger")
        private val BOOLEAN_BASE_TYPE_IDS = setOf("extendedEnumYesNo")

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
     * @param newValue The value node of the data point in the dataset under review.
     * @param liveValue The value node of the same data point in the currently live dataset.
     * @param valueType The value type category of the data point.
     * @param dataPointType The data point type identifier (used for per-data-point threshold lookups).
     * @param framework The framework of the dataset (used for per-data-point threshold lookups).
     * @return true if the change is significant and auto pre-approval should be suppressed; false otherwise.
     */
    fun hasSignificantChange(
        newValue: JsonNode?,
        liveValue: JsonNode?,
        valueType: ValueType,
        dataPointType: String,
        framework: DataTypeEnum,
    ): Boolean {
        val newVal = newValue?.takeUnless { it.isNull }
        val live = liveValue?.takeUnless { it.isNull }
        if (newVal == null || live == null) return false

        return when (valueType) {
            ValueType.BOOLEAN -> newVal.asText() != live.asText()
            ValueType.DECIMAL -> isDecimalChangeSignificant(newVal, live, dataPointType, framework)
            ValueType.INTEGER -> isIntegerChangeSignificant(newVal, live, dataPointType, framework)
            ValueType.UNSUPPORTED -> false
        }
    }

    private fun isDecimalChangeSignificant(
        newValue: JsonNode,
        liveValue: JsonNode,
        dataPointType: String,
        framework: DataTypeEnum,
    ): Boolean {
        val original = newValue.decimalValueOrNull()
        val live = liveValue.decimalValueOrNull()
        if (original == null || live == null) return false
        val threshold = getDecimalThreshold(dataPointType, framework)

        if (live.compareTo(BigDecimal.ZERO) == 0) {
            return original.compareTo(BigDecimal.ZERO) != 0
        }

        val relativeChange =
            original
                .subtract(live)
                .abs()
                .divide(live.abs(), DECIMAL_DIVISION_SCALE, RoundingMode.HALF_UP)

        return relativeChange > threshold
    }

    private fun isIntegerChangeSignificant(
        newValue: JsonNode,
        liveValue: JsonNode,
        dataPointType: String,
        framework: DataTypeEnum,
    ): Boolean {
        val original = newValue.bigIntegerValueOrNull()
        val live = liveValue.bigIntegerValueOrNull()
        if (original == null || live == null) return false
        val threshold = getIntegerThreshold(dataPointType, framework)

        return original.subtract(live).abs() > threshold
    }

    private fun JsonNode.decimalValueOrNull(): BigDecimal? = if (isNumber) decimalValue() else null

    private fun JsonNode.bigIntegerValueOrNull(): BigInteger? = if (isIntegralNumber) bigIntegerValue() else null

    private fun getDecimalThreshold(
        dataPointType: String,
        framework: DataTypeEnum,
    ): BigDecimal =
        BigDecimal.valueOf(
            individualDecimalThresholds[framework]?.get(dataPointType) ?: DECIMAL_RELATIVE_THRESHOLD,
        )

    private fun getIntegerThreshold(
        dataPointType: String,
        framework: DataTypeEnum,
    ): BigInteger = individualIntegerThresholds[framework]?.get(dataPointType) ?: INTEGER_ABSOLUTE_THRESHOLD
}
