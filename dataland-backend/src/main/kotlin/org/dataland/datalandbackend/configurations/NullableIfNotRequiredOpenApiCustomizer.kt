package org.dataland.datalandbackend.configurations

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.media.Schema
import org.springdoc.core.customizers.OpenApiCustomizer
import org.springframework.stereotype.Component

/**
 * This class customises the generated open api docs
 * by setting the nullable flag to properties that are nullable in the API model
 */
@Component
class NullableIfNotRequiredOpenApiCustomizer : OpenApiCustomizer {
    override fun customise(openApi: OpenAPI) {
        openApi.components.schemas.values.forEach { schema ->
            if (schema.properties == null) {
                return@forEach
            }
            (schema.properties as Map<String?, Schema<*>>).forEach { (name: String?, value: Schema<*>) ->
                if (schema.required == null || !schema.required.contains(name)) {
                    setPropertyNullable(value)
                }
            }
        }
    }

    private fun setPropertyNullable(property: Schema<*>) {
        val prefix = "#/components/schemas"
        if (property.`$ref` != null &&
            property.`$ref`
                .split("/")
                .dropLast(1)
                .joinToString("/") == prefix
        ) {
            val ref = property.`$ref`
            property.`$ref` = null
            property.nullable = true
            property.allOf(listOf(Schema<Any>(property.specVersion).also { it.`$ref` = ref }))
        } else {
            property.nullable = true
        }
    }
}
