package org.dataland.datalandbackend.services.dataPoints

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.module.kotlin.readValue
import org.dataland.datalandbackend.services.datapoints.DataPointConversion
import org.dataland.datalandbackendutils.utils.JsonUtils.defaultObjectMapper
import org.dataland.specificationservice.openApiClient.model.CalculationRule
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.File

@JsonIgnoreProperties(ignoreUnknown = true)
private data class RawDataPointType(
    val calculationRules: List<CalculationRule>? = null,
)

class CoveredCalculationRulesTest {
    companion object {
        private const val DATA_POINT_TYPES_PATH =
            "../dataland-specification-service/src/main/resources/specifications/dataPointTypes"
    }

    private fun getDataPointTypeFiles(): List<File> =
        File(DATA_POINT_TYPES_PATH)
            .listFiles { f -> f.extension == "json" }
            .orEmpty()
            .toList()

    private fun getSpecifiedCalculationRules(): List<Pair<File, CalculationRule>> =
        getDataPointTypeFiles()
            .flatMap { file ->
                defaultObjectMapper
                    .readValue<RawDataPointType>(file)
                    .calculationRules
                    .orEmpty()
                    .map { file to it }
            }

    @Test
    fun `check that all calculation rules specified in the framework toolbox are also implemented`() {
        val specifiedCalculationRules =
            getSpecifiedCalculationRules()
                .map { it.second }
                .distinctBy { it.calculationMethod }

        val unimplementedRules =
            specifiedCalculationRules.filter { rule ->
                try {
                    DataPointConversion.byId(rule.calculationMethod)
                    false
                } catch (_: IllegalArgumentException) {
                    true
                }
            }

        assertTrue(
            unimplementedRules.isEmpty(),
            "The following calculation rules are specified in the framework toolbox but not implemented:" +
                " ${unimplementedRules.joinToString(", ")}",
        )
    }

    /**
     * The number of inputs each calculation method expects, expressed as an (inclusive) range. Methods not listed
     * here (e.g. those with input counts that legitimately depend on runtime data, or whose arity is not yet
     * pinned down for all conversion paths) are not checked by the arity test below.
     */
    private val expectedInputCountRanges: Map<String, IntRange> =
        mapOf(
            "Division" to 2..2,
            "DivisionByPercent" to 2..2,
            "Subtraction" to 2..2,
            "ComplementToPercent" to 1..1,
            "MultiplicationByPercent" to 2..2,
            "MultiplicationByComplementPercent" to 2..2,
            "MultiplicationByPercentMinusCurrency" to 3..3,
            "Identity" to 1..1,
            "EuTaxonomyActivityMerge" to 2..2,
        )

    @Test
    fun `check that all configured calculation rules have the expected number of inputs`() {
        val rulesWithUnexpectedArity =
            getSpecifiedCalculationRules().filter { (_, rule) ->
                val expectedRange = expectedInputCountRanges[rule.calculationMethod]
                expectedRange != null && rule.inputs.size !in expectedRange
            }

        assertTrue(
            rulesWithUnexpectedArity.isEmpty(),
            "The following calculation rules are configured with an unexpected number of inputs: " +
                rulesWithUnexpectedArity.joinToString(", ") { (file, rule) ->
                    "${file.name} (${rule.calculationMethod}: ${rule.inputs.size} inputs, expected " +
                        "${expectedInputCountRanges[rule.calculationMethod]})"
                },
        )
    }
}
