package org.dataland.datalandbackend.services.dataPoints

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.module.kotlin.readValue
import org.dataland.datalandbackend.services.datapoints.DataPointConversion
import org.dataland.datalandbackendutils.utils.JsonUtils.defaultObjectMapper
import org.dataland.specificationservice.openApiClient.model.CalculationRule
import org.hibernate.validator.internal.util.Contracts.assertTrue
import org.junit.jupiter.api.Test
import java.io.File

@JsonIgnoreProperties(ignoreUnknown = true)
private data class RawDataPointType(
    val calculationRules: List<CalculationRule>? = null,
)

class CoveredCalculationRulesTest {
    @Test
    fun `check that all calculation rules specified in the framework toolbox are also implemented`() {
        val dataPointTypesFolder =
            File("../dataland-specification-service/src/main/resources/specifications/dataPointTypes")

        val specifiedCalculationRules =
            dataPointTypesFolder
                .listFiles { f -> f.extension == "json" }
                .orEmpty()
                .flatMap { file ->
                    defaultObjectMapper.readValue<RawDataPointType>(file).calculationRules.orEmpty()
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
