package org.dataland.datalandbackend.services.datapoints

import com.fasterxml.jackson.module.kotlin.readValue
import org.dataland.datalandbackend.frameworks.eutaxonomynonfinancials.custom.EuTaxonomyActivity
import org.dataland.datalandbackend.frameworks.eutaxonomynonfinancials.custom.EuTaxonomyAlignedActivity
import org.dataland.datalandbackend.model.datapoints.ExtendedDataPoint
import org.dataland.datalandbackend.model.datapoints.UploadedDataPoint
import org.dataland.datalandbackend.model.datapoints.extended.ExtendedCurrencyDataPoint
import org.dataland.datalandbackendutils.model.DataPointType
import org.dataland.datalandbackendutils.utils.JsonUtils.defaultObjectMapper
import org.dataland.specificationservice.openApiClient.model.DataPointTypeSpecification
import org.dataland.specificationservice.openApiClient.model.FrameworkSpecification
import java.math.BigDecimal
import org.dataland.datalandbackend.interfaces.datapoints.ExtendedDataPoint as ExtendedDataPointInterface

private val ONE_HUNDRED = BigDecimal("100")
private const val REQUIRED_INPUT_COUNT = 3

/**
 * Closed set of strategies for deriving a data point from a collection of other data points.
 *
 * Variants are dispatched by their [id] via [byId].
 *
 * @param id the textual identifier of the conversion strategy
 */
@Suppress("TooManyFunctions")
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

    SUBTRACTION("Subtraction") {
        override fun getCalculationFormula(inputs: Collection<UploadedDataPoint>): String =
            getNumberedSourceReferences(inputs).joinToString(" - ")

        override fun convert(
            inputs: Collection<UploadedDataPoint>,
            targetType: DataPointType,
            specs: Map<DataPointType, DataPointTypeSpecification>,
            sourceFrameworksByType: Map<DataPointType, List<FrameworkSpecification>>,
        ): UploadedDataPoint = convertSubtraction(inputs, targetType, specs, sourceFrameworksByType)
    },

    COMPLEMENT_TO_PERCENT("ComplementToPercent") {
        override fun getCalculationFormula(inputs: Collection<UploadedDataPoint>): String =
            "100 - ${getNumberedSourceReferences(inputs).single()}"

        override fun convert(
            inputs: Collection<UploadedDataPoint>,
            targetType: DataPointType,
            specs: Map<DataPointType, DataPointTypeSpecification>,
            sourceFrameworksByType: Map<DataPointType, List<FrameworkSpecification>>,
        ): UploadedDataPoint = convertComplementToPercent(inputs, targetType, specs, sourceFrameworksByType)
    },

    MULTIPLICATION_BY_PERCENT("MultiplicationByPercent") {
        override fun getCalculationFormula(inputs: Collection<UploadedDataPoint>): String {
            val references = getNumberedSourceReferences(inputs)
            return "${references[0]} * ${references[1]} / 100"
        }

        override fun convert(
            inputs: Collection<UploadedDataPoint>,
            targetType: DataPointType,
            specs: Map<DataPointType, DataPointTypeSpecification>,
            sourceFrameworksByType: Map<DataPointType, List<FrameworkSpecification>>,
        ): UploadedDataPoint =
            convertMultiplicationByPercent(
                inputs = inputs,
                targetType = targetType,
                specs = specs,
                sourceFrameworksByType = sourceFrameworksByType,
                useComplement = false,
                operationName = "multiplication by percent",
            )
    },

    MULTIPLICATION_BY_COMPLEMENT_PERCENT("MultiplicationByComplementPercent") {
        override fun getCalculationFormula(inputs: Collection<UploadedDataPoint>): String {
            val references = getNumberedSourceReferences(inputs)
            return "${references[0]} * (100 - ${references[1]}) / 100"
        }

        override fun convert(
            inputs: Collection<UploadedDataPoint>,
            targetType: DataPointType,
            specs: Map<DataPointType, DataPointTypeSpecification>,
            sourceFrameworksByType: Map<DataPointType, List<FrameworkSpecification>>,
        ): UploadedDataPoint =
            convertMultiplicationByPercent(
                inputs = inputs,
                targetType = targetType,
                specs = specs,
                sourceFrameworksByType = sourceFrameworksByType,
                useComplement = true,
                operationName = "multiplication by complement percent",
            )
    },

    MULTIPLICATION_BY_PERCENT_MINUS_CURRENCY("MultiplicationByPercentMinusCurrency") {
        override fun getCalculationFormula(inputs: Collection<UploadedDataPoint>): String {
            val references = getNumberedSourceReferences(inputs)
            return "(${references[0]} * ${references[1]} / 100) - ${references[2]}"
        }

        override fun convert(
            inputs: Collection<UploadedDataPoint>,
            targetType: DataPointType,
            specs: Map<DataPointType, DataPointTypeSpecification>,
            sourceFrameworksByType: Map<DataPointType, List<FrameworkSpecification>>,
        ): UploadedDataPoint = convertMultiplicationByPercentMinusCurrency(inputs, targetType, specs, sourceFrameworksByType)
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
    },

    EU_TAXONOMY_ACTVITY_MERGE("EuTaxonomyActivityMerge") {
        override fun createComment(
            inputs: Collection<UploadedDataPoint>,
            specs: Map<DataPointType, DataPointTypeSpecification>,
            dataPoints: Collection<ExtendedDataPointInterface<*>>,
            sourceFrameworksByType: Map<DataPointType, List<FrameworkSpecification>>,
        ): String =
            "This list of activities was mapped from the EU Taxonomy (2020/852) framework by merging the activities " +
                "in the activity lists " + getNumberedSourceReferences(inputs).joinToString(", ") + "\n\n***\n\n" +
                getSourcesSection(inputs, specs, dataPoints, sourceFrameworksByType)

        override fun convert(
            inputs: Collection<UploadedDataPoint>,
            targetType: DataPointType,
            specs: Map<DataPointType, DataPointTypeSpecification>,
            sourceFrameworksByType: Map<DataPointType, List<FrameworkSpecification>>,
        ): UploadedDataPoint = convertEuTaxonomyActivityMerge(inputs, targetType, specs, sourceFrameworksByType)
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
        val calculatedDataPoint =
            if (isCurrencyDataPoint(targetType, specs)) {
                val operands = extractSumOperands<ExtendedCurrencyDataPoint>(inputs)
                ExtendedCurrencyDataPoint(
                    value = operands.values.sumOf { it },
                    currency = getCommonCurrency(operands.dataPoints),
                    quality = mergeQuality(operands.dataPoints.map { it.quality }),
                    comment = createComment(inputs, specs, operands.dataPoints, sourceFrameworksByType),
                    dataSource = mergeDataSources(operands.dataPoints.mapNotNull(::getDataSource)),
                )
            } else {
                val operands = extractSumOperands<ExtendedDataPoint<BigDecimal>>(inputs)
                ExtendedDataPoint(
                    value = operands.values.sumOf { it },
                    quality = mergeQuality(operands.dataPoints.map { it.quality }),
                    comment = createComment(inputs, specs, operands.dataPoints, sourceFrameworksByType),
                    dataSource = mergeDataSources(operands.dataPoints.mapNotNull(::getDataSource)),
                )
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
        val calculatedDataPoint =
            if (isCurrencyDataPoint(targetType, specs)) {
                val operands =
                    extractDivisionOperands<ExtendedCurrencyDataPoint, ExtendedDataPoint<BigDecimal>>(
                        inputs,
                        operationName,
                    )
                val sources = listOf(operands.numerator, operands.denominator)
                ExtendedCurrencyDataPoint(
                    value = operands.calculateValue(multiplier),
                    currency = getCurrency(operands.numerator),
                    quality = mergeQuality(sources.map { it.quality }),
                    comment = createComment(inputs, specs, sources, sourceFrameworksByType),
                    dataSource = mergeDataSources(sources.mapNotNull(::getDataSource)),
                )
            } else {
                val operands =
                    extractDivisionOperands<ExtendedDataPoint<BigDecimal>, ExtendedDataPoint<BigDecimal>>(
                        inputs,
                        operationName,
                    )
                val sources = listOf(operands.numerator, operands.denominator)
                ExtendedDataPoint(
                    value = operands.calculateValue(multiplier),
                    quality = mergeQuality(sources.map { it.quality }),
                    comment = createComment(inputs, specs, sources, sourceFrameworksByType),
                    dataSource = mergeDataSources(sources.mapNotNull(::getDataSource)),
                )
            }

        return createUploadedDataPoint(
            inputs = inputs,
            targetType = targetType,
            calculatedDataPoint = calculatedDataPoint,
        )
    }

    /**
     * Subtracts the second of [inputs] from the first into a data point of [targetType].
     *
     * @param inputs the minuend and subtrahend source data points
     * @param targetType the data point type assigned to the resulting data point
     * @param specs the data point type specifications used to deserialize and label inputs
     * @param sourceFrameworksByType framework specifications associated with each source data point type
     * @return the derived data point produced by the subtraction
     */
    protected fun convertSubtraction(
        inputs: Collection<UploadedDataPoint>,
        targetType: DataPointType,
        specs: Map<DataPointType, DataPointTypeSpecification>,
        sourceFrameworksByType: Map<DataPointType, List<FrameworkSpecification>>,
    ): UploadedDataPoint {
        val calculatedDataPoint =
            if (isCurrencyDataPoint(targetType, specs)) {
                val operands = extractSubtractionOperands<ExtendedCurrencyDataPoint, ExtendedCurrencyDataPoint>(inputs)
                val sources = listOf(operands.minuend, operands.subtrahend)
                ExtendedCurrencyDataPoint(
                    value = operands.calculateValue(),
                    currency = getCommonCurrency(sources),
                    quality = mergeQuality(sources.map { it.quality }),
                    comment = createComment(inputs, specs, sources, sourceFrameworksByType),
                    dataSource = mergeDataSources(sources.mapNotNull(::getDataSource)),
                )
            } else {
                val operands =
                    extractSubtractionOperands<ExtendedDataPoint<BigDecimal>, ExtendedDataPoint<BigDecimal>>(inputs)
                val sources = listOf(operands.minuend, operands.subtrahend)
                ExtendedDataPoint(
                    value = operands.calculateValue(),
                    quality = mergeQuality(sources.map { it.quality }),
                    comment = createComment(inputs, specs, sources, sourceFrameworksByType),
                    dataSource = mergeDataSources(sources.mapNotNull(::getDataSource)),
                )
            }

        return createUploadedDataPoint(
            inputs = inputs,
            targetType = targetType,
            calculatedDataPoint = calculatedDataPoint,
        )
    }

    /**
     * Subtracts the single element of [inputs] from 100 into a data point of [targetType].
     *
     * @param inputs the single source data point holding the percentage to complement
     * @param targetType the data point type assigned to the resulting data point
     * @param specs the data point type specifications used to deserialize and label inputs
     * @param sourceFrameworksByType framework specifications associated with each source data point type
     * @return the derived data point produced by the complement calculation
     */
    protected fun convertComplementToPercent(
        inputs: Collection<UploadedDataPoint>,
        targetType: DataPointType,
        specs: Map<DataPointType, DataPointTypeSpecification>,
        sourceFrameworksByType: Map<DataPointType, List<FrameworkSpecification>>,
    ): UploadedDataPoint {
        val dataPoint = extractIdentityOperand<ExtendedDataPoint<BigDecimal>>(inputs)
        val value =
            requireNotNull(dataPoint.value) {
                "Data point for complement to percent must not have a null value field."
            }
        val calculatedDataPoint =
            ExtendedDataPoint(
                value = ONE_HUNDRED.subtract(value),
                quality = dataPoint.quality,
                comment = createComment(inputs, specs, listOf(dataPoint), sourceFrameworksByType),
                dataSource = dataPoint.dataSource,
            )

        return createUploadedDataPoint(
            inputs = inputs,
            targetType = targetType,
            calculatedDataPoint = calculatedDataPoint,
        )
    }

    /**
     * Multiplies the first of [inputs] by the second, treated as a percentage, into a data point of [targetType].
     *
     * @param inputs the value and percentage source data points
     * @param targetType the data point type assigned to the resulting data point
     * @param specs the data point type specifications used to deserialize and label inputs
     * @param sourceFrameworksByType framework specifications associated with each source data point type
     * @param useComplement true to multiply by the complement of the percentage (100 - percentage) instead of the
     * percentage itself
     * @param operationName the human-readable operation name used in validation error messages
     * @return the derived data point produced by the multiplication
     */
    protected fun convertMultiplicationByPercent(
        inputs: Collection<UploadedDataPoint>,
        targetType: DataPointType,
        specs: Map<DataPointType, DataPointTypeSpecification>,
        sourceFrameworksByType: Map<DataPointType, List<FrameworkSpecification>>,
        useComplement: Boolean,
        operationName: String,
    ): UploadedDataPoint {
        val calculatedDataPoint =
            if (isCurrencyDataPoint(targetType, specs)) {
                val operands = extractPercentageMultiplicationOperands<ExtendedCurrencyDataPoint>(inputs, operationName)
                val sources = listOf(operands.valueDataPoint, operands.percentDataPoint)
                ExtendedCurrencyDataPoint(
                    value = operands.calculateShare(useComplement),
                    currency = getCurrency(operands.valueDataPoint),
                    quality = mergeQuality(sources.map { it.quality }),
                    comment = createComment(inputs, specs, sources, sourceFrameworksByType),
                    dataSource = mergeDataSources(sources.mapNotNull(::getDataSource)),
                )
            } else {
                val operands =
                    extractPercentageMultiplicationOperands<ExtendedDataPoint<BigDecimal>>(inputs, operationName)
                val sources = listOf(operands.valueDataPoint, operands.percentDataPoint)
                ExtendedDataPoint(
                    value = operands.calculateShare(useComplement),
                    quality = mergeQuality(sources.map { it.quality }),
                    comment = createComment(inputs, specs, sources, sourceFrameworksByType),
                    dataSource = mergeDataSources(sources.mapNotNull(::getDataSource)),
                )
            }

        return createUploadedDataPoint(
            inputs = inputs,
            targetType = targetType,
            calculatedDataPoint = calculatedDataPoint,
        )
    }

    /**
     * Multiplies the first of [inputs] by the second, treated as a percentage, and subtracts the third from the
     * result into a data point of [targetType].
     *
     * @param inputs the value, percentage and amount-to-subtract source data points, in that order
     * @param targetType the data point type assigned to the resulting data point
     * @param specs the data point type specifications used to deserialize and label inputs
     * @param sourceFrameworksByType framework specifications associated with each source data point type
     * @return the derived data point produced by the calculation
     */
    protected fun convertMultiplicationByPercentMinusCurrency(
        inputs: Collection<UploadedDataPoint>,
        targetType: DataPointType,
        specs: Map<DataPointType, DataPointTypeSpecification>,
        sourceFrameworksByType: Map<DataPointType, List<FrameworkSpecification>>,
    ): UploadedDataPoint {
        val operationName = "multiplication by percent minus currency"
        require(inputs.size == REQUIRED_INPUT_COUNT) { "Exactly three data points must be provided for $operationName." }
        val percentageOperands =
            extractPercentageMultiplicationOperands<ExtendedCurrencyDataPoint>(inputs.take(2), operationName)
        val amountToSubtract = defaultObjectMapper.readValue<ExtendedCurrencyDataPoint>(inputs.elementAt(2).dataPoint)
        val amountToSubtractValue =
            requireNotNull(amountToSubtract.value) {
                "Data points for $operationName must not have null value fields."
            }
        val sources = listOf(percentageOperands.valueDataPoint, percentageOperands.percentDataPoint, amountToSubtract)
        val calculatedDataPoint =
            ExtendedCurrencyDataPoint(
                value = percentageOperands.calculateShare().subtract(amountToSubtractValue),
                currency = getCommonCurrency(listOf(percentageOperands.valueDataPoint, amountToSubtract)),
                quality = mergeQuality(sources.map { it.quality }),
                comment = createComment(inputs, specs, sources, sourceFrameworksByType),
                dataSource = mergeDataSources(sources.mapNotNull(::getDataSource)),
            )

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

    protected fun convertEuTaxonomyActivityMerge(
        inputs: Collection<UploadedDataPoint>,
        targetType: DataPointType,
        specs: Map<DataPointType, DataPointTypeSpecification>,
        sourceFrameworksByType: Map<DataPointType, List<FrameworkSpecification>>,
    ): UploadedDataPoint {
        val activityLists =
            extractEuTaxonomy2020ActivityLists<
                ExtendedDataPoint<Iterable<EuTaxonomyActivity>?>?,
                ExtendedDataPoint<Iterable<EuTaxonomyAlignedActivity>?>?,
            >(inputs, specs)
        val sources = listOf(activityLists.alignedActivities, activityLists.nonAlignedActivities)
        val (mergedActivities, activitiesWithConflictingSubstantialContributions, activitiesWithoutAlignedShares) =
            activityLists.mergeLists()
        val baseComment =
            createComment(
                inputs,
                specs,
                listOfNotNull(activityLists.alignedActivities, activityLists.nonAlignedActivities),
                sourceFrameworksByType,
            )
        val comment =
            extendCreateCommentEuTaxonomyActivitiesMerge(
                baseComment,
                activitiesWithConflictingSubstantialContributions,
                activitiesWithoutAlignedShares,
            )
        val calculatedDataPoint =
            ExtendedDataPoint(
                value = mergedActivities.takeIf { it.isNotEmpty() },
                quality = mergeQuality(sources.map { it?.quality }),
                comment = comment,
                dataSource = mergeDataSources(sources.mapNotNull { it?.let(::getDataSource) }),
            )
        return createUploadedDataPoint(
            inputs = inputs,
            targetType = targetType,
            calculatedDataPoint = calculatedDataPoint,
        )
    }
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
