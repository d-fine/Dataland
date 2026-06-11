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
}
