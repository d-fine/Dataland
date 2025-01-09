package org.dataland.datalandbackend.utils

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.convertValue
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.dataland.datalandbackend.model.documents.CompanyReport
import org.dataland.datalandbackend.model.documents.ExtendedDocumentReference
import org.dataland.datalandbackendutils.utils.JsonSpecificationLeaf
import org.dataland.specificationservice.openApiClient.model.IdWithRef
import java.text.SimpleDateFormat
import java.time.LocalDate

object ReferencedReportsUtilities {
    private const val JSON_PATH_NOT_FOUND_MESSAGE = "The path %s is not valid in the provided JSON node."

    private const val PUBLICATION_DATE_FIELD = "publicationDate"
    private const val FILE_REFERENCE_FIELD = "fileReference"
    private const val DATA_SOURCE_FIELD = "dataSource"

    const val REFERENCED_REPORTS_ID = "referencedReports"

    val objectMapper: ObjectMapper = jacksonObjectMapper().findAndRegisterModules().setDateFormat(SimpleDateFormat("yyyy-MM-dd"))

    /**
     * Converts a JSON string to a JSON node.
     * @param json The JSON string
     * @return The JSON node
     */
    fun getJsonNodeFromString(json: String): JsonNode = objectMapper.readTree(json)

    /**
     * Extracts the company report from an extended data source.
     * @param dataPointContent The content of the data point
     * @return The company report or null if it could not be extracted
     */
    fun getCompanyReportFromDataSource(dataPointContent: String): CompanyReport? {
        val dataSource = getJsonNodeFromString(dataPointContent).get(DATA_SOURCE_FIELD)

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
     * @param referencedReportsLeaf The entry of the referenced reports as a JsonSpecificationLeaf
     * @return The mapping of file references to publication dates
     */
    fun getFileReferenceToPublicationDateMapping(referencedReportsLeaf: JsonSpecificationLeaf?): Map<String, LocalDate> {
        val fileToPublicationDateMapping = mutableMapOf<String, LocalDate>()
        if (referencedReportsLeaf == null) {
            return fileToPublicationDateMapping
        }
        val referencedReports = objectMapper.convertValue<Map<String, CompanyReport>>(referencedReportsLeaf.content)
        referencedReports.values.forEach { companyReport ->
            if (companyReport.publicationDate != null) {
                fileToPublicationDateMapping[companyReport.fileReference] = companyReport.publicationDate
            }
        }
        return fileToPublicationDateMapping
    }

    /**
     * Navigates to a JSON node identified by a JSON path.
     * @param jsonNode The JSON node to navigate
     * @param jsonPath The JSON path to the target node
     * @return The target JSON node
     */
    private fun navigateToNode(
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
        if (jsonNode.isObject && currentNodeName == DATA_SOURCE_FIELD && jsonNode.has(FILE_REFERENCE_FIELD)) {
            val fileReference = jsonNode.get(FILE_REFERENCE_FIELD).asText()
            if (fileReferenceToPublicationDate.containsKey(fileReference)) {
                (jsonNode as ObjectNode).put(PUBLICATION_DATE_FIELD, fileReferenceToPublicationDate[fileReference].toString())
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
     * Inserts the referenced reports entry into the JSON node representation of a specification schema.
     * @param inputJsonNode The schema as JSON node to be updated
     * @param targetPath The path specifying where to insert the entry
     */
    fun insertReferencedReports(
        inputJsonNode: JsonNode,
        targetPath: String?,
    ) {
        if (targetPath == null) return
        val insertLocation = targetPath.split(".").dropLast(1).joinToString(".")
        val insertName = targetPath.split(".").last()
        val parentNode = navigateToNode(inputJsonNode, insertLocation)
        (parentNode as ObjectNode).set<JsonNode>(insertName, objectMapper.valueToTree(IdWithRef(REFERENCED_REPORTS_ID, "dummy")))
    }
}
