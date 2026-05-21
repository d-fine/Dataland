package org.dataland.datalandbackend.services.dataPoints

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.dataland.datalandbackend.services.datapoints.DataPointConversion
import org.hibernate.validator.internal.util.Contracts.assertTrue
import org.junit.jupiter.api.Test
import java.io.File

private data class RawCalculationRule(
    val inputs: List<String>,
    val calculationMethod: String,
)

@JsonIgnoreProperties(ignoreUnknown = true)
private data class RawDataPointType(
    val calculationRules: List<RawCalculationRule>? = null,
)

class CoveredCalculationRulesTest {
    @Test
    fun `check that all calculation rules specified in the framework toolbox are also implemented`() {
        val objectMapper = jacksonObjectMapper()
        val dataPointTypesFolder =
            File("../dataland-specification-service/src/main/resources/specifications/dataPointTypes")

        val specifiedCalculationRules =
            dataPointTypesFolder
                .listFiles { f -> f.extension == "json" }
                .orEmpty()
                .flatMap { file ->
                    objectMapper.readValue<RawDataPointType>(file).calculationRules.orEmpty()
                }.distinctBy { it.calculationMethod }

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
