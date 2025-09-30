package org.dataland.datalandbackend.validator

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.DatalandBackend
import org.dataland.datalandbackend.utils.DataPointValidator
import org.dataland.datalandbackend.utils.DefaultMocks
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import java.io.File
import org.dataland.datalandbackendutils.utils.JsonUtils.defaultObjectMapper as objectMapper

@SpringBootTest(classes = [DatalandBackend::class], properties = ["spring.profiles.active=nodb"])
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@DefaultMocks
class DataPointSpecificationExampleValidationTest
    @Autowired
    constructor(
        private val dataPointValidator: DataPointValidator,
    ) {
        companion object {
            private const val DATA_POINT_BASE_TYPE_DIRECTORY =
                "../dataland-specification-service/src/main/resources/specifications/dataPointBaseTypes"

            @JvmStatic
            fun dataPointBaseTypeTestProvider(): List<Arguments> =
                File(DATA_POINT_BASE_TYPE_DIRECTORY)
                    .listFiles()!!
                    .filter {
                        it.isFile
                    }.map {
                        val dataPointBaseTypeId = it.nameWithoutExtension
                        Arguments.of(dataPointBaseTypeId, it)
                    }
        }

        @ParameterizedTest(name = "Ensure the datapoint example {0} matches the specification")
        @MethodSource("dataPointBaseTypeTestProvider")
        fun `ensure the datapoint example matches the specification`(
            ignored: String,
            file: File,
        ) {
            val fileContents = objectMapper.readTree(file)
            val validatedBy = fileContents["validatedBy"].asText()
            val example = fileContents["example"].toString()
            assertDoesNotThrow {
                dataPointValidator.validateConsistency(example, validatedBy, "correlationId")
            }
        }

        @ParameterizedTest(name = "Ensure the datapoint example {0} matches the schema structure and class")
        @MethodSource("dataPointBaseTypeTestProvider")
        fun `ensure the datapoint example matches the schema structure and class`(
            ignored: String,
            file: File,
        ) {
            val fileContents = objectMapper.readTree(file)
            val schema = fileContents["schema"]
            val example = fileContents["example"]
            val validatedBy = fileContents["validatedBy"].asText()

            requireNotNull(schema) { "Schema is missing in ${file.name}" }
            requireNotNull(example) { "Example is missing in ${file.name}" }

            val generated = buildJsonFromSchemaWithExample(schema, example, objectMapper)

            assertDoesNotThrow {
                dataPointValidator.validateConsistency(
                    objectMapper.writeValueAsString(generated),
                    validatedBy,
                    "correlationId",
                )
            }
        }

        fun buildJsonFromSchemaWithExample(
            schema: JsonNode,
            example: JsonNode,
            objectMapper: ObjectMapper,
        ): JsonNode =
            when {
                schema.isObject -> {
                    val objectNode = objectMapper.createObjectNode()
                    schema.fieldNames().forEach { field ->
                        val schemaField = schema[field]
                        val exampleField = example.get(field)
                        objectNode.set<JsonNode>(field, buildJsonFromSchemaWithExample(schemaField, exampleField, objectMapper))
                    }
                    objectNode
                }
                schema.isArray -> {
                    val array = objectMapper.createArrayNode()
                    val schemaElement = schema.firstOrNull()
                    if (example.isArray && schemaElement != null) {
                        example.forEach { exampleElement ->
                            array.add(buildJsonFromSchemaWithExample(schemaElement, exampleElement, objectMapper))
                        }
                    }
                    array
                }
                schema.isTextual -> {
                    example
                }
                else -> objectMapper.nullNode()
            }
    }
