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
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.time.LocalDate

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

    private val logger = LoggerFactory.getLogger(javaClass)
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
     * This includes checking for duplicate file references.
     */
    fun validateReferencedReportConsistency(referencedReports: Map<String, CompanyReport>) {
        val observedFileReferences = mutableSetOf<String>()
        for ((nameAccordingToKey, companyReport) in referencedReports.entries) {
            if (companyReport.fileReference in observedFileReferences) {
                throw InvalidInputApiException(
                    "Inconsistent reference reports field.",
                    "The file reference ${companyReport.fileReference} is used multiple times.",
                )
            }
            if (companyReport.fileName != null && companyReport.fileName != nameAccordingToKey) {
                throw InvalidInputApiException(
                    "Inconsistent reference reports field.",
                    "The file name ${companyReport.fileName} does not match the dictionary key $nameAccordingToKey.",
                )
            }
            observedFileReferences.add(companyReport.fileReference)
        }
    }

    /**
     * Validates if a company report is consistent with referenced reports.
     */
    fun validateReportConsistencyWithGlobalList(
        report: CompanyReport,
        referencedReports: Map<String, CompanyReport>,
    ) {
        val matchingReport =
            referencedReports.values.firstOrNull { it.fileReference == report.fileReference } ?: throw InvalidInputApiException(
                "Data point report not listed in referenced reports",
                "The report '${report.fileReference}' is not contained in the referenced reports field.",
            )

        if (report.publicationDate != null && report.publicationDate != matchingReport.publicationDate) {
            logger.warn(
                "The publication date of the report '${report.fileName}' is '${report.publicationDate}' " +
                    "and inconsistent with the publication date listed in the referenced reports '${matchingReport.publicationDate}'. " +
                    "The publication date of the report will be overwritten to '${matchingReport.publicationDate}'.",
            )
        }
        if (report.fileName != null && matchingReport.fileName != null && report.fileName != matchingReport.fileName) {
            throw InvalidInputApiException(
                "Inconsistent file name",
                "The file name of the report '${report.fileName}' is not consistent " +
                    "with the file name listed in the referenced reports which is '${matchingReport.fileName}'.",
            )
        }
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
            val fields = contentNode.fields()
            while (fields.hasNext()) {
                val jsonField = fields.next()
                getAllCompanyReportsFromDataSource(objectMapper.writeValueAsString(jsonField.value), allCompanyReports)
            }
        }
    }

    /**
     * Extracts the company report from an extended data source.
     * @param dataPoint The string representation of the contained data
     * @return The company report or null if it could not be extracted
     */
    fun getCompanyReportFromDataSource(dataPoint: String): CompanyReport? {
        val dataSource = objectMapper.readTree(dataPoint).get(DATA_SOURCE_FIELD)

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

    private fun nodeMayRequireUpdate(jsonNode: JsonNode): Boolean =
        jsonNode.isObject &&
            (jsonNode.has(FILE_REFERENCE_FIELD) || jsonNode.has(FILE_NAME_FIELD))

    /**
     * Updates the publication date in a JSON node.
     * @param jsonNode The JSON node to update
     * @param fileReferenceToPublicationDate The mapping of file references to publication dates
     * @param currentNodeName The name of the current JSON node
     */
    fun updateJsonNodeWithDataFromReferencedReports(
        jsonNode: JsonNode,
        fileReferenceToPublicationDate: Map<String, LocalDate>,
        fileReferenceToFileName: Map<String, String>,
        currentNodeName: String,
    ) {
        if (currentNodeName == DATA_SOURCE_FIELD && nodeMayRequireUpdate(jsonNode)) {
            val fileReference = jsonNode.get(FILE_REFERENCE_FIELD).asText()
            if (fileReferenceToPublicationDate.containsKey(fileReference)) {
                (jsonNode as ObjectNode).put(PUBLICATION_DATE_FIELD, fileReferenceToPublicationDate[fileReference].toString())
            }
            if (fileReferenceToFileName.containsKey(fileReference)) {
                (jsonNode as ObjectNode).put(FILE_NAME_FIELD, fileReferenceToFileName[fileReference])
            }
        } else {
            val fields = jsonNode.fields()
            while (fields.hasNext()) {
                val jsonField = fields.next()
                updateJsonNodeWithDataFromReferencedReports(
                    jsonField.value, fileReferenceToPublicationDate,
                    fileReferenceToFileName, jsonField.key,
                )
            }
        }
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
