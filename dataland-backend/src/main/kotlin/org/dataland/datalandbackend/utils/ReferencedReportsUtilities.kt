package org.dataland.datalandbackend.utils

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.contains
import com.fasterxml.jackson.module.kotlin.convertValue
import org.dataland.datalandbackend.model.documents.CompanyReport
import org.dataland.datalandbackend.model.documents.ExtendedDocumentReference
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandbackendutils.utils.JsonSpecificationLeaf
import org.dataland.datalandbackendutils.utils.JsonUtils
import org.dataland.specificationservice.openApiClient.model.IdWithRef
import org.springframework.stereotype.Service

/**
 * Utilities for handling referenced reports in a specification schema.
 */
@Service
class ReferencedReportsUtilities {
    companion object {
        private const val JSON_PATH_NOT_FOUND_MESSAGE = "The path %s is not valid in the provided JSON node."

        private const val PUBLICATION_DATE_FIELD = "publicationDate"
        private const val FILE_REFERENCE_FIELD = "fileReference"
        private const val DATA_SOURCE_FIELD = "dataSource"
        private const val FILE_NAME_FIELD = "fileName"

        const val REFERENCED_REPORTS_ID = "referencedReports"
    }

    private val objectMapper = JsonUtils.defaultObjectMapper

    /**
     * Parses the referenced reports from a JSON leaf.
     */
    fun parseReferencedReportsFromJsonLeaf(referencedReportsLeaf: JsonSpecificationLeaf?): Map<String, CompanyReport> {
        if (referencedReportsLeaf == null) {
            return emptyMap()
        }
        return objectMapper.convertValue<Map<String, CompanyReport>>(referencedReportsLeaf.content)
    }

    /**
     * Validates the consistency of the referenced reports field.
     * This includes checking for duplicate file references and ensuring that inferable
     * fields (file name, publication date) are not set.
     */
    fun validateReferencedReportConsistency(referencedReports: Map<String, CompanyReport>) {
        val observedFileReferences = mutableSetOf<String>()
        val violations = mutableListOf<String>()
        for ((nameAccordingToKey, companyReport) in referencedReports.entries) {
            if (companyReport.fileReference in observedFileReferences) {
                throw InvalidInputApiException(
                    "Inconsistent reference reports field.",
                    "The file reference ${companyReport.fileReference} is used multiple times.",
                )
            }
            if (companyReport.fileName != null) {
                violations.add(
                    "The referenced report '$nameAccordingToKey' must not set the field '$FILE_NAME_FIELD' " +
                        "(found '${companyReport.fileName}').",
                )
            }
            if (companyReport.publicationDate != null) {
                violations.add(
                    "The referenced report '$nameAccordingToKey' must not set the field '$PUBLICATION_DATE_FIELD' " +
                        "(found '${companyReport.publicationDate}').",
                )
            }
            observedFileReferences.add(companyReport.fileReference)
        }
        if (violations.isNotEmpty()) {
            throw InvalidInputApiException(
                "Referenced reports contain inferable fields.",
                "The referenced reports field contains fields that are not allowed to be set: " +
                    violations.joinToString(" "),
            )
        }
    }

    /**
     * Validates if a company report is consistent with referenced reports.
     */
    fun validateReportConsistencyWithGlobalList(
        report: CompanyReport,
        referencedReports: Map<String, CompanyReport>,
    ) {
        referencedReports.values.firstOrNull { it.fileReference == report.fileReference } ?: throw InvalidInputApiException(
            "Data point report not listed in referenced reports",
            "The report '${report.fileReference}' is not contained in the referenced reports field.",
        )
    }

    /**
     * Extracts all company reports recursively from a string representation of a JSON node.
     * @param content The string representation of the JSON node to extract the reports from
     * @param allCompanyReports The list to store the extracted reports in
     */
    fun getAllCompanyReportsFromDataSource(
        content: String,
        allCompanyReports: MutableList<CompanyReport>,
    ) {
        val contentNode = objectMapper.readTree(content)
        if (contentNode.contains(DATA_SOURCE_FIELD)) {
            val foundReport = getCompanyReportFromDataSource(content)
            if (foundReport != null) {
                allCompanyReports.add(foundReport)
            }
        } else {
            contentNode
                .fieldNames()
                .asSequence()
                .map { fieldName -> fieldName to contentNode[fieldName] }
                .forEach { (_, value) ->
                    getAllCompanyReportsFromDataSource(objectMapper.writeValueAsString(value), allCompanyReports)
                }
        }
    }

    /**
     * Extracts the company report from an extended data source.
     * @param dataPoint The string representation of the contained data
     * @return The company report or null if it could not be extracted
     */
    fun getCompanyReportFromDataSource(dataPoint: String): CompanyReport? {
        val dataSource = objectMapper.readTree(dataPoint)[DATA_SOURCE_FIELD]

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

    private fun isDataSourceNode(jsonNode: JsonNode): Boolean =
        jsonNode.isObject &&
            (jsonNode.has(FILE_REFERENCE_FIELD) || jsonNode.has(FILE_NAME_FIELD))

    /**
     * Recursively validates that no data source node within a JSON node tree sets the
     * file name or publication date fields, as these fields must be inferred rather than
     * set explicitly on upload.
     *
     * @param jsonNode The JSON node to process
     * @param currentNodeName The name of the current JSON node, used to identify data source nodes
     * @param currentPath The JSON path to the current node, used for error messages
     * @param violations The list that collects all violation messages found in the tree
     * @return the list of violation messages found in the tree
     */
    fun validateDataSourcesDoNotContainInferableFields(
        jsonNode: JsonNode,
        currentNodeName: String,
        currentPath: String = currentNodeName,
        violations: MutableList<String> = mutableListOf(),
    ): List<String> {
        when {
            currentNodeName == DATA_SOURCE_FIELD && isDataSourceNode(jsonNode) -> {
                if (jsonNode.hasNonNull(FILE_NAME_FIELD)) {
                    violations.add(
                        "The data source at '$currentPath' must not set the field '$FILE_NAME_FIELD' " +
                            "(found '${jsonNode.get(FILE_NAME_FIELD).asText()}').",
                    )
                }
                if (jsonNode.hasNonNull(PUBLICATION_DATE_FIELD)) {
                    violations.add(
                        "The data source at '$currentPath' must not set the field '$PUBLICATION_DATE_FIELD' " +
                            "(found '${jsonNode.get(PUBLICATION_DATE_FIELD).asText()}').",
                    )
                }
            }
            jsonNode.isObject -> {
                jsonNode
                    .fieldNames()
                    .asSequence()
                    .map { fieldName -> fieldName to jsonNode.get(fieldName) }
                    .forEach { (fieldName, value) ->
                        validateDataSourcesDoNotContainInferableFields(
                            value,
                            currentNodeName = fieldName,
                            currentPath = "$currentPath.$fieldName",
                            violations = violations,
                        )
                    }
            }
            jsonNode.isArray -> {
                jsonNode.forEachIndexed { index, arrayEntry ->
                    validateDataSourcesDoNotContainInferableFields(
                        arrayEntry,
                        currentNodeName = currentNodeName,
                        currentPath = "$currentPath[$index]",
                        violations = violations,
                    )
                }
            }
            else -> {
                // Primitive values cannot contain nested data source nodes.
            }
        }
        return violations
    }

    /**
     * Inserts the referenced reports entry into the JSON node representation of a specification schema.
     * @param inputJsonNode The schema as JSON node to be updated
     * @param targetPath The path specifying where to insert the entry
     */
    fun insertReferencedReportsIntoFrameworkSchema(
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
