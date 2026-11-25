package org.dataland.datalandbackend.services.datapoints

import org.dataland.datalandbackend.model.datapoints.UploadedDataPoint
import org.dataland.datalandbackendutils.model.DataPointType
import org.dataland.specificationservice.openApiClient.model.DataPointTypeSpecification
import org.dataland.specificationservice.openApiClient.model.FrameworkSpecification
import java.math.BigDecimal
import org.dataland.datalandbackend.interfaces.datapoints.ExtendedDataPoint as ExtendedDataPointInterface

private const val SUM_NUMERATOR_INPUT_COUNT = 3

/**
 * Ensures that [inputs] preserve a fixed order and returns them as a [List].
 *
 * @param inputs the source data points to check
 * @param operationName the human-readable operation name used in the validation error message
 * @return [inputs] as a [List]
 * @throws IllegalArgumentException if [inputs] is not already a [List]
 */
internal fun requireOrderedInputs(
    inputs: Collection<UploadedDataPoint>,
    operationName: String,
): List<UploadedDataPoint> {
    require(inputs is List<UploadedDataPoint>) {
        "Inputs for $operationName must be provided as an ordered list, but were a ${inputs::class.simpleName}."
    }
    return inputs
}

/**
 * Closed set of strategies for deriving a data point from a collection of other data points.
 * Variants are dispatched by their [id] via [byId].
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
                inputs = requireOrderedInputs(inputs, "division"),
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
                inputs = requireOrderedInputs(inputs, "division by percent"),
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
        ): UploadedDataPoint = convertSubtraction(requireOrderedInputs(inputs, "subtraction"), targetType, specs, sourceFrameworksByType)
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
                inputs = requireOrderedInputs(inputs, "multiplication by percent"),
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
                inputs = requireOrderedInputs(inputs, "multiplication by complement percent"),
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
        ): UploadedDataPoint =
            convertMultiplicationByPercentMinusCurrency(
                requireOrderedInputs(inputs, "multiplication by percent minus currency"),
                targetType,
                specs,
                sourceFrameworksByType,
            )
    },

    DIVISION_PER_MILLION("DivisionPerMillion") {
        override fun getCalculationFormula(inputs: Collection<UploadedDataPoint>): String {
            val references = getNumberedSourceReferences(inputs)
            return "${references[0]} / ${references[1]} * 10^6"
        }

        override fun convert(
            inputs: Collection<UploadedDataPoint>,
            targetType: DataPointType,
            specs: Map<DataPointType, DataPointTypeSpecification>,
            sourceFrameworksByType: Map<DataPointType, List<FrameworkSpecification>>,
        ): UploadedDataPoint =
            convertNumeric(inputs, targetType, specs, sourceFrameworksByType, "division per million", 2) {
                require(it.size == 2) { "Exactly two data points must be provided for division per million." }
                divideScaled(it[0], it[1], ONE_MILLION, "division per million")
            }
    },

    SUM_DIVISION_PER_MILLION("SumDivisionPerMillion") {
        override fun getCalculationFormula(inputs: Collection<UploadedDataPoint>): String {
            val references = getNumberedSourceReferences(inputs)
            return "(${references.dropLast(1).joinToString(" + ")}) / ${references.last()} * 10^6"
        }

        override fun convert(
            inputs: Collection<UploadedDataPoint>,
            targetType: DataPointType,
            specs: Map<DataPointType, DataPointTypeSpecification>,
            sourceFrameworksByType: Map<DataPointType, List<FrameworkSpecification>>,
        ): UploadedDataPoint =
            convertNumeric(inputs, targetType, specs, sourceFrameworksByType, "sum division per million") {
                divideSumsPerMillion(it.dropLast(1), listOf(it.last()), "sum division per million")
            }
    },

    DIVISION_BY_SUM_PER_MILLION("DivisionBySumPerMillion") {
        override fun getCalculationFormula(inputs: Collection<UploadedDataPoint>): String {
            val references = getNumberedSourceReferences(inputs)
            return "${references.first()} / (${references.drop(1).joinToString(" + ")}) * 10^6"
        }

        override fun convert(
            inputs: Collection<UploadedDataPoint>,
            targetType: DataPointType,
            specs: Map<DataPointType, DataPointTypeSpecification>,
            sourceFrameworksByType: Map<DataPointType, List<FrameworkSpecification>>,
        ): UploadedDataPoint =
            convertNumeric(inputs, targetType, specs, sourceFrameworksByType, "division by sum per million") {
                divideSumsPerMillion(listOf(it.first()), it.drop(1), "division by sum per million")
            }
    },

    /**
     * Fixed split: the first [SUM_NUMERATOR_INPUT_COUNT] inputs form the numerator sum, all remaining inputs
     * (at least two) form the denominator sum.
     */
    SUM_DIVISION_BY_SUM_PER_MILLION("SumDivisionBySumPerMillion") {
        override fun getCalculationFormula(inputs: Collection<UploadedDataPoint>): String {
            val references = getNumberedSourceReferences(inputs)
            return "(${references.take(SUM_NUMERATOR_INPUT_COUNT).joinToString(" + ")}) / " +
                "(${references.drop(SUM_NUMERATOR_INPUT_COUNT).joinToString(" + ")}) * 10^6"
        }

        override fun convert(
            inputs: Collection<UploadedDataPoint>,
            targetType: DataPointType,
            specs: Map<DataPointType, DataPointTypeSpecification>,
            sourceFrameworksByType: Map<DataPointType, List<FrameworkSpecification>>,
        ): UploadedDataPoint =
            convertNumeric(
                inputs, targetType, specs, sourceFrameworksByType, "sum division by sum per million",
                SUM_NUMERATOR_INPUT_COUNT + 2,
            ) {
                divideSumsPerMillion(
                    it.take(SUM_NUMERATOR_INPUT_COUNT),
                    it.drop(SUM_NUMERATOR_INPUT_COUNT),
                    "sum division by sum per million",
                )
            }
    },

    SHARE_OF_SUM_IN_PERCENT("ShareOfSumInPercent") {
        override fun getCalculationFormula(inputs: Collection<UploadedDataPoint>): String {
            val references = getNumberedSourceReferences(inputs)
            return "${references[0]} / (${references[0]} + ${references[1]}) * 100"
        }

        override fun convert(
            inputs: Collection<UploadedDataPoint>,
            targetType: DataPointType,
            specs: Map<DataPointType, DataPointTypeSpecification>,
            sourceFrameworksByType: Map<DataPointType, List<FrameworkSpecification>>,
        ): UploadedDataPoint =
            convertNumeric(inputs, targetType, specs, sourceFrameworksByType, "share of sum in percent", 2) {
                require(it.size == 2) { "Exactly two data points must be provided for share of sum in percent." }
                shareOfSumInPercent(it[0], it[1], "share of sum in percent")
            }
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
    },

    EU_TAXONOMY_SHARE("EuTaxonomyShare") {
        override fun createComment(
            inputs: Collection<UploadedDataPoint>,
            specs: Map<DataPointType, DataPointTypeSpecification>,
            dataPoints: Collection<ExtendedDataPointInterface<*>>,
            sourceFrameworksByType: Map<DataPointType, List<FrameworkSpecification>>,
        ): String = createCommentEuTaxonomyShare(inputs, specs, dataPoints, sourceFrameworksByType)

        override fun convert(
            inputs: Collection<UploadedDataPoint>,
            targetType: DataPointType,
            specs: Map<DataPointType, DataPointTypeSpecification>,
            sourceFrameworksByType: Map<DataPointType, List<FrameworkSpecification>>,
        ): UploadedDataPoint = convertEuTaxonomyShare(inputs, targetType, specs, sourceFrameworksByType)
    }, ;

    /**
     * Applies this conversion strategy to merge [inputs] into a single derived data point of [targetType].
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
}

/**
 * Resolves [method] to a [DataPointConversion] and applies it to [inputs] producing a data point of [targetType].
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
