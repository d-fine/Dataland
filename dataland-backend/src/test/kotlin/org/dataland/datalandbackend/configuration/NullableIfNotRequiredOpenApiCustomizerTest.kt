package org.dataland.datalandbackend.configuration

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.media.Schema
import org.dataland.datalandbackend.configurations.NullableIfNotRequiredOpenApiCustomizer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class NullableIfNotRequiredOpenApiCustomizerTest {
    @Test
    fun `check if the open api config is transformed as expected`() {
        val openApi =
            OpenAPI()
                .components(
                    Components().schemas(
                        mapOf(
                            "schema1" to
                                Schema<Any>().also {
                                    it.properties =
                                        mutableMapOf<String, Schema<Any>>(
                                            "prop1" to Schema<Any>().also { it.type = "number" },
                                            "prop2" to Schema<Any>().also { it.`$ref` = "#/components/schemas/schema2" },
                                        )
                                },
                            "schema2" to
                                Schema<Any>().also {
                                    it.required = listOf("prop3")
                                    it.properties =
                                        mutableMapOf<String, Schema<Any>>(
                                            "prop3" to Schema<Any>().also { it.type = "string" },
                                        )
                                },
                        ),
                    ),
                )
        assertOpenApiFormat(openApi, null)
        val customizer = NullableIfNotRequiredOpenApiCustomizer()
        customizer.customise(openApi)
        assertOpenApiFormat(openApi, true)
    }

    private fun assertOpenApiFormat(
        openApi: OpenAPI,
        nullableShouldBe: Boolean?,
    ) {
        assertEquals(openApi.components.schemas.size, 2)
        val schemas = openApi.components.schemas
        val schema1 = schemas.getValue("schema1")
        val schema2 = schemas.getValue("schema2")
        assertEquals(schema1.properties.size, 2)
        assertNull(schema1.nullable)
        assert(schema1.properties.getValue("prop1").nullable == nullableShouldBe)
        assert(schema1.properties.getValue("prop2").nullable == nullableShouldBe)
        assertEquals(schema2.properties.size, 1)
        assertNull(schema2.nullable)
        assertNull(schema2.properties.getValue("prop3").nullable)
    }
}
