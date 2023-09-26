package org.dataland.datalandbackend.configurations

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.media.Schema
import org.springdoc.core.customizers.OpenApiCustomizer
import org.springframework.stereotype.Component

@Component
class NullableIfNotRequiredOpenApiCustomizer : OpenApiCustomizer {
    override fun customise(openApi: OpenAPI) {
        openApi.components.schemas.values.forEach { schema ->
            if (schema.properties == null) {
                return@forEach
            }
            (schema.properties as Map<String?, Schema<*>>).forEach { (name: String?, value: Schema<*>) ->
                if (schema.required == null || !schema.required.contains(name)) {
                    val prefix = "#/components/schemas"
                    if(value.`$ref` != null && value.`$ref`.split("/").dropLast(1).joinToString("/") == prefix) {
                        println(value.`$ref`)
                        val ref = value.`$ref`
                        value.`$ref` = null
                        value.nullable = true
                        value.allOf(listOf(Schema<Any>(value.specVersion).also { it.`$ref` = ref }))
                    } else {
                        value.nullable = true
                    }
                }
            }
        }
    }
}