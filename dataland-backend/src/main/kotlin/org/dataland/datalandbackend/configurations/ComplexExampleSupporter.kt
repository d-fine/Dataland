package org.dataland.datalandbackend.configurations

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.media.Schema
import org.springdoc.core.customizers.OpenApiCustomizer
import org.springframework.stereotype.Component

/**
 * This class adds additional properties (ref) to fields of a more complex type (e.g. Map<...,...>) that are equipped
 * with an example, making it in some cases not possible to keep the information about the corresponding type used in
 * the map
 */

@Component
class ComplexExampleSupporter : OpenApiCustomizer {

    private fun addRefToField(openApi: OpenAPI, schema: String, field: String, refType: String) {
        openApi.components.schemas.getValue(schema)
            .properties.getValue(field).additionalProperties =
            Schema<Any>(openApi.specVersion).apply { this.`$ref` = "#/components/schemas/$refType" }
    }

    override fun customise(openApi: OpenAPI) {
        addRefToField(
            openApi,
            "SfdrEnvironmentalEnergyPerformance",
            "applicableHighImpactClimateSectors",
            "ExtendedDataPointBigDecimal",
        )
    }
}
