package org.dataland.datalandbackend.configurations

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.media.Schema
import org.springdoc.core.customizers.OpenApiCustomizer
import org.springframework.stereotype.Component

@Component
class NullableIfNotRequiredOpenApiCustomizer : OpenApiCustomizer {
    override fun customise(openApi: OpenAPI) {
        for (schema in openApi.components.schemas.values) {
            if (schema.properties == null) {
                continue
            }
            (schema.properties as Map<String?, Schema<*>>).forEach { (name: String?, value: Schema<*>) ->
                if (schema.required == null || !schema.required.contains(name)) {
//                    val prefix = "#/components/schemas"
//                    if(value.`$ref` != null &&
//                        value.`$ref`.split("/").dropLast(1).joinToString("/") == prefix) {
//                        println(value.`$ref`)
//                        (schema.properties as Map<String?, Schema<*>>).filterKeys {
//                            it == value.`$ref`.split("/").last()
//                        }.forEach {
//                            println("NULL ${value.`$ref`}")
//                            it.value.nullable = true
//                        }
//                    } else {
                        value.nullable = true
//                    }
                }
            }
        }
    }
}