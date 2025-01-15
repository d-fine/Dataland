package org.dataland.datalandbackendutils.services.utils

import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.utils.JsonSpecificationUtils
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class JsonSpecificationUtilsTest {
    private val objectMapper = jacksonObjectMapper()
    private val demoSpecification =
        objectMapper.readTree(
            """
        {
            "field": {
                "id": "field1",
                "ref": "field1"
            },
            "group": {
                "subgroup": {
                    "subgroupfield": {
                        "id": "subgroupfield",
                        "ref": "subgroupfield"
                    }
                },
                "groupfield": {
                    "id": "groupfield",
                    "ref": "groupfield"
                }
            }
        }
    """,
        ) as ObjectNode

    private val demoValueMap =
        mapOf(
            "field1" to objectMapper.readTree("""{"value": "field1"}"""),
            "groupfield" to objectMapper.readTree("12"),
            "subgroupfield" to objectMapper.readTree("\"test\""),
        )

    @Test
    fun `hydration should work for a simple specification`() {
        val hydrated = JsonSpecificationUtils.hydrateJsonSpecification(demoSpecification) { demoValueMap[it] }
        val expected =
            objectMapper.readTree(
                """
                {
                    "field": {"value": "field1"},
                    "group": {
                        "subgroup": {"subgroupfield": "test"},
                        "groupfield": 12
                    }
                }
                """.trimIndent(),
            )
        assertEquals(expected, hydrated)
    }

    @Test
    fun `hydration should omit empty subgroups`() {
        val hydrated =
            JsonSpecificationUtils.hydrateJsonSpecification(demoSpecification) {
                if (it == "subgroupfield") null else demoValueMap[it]
            }
        val expected =
            objectMapper.readTree(
                """
                {
                    "field": {"value": "field1"},
                    "group": {
                        "groupfield": 12
                    }
                }
                """.trimIndent(),
            )
        assert(hydrated == expected)
    }

    @Test
    fun `dehydration should work for a simple specification`() {
        val hydrated = JsonSpecificationUtils.hydrateJsonSpecification(demoSpecification) { demoValueMap[it] }
        val dehydrated = JsonSpecificationUtils.dehydrateJsonSpecification(demoSpecification, hydrated as ObjectNode)
        val transformedMap = dehydrated.mapValues { it.value.content }
        assertEquals(demoValueMap, transformedMap)
    }

    @Test
    fun `dehydration should work for a simple specification with missing values`() {
        val partialValueMap = demoValueMap.toMutableMap()
        partialValueMap.remove("groupfield")
        val hydrated = JsonSpecificationUtils.hydrateJsonSpecification(demoSpecification) { partialValueMap[it] }
        val dehydrated = JsonSpecificationUtils.dehydrateJsonSpecification(demoSpecification, hydrated as ObjectNode)
        val transformedMap = dehydrated.mapValues { it.value.content }
        assertEquals(partialValueMap, transformedMap)
    }

    @Test
    fun `dehydration should fail on unexpected inputs`() {
        val unexpectedInput =
            objectMapper.readTree(
                """
                {
                    "unexpectedGreeting": "Hello!"
                }
                """.trimIndent(),
            )
        val exception =
            assertThrows<InvalidInputApiException> {
                JsonSpecificationUtils.dehydrateJsonSpecification(demoSpecification, unexpectedInput as ObjectNode)
            }
        assert(exception.message.contains("unexpectedGreeting"))
    }
}
