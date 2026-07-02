package org.dataland.datalandbackend.services.datapoints

import com.fasterxml.jackson.module.kotlin.readValue
import org.dataland.datalandbackend.interfaces.datapoints.BaseDataPoint
import org.dataland.datalandbackend.model.datapoints.ExtendedDataPoint
import org.dataland.datalandbackend.model.datapoints.UploadedDataPoint
import org.dataland.datalandbackend.model.datapoints.extended.ExtendedCurrencyDataPoint
import org.dataland.datalandbackend.model.documents.ExtendedDocumentReference
import org.dataland.datalandbackend.model.enums.data.QualityOptions
import org.dataland.datalandbackendutils.model.DataPointType
import org.dataland.datalandbackendutils.utils.JsonUtils.defaultObjectMapper
import org.dataland.specificationservice.openApiClient.model.DataPointTypeSpecification
import org.dataland.specificationservice.openApiClient.model.FrameworkSpecification
import java.math.BigDecimal
import java.math.RoundingMode
import org.dataland.datalandbackend.interfaces.datapoints.ExtendedDataPoint as ExtendedDataPointInterface

private const val CALCULATION_SCALE = 10
private val CALCULATION_ROUNDING_MODE = RoundingMode.HALF_UP
private val ONE_HUNDRED = BigDecimal("100")
private const val EXTENDED_CURRENCY_BASE_TYPE = "extendedCurrency"

/**
 * Closed set of strategies for deriving a data point from a collection of other data points.
 *
 * Variants are dispatched by their [id] via [byId].
 *
 * @param id the textual identifier of the conversion strategy
 */
enum class DataPointConversion(
    val id: String,
) {
    SUM("Sum") {
        override fun getCalculationFormula(inputs: Collection<UploadedDataPoint>): String =
            getNumberedSourceReferences(inputs).joinToString(" + ")

        override fun convert(
            inputs: Collection<UploadedDataPoint>,
            targetType: DataPointType,
            specs: Map<DataPointType, DataPointTypeSpecification>,
            sourceFrameworksByType: Map<DataPointType, List<FrameworkSpecification>>,
        ): UploadedDataPoint = convertSum(inputs, targetType, specs, sourceFrameworksByType)
    },

    DIVISION("Division") {
        override fun getCalculationFormula(inputs: Collection<UploadedDataPoint>): String =
            getNumberedSourceReferences(inputs).joinToString(" / ")

        override fun convert(
            inputs: Collection<UploadedDataPoint>,
            targetType: DataPointType,
            specs: Map<DataPointType, DataPointTypeSpecification>,
            sourceFrameworksByType: Map<DataPointType, List<FrameworkSpecification>>,
        ): UploadedDataPoint =
            convertDivision(
                inputs = inputs,
                targetType = targetType,
                specs = specs,
                sourceFrameworksByType = sourceFrameworksByType,
                multiplier = BigDecimal.ONE,
                operationName = "division",
            )
    },

    DIVISION_BY_PERCENT("DivisionByPercent") {
        override fun getCalculationFormula(inputs: Collection<UploadedDataPoint>): String =
            "100 * ${getNumberedSourceReferences(inputs).joinToString(" / ")}"

        override fun convert(
            inputs: Collection<UploadedDataPoint>,
            targetType: DataPointType,
            specs: Map<DataPointType, DataPointTypeSpecification>,
            sourceFrameworksByType: Map<DataPointType, List<FrameworkSpecification>>,
        ): UploadedDataPoint =
            convertDivision(
                inputs = inputs,
                targetType = targetType,
                specs = specs,
                sourceFrameworksByType = sourceFrameworksByType,
                multiplier = ONE_HUNDRED,
                operationName = "division by percent",
            )
    },

    IDENTITY("Identity") {
        override fun createComment(
            inputs: Collection<UploadedDataPoint>,
            specs: Map<DataPointType, DataPointTypeSpecification>,
            dataPoints: Collection<ExtendedDataPointInterface<*>>,
            sourceFrameworksByType: Map<DataPointType, List<FrameworkSpecification>>,
        ): String =
            "This data point was mapped from the following source: " +
                "${getNumberedSourceReferences(inputs).single()}\n\n***\n\n" +
                getSourcesSection(inputs, specs, dataPoints, sourceFrameworksByType)

        override fun convert(
            inputs: Collection<UploadedDataPoint>,
            targetType: DataPointType,
            specs: Map<DataPointType, DataPointTypeSpecification>,
            sourceFrameworksByType: Map<DataPointType, List<FrameworkSpecification>>,
        ): UploadedDataPoint = convertIdentity(inputs, targetType, specs, sourceFrameworksByType)
    }, ;

    /**
     * Applies this conversion strategy to merge [inputs] into a single derived data point of [targetType].
     *
     * @param inputs the source data points to be combined
     * @param targetType the data point type assigned to the resulting data point
     * @param specs the data point type specifications used to deserialize and label inputs
     * @param sourceFrameworksByType framework specifications associated with each source data point type
     * @return the derived data point produced by this strategy
     */
    abstract fun convert(
        inputs: Collection<UploadedDataPoint>,
        targetType: DataPointType,
        specs: Map<DataPointType, DataPointTypeSpecification>,
        sourceFrameworksByType: Map<DataPointType, List<FrameworkSpecification>>,
    ): UploadedDataPoint

    /**
     * Creates a comment for the resulting data point describing this conversion's formula.
     *
     * @param inputs the uploaded data points used as calculation inputs
     * @param specs the data point type specifications used to resolve input display names
     * @param dataPoints the deserialized source data points used for the conversion
     * @param sourceFrameworksByType framework specifications associated with each source data point type
     * @return a generated comment describing the calculation
     */
    open fun createComment(
        inputs: Collection<UploadedDataPoint>,
        specs: Map<DataPointType, DataPointTypeSpecification>,
        dataPoints: Collection<ExtendedDataPointInterface<*>>,
        sourceFrameworksByType: Map<DataPointType, List<FrameworkSpecification>>,
    ): String =
        getCalculationComment(
            formula = getCalculationFormula(inputs),
            inputs = inputs,
            specs = specs,
            dataPoints = dataPoints,
            sourceFrameworksByType = sourceFrameworksByType,
        )

    /**
     * Returns the formula fragment used in the generated calculation comment.
     *
     * @param inputs the uploaded data points used as calculation inputs
     * @return a formula using numbered source references such as `[1]`
     */
    protected open fun getCalculationFormula(inputs: Collection<UploadedDataPoint>): String =
        throw UnsupportedOperationException("Conversion $id does not use a calculation formula.")

    companion object {
        /**
         * Resolves the [DataPointConversion] whose [id] matches the given string.
         *
         * @param id the textual identifier of the conversion strategy
         * @return the matching [DataPointConversion]
         * @throws IllegalArgumentException if no variant has the given [id]
         */
        fun byId(id: String): DataPointConversion =
            entries.firstOrNull { it.id == id }
                ?: throw IllegalArgumentException("Unsupported method: $id")
    }

    /**
     * Sums the values of [inputs] into a single derived data point of [targetType].
     *
     * @param inputs the source data points to be summed
     * @param targetType the data point type assigned to the resulting data point
     * @param specs the data point type specifications used to deserialize and label inputs
     * @param sourceFrameworksByType framework specifications associated with each source data point type
     * @return the derived data point produced by the summation
     */
    protected fun convertSum(
        inputs: Collection<UploadedDataPoint>,
        targetType: DataPointType,
        specs: Map<DataPointType, DataPointTypeSpecification>,
        sourceFrameworksByType: Map<DataPointType, List<FrameworkSpecification>>,
    ): UploadedDataPoint {
        val operands = extractSumOperands(inputs)
        val sum = operands.values.sumOf { it }
        val quality = mergeQuality(operands.dataPoints.map { it.quality })
        val comment = createComment(inputs, specs, operands.dataPoints, sourceFrameworksByType)
        val dataSource = mergeDataSources(operands.dataPoints.mapNotNull { it.dataSource })

        val calculatedDataPoint =
            if (isCurrencyDataPoint(targetType, specs)) {
                ExtendedCurrencyDataPoint(
                    value = sum,
                    currency = getCommonCurrency(operands.dataPoints),
                    quality = quality,
                    comment = comment,
                    dataSource = dataSource,
                )
            } else {
                ExtendedDataPoint(value = sum, quality = quality, comment = comment, dataSource = dataSource)
            }

        return createUploadedDataPoint(
            inputs = inputs,
            targetType = targetType,
            calculatedDataPoint = calculatedDataPoint,
        )
    }

    /**
     * Divides the first of [inputs] by the second, optionally scaling by [multiplier], into a data point of [targetType].
     *
     * @param inputs the numerator and denominator source data points
     * @param targetType the data point type assigned to the resulting data point
     * @param specs the data point type specifications used to deserialize and label inputs
     * @param sourceFrameworksByType framework specifications associated with each source data point type
     * @param multiplier a factor applied to the numerator before the division
     * @param operationName the human-readable operation name used in validation error messages
     * @return the derived data point produced by the division
     */
    protected fun convertDivision(
        inputs: Collection<UploadedDataPoint>,
        targetType: DataPointType,
        specs: Map<DataPointType, DataPointTypeSpecification>,
        sourceFrameworksByType: Map<DataPointType, List<FrameworkSpecification>>,
        multiplier: BigDecimal,
        operationName: String,
    ): UploadedDataPoint {
        val operands = extractDivisionOperands(inputs, operationName)
        val value =
            operands.numeratorValue.multiply(multiplier).divide(
                operands.denominatorValue,
                CALCULATION_SCALE,
                CALCULATION_ROUNDING_MODE,
            )
        val sources = listOf(operands.numerator, operands.denominator)
        val quality = mergeQuality(sources.map { it.quality })
        val comment = createComment(inputs, specs, sources, sourceFrameworksByType)
        val dataSource = mergeDataSources(sources.mapNotNull { it.dataSource })

        val calculatedDataPoint =
            if (isCurrencyDataPoint(targetType, specs)) {
                ExtendedCurrencyDataPoint(
                    value = value,
                    currency = getCurrency(operands.numerator),
                    quality = quality,
                    comment = comment,
                    dataSource = dataSource,
                )
            } else {
                ExtendedDataPoint(value = value, quality = quality, comment = comment, dataSource = dataSource)
            }

        return createUploadedDataPoint(
            inputs = inputs,
            targetType = targetType,
            calculatedDataPoint = calculatedDataPoint,
        )
    }

    /**
     * Maps the single element of [inputs] into a derived data point of [targetType] without altering its value.
     *
     * @param inputs the single source data point to be mapped
     * @param targetType the data point type assigned to the resulting data point
     * @param specs the data point type specifications used to deserialize and label inputs
     * @param sourceFrameworksByType framework specifications associated with each source data point type
     * @return the derived data point produced by the identity mapping
     */
    protected fun convertIdentity(
        inputs: Collection<UploadedDataPoint>,
        targetType: DataPointType,
        specs: Map<DataPointType, DataPointTypeSpecification>,
        sourceFrameworksByType: Map<DataPointType, List<FrameworkSpecification>>,
    ): UploadedDataPoint {
        val calculatedDataPoint =
            if (isCurrencyDataPoint(targetType, specs)) {
                val dataPoint = extractIdentityOperand<ExtendedCurrencyDataPoint>(inputs)
                ExtendedCurrencyDataPoint(
                    value = dataPoint.value,
                    currency = getCurrency(dataPoint),
                    quality = dataPoint.quality,
                    comment = createComment(inputs, specs, listOf(dataPoint), sourceFrameworksByType),
                    dataSource = dataPoint.dataSource,
                )
            } else {
                val dataPoint = extractIdentityOperand<ExtendedDataPoint<Any?>>(inputs)
                ExtendedDataPoint(
                    value = dataPoint.value,
                    quality = dataPoint.quality,
                    comment = createComment(inputs, specs, listOf(dataPoint), sourceFrameworksByType),
                    dataSource = dataPoint.dataSource,
                )
            }

        return createUploadedDataPoint(
            inputs = inputs,
            targetType = targetType,
            calculatedDataPoint = calculatedDataPoint,
        )
    }
}

/**
 * Checks whether [dataPointType] has the extended currency base type in [specs].
 *
 * @param dataPointType the data point type to inspect
 * @param specs the data point type specifications keyed by type
 * @return true if the type is specified as an extended currency data point
 */
private fun isCurrencyDataPoint(
    dataPointType: DataPointType,
    specs: Map<DataPointType, DataPointTypeSpecification>,
): Boolean = specs[dataPointType]?.dataPointBaseType?.id == EXTENDED_CURRENCY_BASE_TYPE

/**
 * Returns the currency set on [dataPoint].
 *
 * @param dataPoint the currency data point to inspect
 * @return the non-null currency of the data point
 * @throws IllegalArgumentException if [dataPoint] has no currency
 */
private fun getCurrency(dataPoint: ExtendedCurrencyDataPoint): String {
    require(dataPoint.currency != null) { "Currency data points used in calculations must have a currency." }
    return dataPoint.currency
}

/**
 * Returns the shared currency used by all [dataPoints].
 *
 * @param dataPoints the currency data points to inspect
 * @return the single currency used by all given data points
 * @throws IllegalArgumentException if not all data points have the same non-null currency
 */
private fun getCommonCurrency(dataPoints: Collection<ExtendedCurrencyDataPoint>): String {
    val currencies = dataPoints.map { getCurrency(it) }.toSet()
    require(currencies.size == 1) { "Currency data points used in summation must all have the same currency." }
    return currencies.single()
}

/**
 * Wraps [calculatedDataPoint] in an [UploadedDataPoint] using metadata from the first source input.
 *
 * @param inputs the source inputs providing reporting period and company ID
 * @param targetType the data point type assigned to the calculated data point
 * @param calculatedDataPoint the calculated data point object to serialize
 * @return the uploaded data point representing the calculation result
 */
private fun <T : Any> createUploadedDataPoint(
    inputs: Collection<UploadedDataPoint>,
    targetType: DataPointType,
    calculatedDataPoint: BaseDataPoint<T>,
): UploadedDataPoint =
    UploadedDataPoint(
        dataPoint = defaultObjectMapper.writeValueAsString(calculatedDataPoint),
        reportingPeriod = inputs.first().reportingPeriod,
        companyId = inputs.first().companyId,
        dataPointType = targetType,
    )

/**
 * Merges the given [QualityOptions] into a single entry, returning the lowest quality among them.
 *
 * @param inputs the quality values to merge
 * @return the lowest quality value among the inputs
 */
internal fun mergeQuality(inputs: Collection<QualityOptions?>): QualityOptions? {
    val qualityOrder =
        listOf(
            QualityOptions.Audited, QualityOptions.Reported, QualityOptions.Estimated, QualityOptions.Incomplete,
            QualityOptions.NoDataFound, null,
        )
    return inputs.maxByOrNull { qualityOrder.indexOf(it) }
}

/**
 * Merges the given list of [ExtendedDocumentReference] into a single reference.
 *
 * @param inputs the document references to merge
 * @return the reference with the smallest file reference, or null if no references are provided
 */
internal fun mergeDataSources(inputs: Collection<ExtendedDocumentReference>): ExtendedDocumentReference? =
    inputs.minByOrNull { it.fileReference }

/**
 * Builds the complete comment for a calculated data point.
 *
 * @param formula the formula to display, using numbered source references such as `[1]`
 * @param inputs the uploaded source data points used for the calculation
 * @param specs the data point type specifications used to resolve source display names
 * @param dataPoints the deserialized source data points used to inspect quality and source comments
 * @param sourceFrameworksByType framework specifications associated with each source data point type
 * @return the generated calculation comment including formula and source details
 */
private fun getCalculationComment(
    formula: String,
    inputs: Collection<UploadedDataPoint>,
    specs: Map<DataPointType, DataPointTypeSpecification>,
    dataPoints: Collection<ExtendedDataPointInterface<*>>,
    sourceFrameworksByType: Map<DataPointType, List<FrameworkSpecification>>,
): String =
    "This data point was calculated using the following formula: $formula\n\n***\n\n" +
        getSourcesSection(inputs, specs, dataPoints, sourceFrameworksByType)

/**
 * Returns numbered source references for the given inputs.
 *
 * @param inputs the uploaded source data points to reference
 * @return references in input order, starting with `[1]`
 */
private fun getNumberedSourceReferences(inputs: Collection<UploadedDataPoint>): List<String> =
    inputs.mapIndexed { index, _ -> "[${index + 1}]" }

/**
 * Builds the source details section for a calculated or identity-mapped data point.
 *
 * Every source entry contains the source data point type name and framework display name. Source comments are included
 * only when the source data point quality is not [QualityOptions.Reported] or [QualityOptions.Audited].
 *
 * @param inputs the uploaded source data points used to resolve source type names
 * @param specs the data point type specifications keyed by source data point type
 * @param dataPoints the deserialized source data points used to inspect quality and comments
 * @param sourceFrameworksByType framework specifications associated with each source data point type
 * @return the formatted sources section
 */
private fun getSourcesSection(
    inputs: Collection<UploadedDataPoint>,
    specs: Map<DataPointType, DataPointTypeSpecification>,
    dataPoints: Collection<ExtendedDataPointInterface<*>>,
    sourceFrameworksByType: Map<DataPointType, List<FrameworkSpecification>>,
): String =
    inputs
        .zip(dataPoints)
        .mapIndexed { index, (input, dataPoint) ->
            val sourceName = specs.getValue(input.dataPointType).name
            val sourceFrameworkName = getSourceFrameworkLabel(sourceFrameworksByType[input.dataPointType].orEmpty())
            val commentLine =
                if (dataPoint.quality == QualityOptions.Reported || dataPoint.quality == QualityOptions.Audited) {
                    ""
                } else {
                    val sourceComment = dataPoint.comment?.takeIf { it.isNotBlank() } ?: "none"
                    "\n+ Comment: $sourceComment"
                }
            "[${index + 1}] $sourceName\n" +
                "+ Framework: $sourceFrameworkName" +
                commentLine
        }.joinToString(separator = "\n\n")

/**
 * Formats source framework specifications for display in generated calculation comments.
 *
 * @param sourceFrameworks framework specifications associated with a source data point type
 * @return a stable comma-separated list of framework names, or "Unknown" when no framework is available
 */
private fun getSourceFrameworkLabel(sourceFrameworks: List<FrameworkSpecification>): String =
    sourceFrameworks
        .map { it.name }
        .distinct()
        .sorted()
        .takeIf { it.isNotEmpty() }
        ?.joinToString(", ")
        ?: "Unknown"

private data class SumOperands(
    val dataPoints: List<ExtendedCurrencyDataPoint>,
    val values: List<BigDecimal>,
)

private fun extractSumOperands(inputs: Collection<UploadedDataPoint>): SumOperands {
    val dataPoints = inputs.map { defaultObjectMapper.readValue<ExtendedCurrencyDataPoint>(it.dataPoint) }
    require(dataPoints.size >= 2) { "At least two data points must be provided for summation." }
    val values =
        dataPoints.map {
            requireNotNull(it.value) { "Data points for summation must not have null value fields." }
        }
    return SumOperands(dataPoints = dataPoints, values = values)
}

private inline fun <reified T : ExtendedDataPointInterface<*>> extractIdentityOperand(inputs: Collection<UploadedDataPoint>): T {
    val dataPoints = inputs.map { defaultObjectMapper.readValue<T>(it.dataPoint) }
    require(dataPoints.size == 1) { "Exactly one data point must be provided for the identity rule." }
    return dataPoints.single()
}

private data class DivisionOperands(
    val numerator: ExtendedCurrencyDataPoint,
    val denominator: ExtendedCurrencyDataPoint,
    val numeratorValue: BigDecimal,
    val denominatorValue: BigDecimal,
)

private fun extractDivisionOperands(
    inputs: Collection<UploadedDataPoint>,
    operationName: String,
): DivisionOperands {
    val nullValueErrorMessage = "Data points for $operationName must not have null value fields."
    require(inputs.size == 2) { "Exactly two data points must be provided for $operationName." }
    val numerator = defaultObjectMapper.readValue<ExtendedCurrencyDataPoint>(inputs.elementAt(0).dataPoint)
    val denominator = defaultObjectMapper.readValue<ExtendedCurrencyDataPoint>(inputs.elementAt(1).dataPoint)
    val numeratorValue = requireNotNull(numerator.value) { nullValueErrorMessage }
    val denominatorValue = requireNotNull(denominator.value) { nullValueErrorMessage }
    require(denominatorValue.signum() != 0) { "The divisor in $operationName must not be zero." }
    return DivisionOperands(
        numerator = numerator,
        denominator = denominator,
        numeratorValue = numeratorValue,
        denominatorValue = denominatorValue,
    )
}

/**
 * Resolves [method] to a [DataPointConversion] and applies it to [inputs] producing a data point of [targetType].
 *
 * @param inputs the source data points to be converted
 * @param targetType the data point type assigned to the resulting data point
 * @param method the textual identifier of the conversion strategy
 * @param specs the data point type specifications used during conversion
 * @param sourceFrameworksByType framework specifications associated with each source data point type
 * @return the derived data point produced by the resolved strategy
 */
fun applyTransformation(
    inputs: Collection<UploadedDataPoint>,
    targetType: DataPointType,
    method: String,
    specs: Map<DataPointType, DataPointTypeSpecification>,
    sourceFrameworksByType: Map<DataPointType, List<FrameworkSpecification>>,
): UploadedDataPoint = DataPointConversion.byId(method).convert(inputs, targetType, specs, sourceFrameworksByType)
