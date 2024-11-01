package org.dataland.datalandbackend.utils

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import jakarta.validation.Validation

object JsonOperations {
    /**
     * Converts a JSON string to a JSON node.
     * @param json The JSON string
     * @return The JSON node
     */
    fun getJsonNodeFromString(json: String): JsonNode = ObjectMapper().readTree(json)

    /**
     * Gets the string value of the JSON node identified by the (possibly) nested JSON path.
     * @param jsonNode The JSON node
     * @param jsonPath The JSON path identifying the value
     * @return The string representation of the value
     */
    fun getValueFromJsonNode(
        jsonNode: JsonNode,
        jsonPath: String,
    ): String {
        var currentNode = jsonNode
        jsonPath.split(".").forEach { path ->
            currentNode = currentNode.get(path) ?: return ""
        }
        return if (currentNode.isNull) {
            ""
        } else if (currentNode.isTextual) {
            currentNode.textValue()
        } else {
            currentNode.toString()
        }
    }

    /**
     * Extracts all data points referenced in a framework template.
     * @param jsonNode A JSON node (the framework template as JSON node in the initial call)
     * @param fullJsonPath The full JSON path to the current node
     * @return A map of the data points
     */
    fun extractDataPointsFromFrameworkTemplate(
        jsonNode: JsonNode,
        fullJsonPath: String,
    ): Map<String, String> {
        val results = mutableMapOf<String, String>()

        if (jsonNode.has("id") && jsonNode.has("ref")) {
            results[fullJsonPath] = jsonNode.get("id").asText()
        } else {
            val fields = jsonNode.fields()
            while (fields.hasNext()) {
                val jsonField = fields.next()
                val extendedJsonPath = if (fullJsonPath.isEmpty()) jsonField.key else "$fullJsonPath.${jsonField.key}"
                results += extractDataPointsFromFrameworkTemplate(jsonField.value, extendedJsonPath)
            }
        }

        return results
    }

    /**
     * Replaces a field in a framework template with a new value.
     * @param frameworkTemplate The JSON node representation of a framework template
     * @param fullFieldName The full name of the field to replace
     * @param currentJsonPath The current JSON path
     * @param replacementValue The new value
     */
    fun replaceFieldInTemplate(
        frameworkTemplate: JsonNode,
        fullFieldName: String,
        currentJsonPath: String,
        replacementValue: JsonNode,
    ) {
        val simpleFieldName = fullFieldName.split(".").last()
        val expectedFullPath = "$currentJsonPath.$simpleFieldName"

        if (frameworkTemplate.has(simpleFieldName) && expectedFullPath == fullFieldName) {
            (frameworkTemplate as ObjectNode).set<JsonNode?>(simpleFieldName, replacementValue)
        } else if (frameworkTemplate.isObject) {
            val fields = frameworkTemplate.fields()
            while (fields.hasNext()) {
                val jsonField = fields.next()
                val jsonPath = if (currentJsonPath.isEmpty()) jsonField.key else "$currentJsonPath.${jsonField.key}"
                replaceFieldInTemplate(jsonField.value, fullFieldName, jsonPath, replacementValue)
            }
        }
    }

    /**
     * Validates the consistency of a JSON string with a given class.
     * @param jsonData The JSON string to validate
     * @param className The name of the class to validate against
     * @param correlationId The correlation ID of the operation
     */
    fun validateConsistency(
        jsonData: String,
        className: String,
        correlationId: String,
    ) {
        val classForValidation = Class.forName(className).kotlin.java
        val validator = Validation.buildDefaultValidatorFactory().validator
        val objectMapper = jacksonObjectMapper().findAndRegisterModules()
        val dataPointObject = objectMapper.readValue(jsonData, classForValidation)
        val violations = validator.validate(dataPointObject)
        if (violations.isNotEmpty()) {
            var errorMessage = "Validation failed for data point of type $className (correlation ID: $correlationId): "
            violations.forEach {
                errorMessage += (it.message)
            }
            throw IllegalArgumentException(errorMessage)
        }
    }
}
