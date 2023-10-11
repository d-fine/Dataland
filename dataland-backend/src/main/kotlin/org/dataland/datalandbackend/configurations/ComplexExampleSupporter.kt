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
    override fun customise(openApi: OpenAPI) {
        openApi.components.schemas.getValue("SfdrEnvironmentalEnergyPerformance").properties
            .getValue("applicableHighImpactClimateSector").additionalProperties =
            Schema<Any>(openApi.specVersion).apply { this.`$ref` = "#/components/schemas/ExtendedDataPointBigDecimal" }
    }
}
