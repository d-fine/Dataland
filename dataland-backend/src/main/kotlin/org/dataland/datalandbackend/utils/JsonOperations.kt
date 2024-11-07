package org.dataland.datalandbackend.utils

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import jakarta.validation.Validation
import org.dataland.datalandbackend.model.documents.CompanyReport
import org.dataland.datalandbackend.model.documents.ExtendedDocumentReference
import org.slf4j.LoggerFactory
import java.text.SimpleDateFormat
import java.time.LocalDate

object JsonOperations {
    private val logger = LoggerFactory.getLogger(javaClass)
    private const val JSON_PATH_NOT_FOUND_MESSAGE = "The path %s is not valid in the provided JSON node."

    val objectMapper: ObjectMapper = jacksonObjectMapper().findAndRegisterModules().setDateFormat(SimpleDateFormat("yyyy-MM-dd"))

    /**
     * Converts a JSON string to a JSON node.
     * @param json The JSON string
     * @return The JSON node
     */
    fun getJsonNodeFromString(json: String): JsonNode = objectMapper.readTree(json)

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
        } else {
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

    /**
     * Extracts the company report from an extended data source.
     * @param dataPointContent The content of the data point
     * @return The company report or null if it could not be extracted
     */
    fun getCompanyReportFromDataSource(dataPointContent: String): CompanyReport? {
        val dataSource = getJsonNodeFromString(dataPointContent).get("dataSource")

        if (dataSource == null || dataSource.isNull) {
            return null
        }

        return try {
            objectMapper.readValue(dataSource.toString(), ExtendedDocumentReference::class.java).toCompanyReport()
        } catch (ignore: Exception) {
            null
        }
    }

    /**
     * Extracts the mapping of file references to publication dates from a data set.
     * @param dataSetContent The content of the data set as JSON node
     * @param jsonPath The JSON path to the referenced reports
     * @return The mapping of file references to publication dates
     */
    fun getFileReferenceToPublicationDateMapping(
        dataSetContent: JsonNode,
        jsonPath: String,
    ): Map<String, LocalDate> {
        val result = mutableMapOf<String, LocalDate>()
        val referencedReportsNode: JsonNode

        try {
            referencedReportsNode = navigateToNode(dataSetContent, jsonPath)
        } catch (ex: IllegalArgumentException) {
            logger.warn("Could not extract the fileReference to publicationDate mapping: ${ex.message}")
            return result
        }

        val fields = referencedReportsNode.fields()
        while (fields.hasNext()) {
            val referencedReport = fields.next().value
            if (referencedReport is ObjectNode) {
                val publicationDate = referencedReport.get("publicationDate")
                val fileReference = referencedReport.get("fileReference")
                if (publicationDate != null && publicationDate.isTextual) {
                    result[fileReference.asText()] = LocalDate.parse(publicationDate.asText())
                }
            }
        }

        return result
    }

    /**
     * Navigates to a JSON node identified by a JSON path.
     * @param jsonNode The JSON node to navigate
     * @param jsonPath The JSON path to the target node
     * @return The target JSON node
     */
    fun navigateToNode(
        jsonNode: JsonNode,
        jsonPath: String,
    ): JsonNode {
        var currentNode = jsonNode
        jsonPath.split(".").forEach { path ->
            if (currentNode.has(path)) {
                currentNode = currentNode.get(path)
            } else {
                throw IllegalArgumentException(JSON_PATH_NOT_FOUND_MESSAGE.format(jsonPath))
            }
        }
        require(!(currentNode.isNull || !currentNode.isObject)) { JSON_PATH_NOT_FOUND_MESSAGE.format(jsonPath) }

        return currentNode
    }

    /**
     * Updates the publication date in a JSON node.
     * @param jsonNode The JSON node to update
     * @param fileReferenceToPublicationDate The mapping of file references to publication dates
     * @param currentNodeName The name of the current JSON node
     */
    fun updatePublicationDateInJsonNode(
        jsonNode: JsonNode,
        fileReferenceToPublicationDate: Map<String, LocalDate>,
        currentNodeName: String,
    ) {
        if (jsonNode.isObject && currentNodeName == "dataSource" && jsonNode.has("fileReference")) {
            val fileReference = jsonNode.get("fileReference").asText()
            if (fileReferenceToPublicationDate.containsKey(fileReference)) {
                (jsonNode as ObjectNode).put("publicationDate", fileReferenceToPublicationDate[fileReference].toString())
            }
        } else {
            val fields = jsonNode.fields()
            while (fields.hasNext()) {
                val jsonField = fields.next()
                updatePublicationDateInJsonNode(jsonField.value, fileReferenceToPublicationDate, jsonField.key)
            }
        }
    }

    /**
     * Inserts the referenced reports into a JSON node.
     * @param inputJsonNode The JSON node to update
     * @param targetPath The path to the target node
     * @param referencedReports The referencedReports object to be inserted
     */
    fun insertReferencedReports(
        inputJsonNode: JsonNode,
        targetPath: String,
        referencedReports: Map<String, CompanyReport>,
    ) {
        val referencedReportsNode = navigateToNode(inputJsonNode, targetPath)
        if (referencedReports.isEmpty()) {
            (referencedReportsNode as ObjectNode).set<JsonNode>("referencedReports", getJsonNodeFromString("null"))
        } else {
            (referencedReportsNode as ObjectNode).set<JsonNode>("referencedReports", objectMapper.valueToTree(referencedReports))
        }
    }
}
