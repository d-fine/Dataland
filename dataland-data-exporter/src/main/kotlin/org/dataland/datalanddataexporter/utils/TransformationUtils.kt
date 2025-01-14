package org.dataland.datalanddataexporter.utils

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.dataland.datalandbackend.openApiClient.model.CompanyAssociatedDataSfdrData
import org.dataland.datalandbackend.openApiClient.model.CompanyInformation
import java.text.SimpleDateFormat

/**
 * A class containing utility methods for transforming data from JSON to CSV.
 */
object TransformationUtils {
    const val LEI_IDENTIFIER = "Lei"
    const val ISIN_IDENTIFIER = "Isin"
    const val LEI_HEADER = "LEI"
    const val ISIN_HEADER = "ISIN"
    const val COMPANY_ID_HEADER = "Company ID"
    const val COMPANY_NAME_HEADER = "Company Name"
    const val REPORTING_PERIOD_HEADER = "Reporting Period"
    private const val NODE_FILTER = ".referencedReports."

    /**
     * Method to extract the mapping of LEI to ISIN from the given company data
     * @param companyData the company data containing the LEI and ISINs
     * @return a list of mappings from LEI to ISIN (empty list if no LEI or ISINs are present)
     */
    fun getLeiToIsinMapping(companyData: CompanyInformation): List<Map<String, String>> {
        val lei = companyData.identifiers[LEI_IDENTIFIER] ?: emptyList()
        val isins = companyData.identifiers[ISIN_IDENTIFIER] ?: emptyList()
        val leiToIsinData = mutableListOf(mapOf<String, String>())
        if (lei.isNotEmpty()) {
            isins.forEach { isin ->
                leiToIsinData.add(mapOf(LEI_HEADER to lei.first(), ISIN_HEADER to isin))
            }
        }
        return leiToIsinData
    }

    /**
     * Gets the headers from the transformation rules.
     * @param transformationRules The transformation rules
     * @return A list of headers
     */
    fun getHeaders(transformationRules: Map<String, String>): List<String> {
        val headers = mutableListOf<String>()
        transformationRules.forEach { (_, csvHeader) -> if (csvHeader.isNotEmpty()) headers.add(csvHeader) }
        require(headers.isNotEmpty()) { "No headers found in transformation rules." }
        headers.addAll(getCompanyRelatedHeaders())
        require(headers.distinct().size == headers.size) { "Duplicate headers found in transformation rules." }
        return headers
    }

    /**
     * Gets the headers for company-related entries that are independent of the data transformation rules.
     * @return A list of headers
     */
    private fun getCompanyRelatedHeaders(): List<String> =
        listOf(COMPANY_ID_HEADER, COMPANY_NAME_HEADER, REPORTING_PERIOD_HEADER, LEI_HEADER)

    /**
     * Checks the consistency of the transformation rules with the JSON data.
     * @param node The JSON node
     * @param transformationRules The transformation rules
     */
    fun checkConsistency(
        node: JsonNode,
        transformationRules: Map<String, String>,
        legacyFields: Map<String, String>,
    ) {
        println("transformation rules")
        println(transformationRules)
        println("legacy fields")
        println(legacyFields)
        val leafNodesInJsonNode: List<String> = getNonArrayLeafNodeFieldNames(node, "")
        val filteredNodes = leafNodesInJsonNode.filter { !it.contains(NODE_FILTER) }
        require(transformationRules.keys.containsAll(filteredNodes)) {
            "Transformation rules do not cover all leaf nodes in the data."
        }

        val legacyValuesNotCovered = legacyFields.values.filter { !transformationRules.keys.contains(it) }
        require(legacyValuesNotCovered.isEmpty()) {
            "Legacy fields contain values that are not in the data: $legacyValuesNotCovered"
        }

        val legacyKeysInTransformationValues = legacyFields.keys.filter { transformationRules.values.contains(it) }
        require(legacyKeysInTransformationValues.isEmpty()) {
            "Legacy field keys should not be present as values in transformation rules: $legacyKeysInTransformationValues"
        }
    }

    /**
     * Gets all leaf node field names from a JSON node ignoring entries in arrays.
     * @param node The JSON node
     * @param currentPath The current path
     * @return A list of leaf node field names
     */
    fun getNonArrayLeafNodeFieldNames(
        node: JsonNode,
        currentPath: String,
    ): MutableList<String> {
        val leafNodeFieldNames = mutableListOf<String>()
        if (node.isValueNode) {
            if (!node.isNull) {
                leafNodeFieldNames.add(currentPath)
            }
        } else {
            // This does not handle arrays (they are skipped)
            node.fields().forEachRemaining { (fieldName, value) ->
                val newPath = if (currentPath.isEmpty()) fieldName else "$currentPath.$fieldName"
                leafNodeFieldNames.addAll(getNonArrayLeafNodeFieldNames(value, newPath))
            }
        }
        return leafNodeFieldNames
    }

    /**
     * Gets the string value of the JSON node identified by the (possibly) nested JSON path.
     * @param jsonNode The JSON node
     * @param jsonPath The JSON path identifying the value
     * @return The string representation of the value
     */
    fun mapJsonToLegacyCsvFields(
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
     * Maps a JSON node to a CSV.
     * @param jsonNode The JSON node
     * @param transformationRules The transformation rules
     * @return A map of CSV headers to values
     */
    fun mapJsonToCsv(
        jsonNode: JsonNode,
        transformationRules: Map<String, String>,
    ): Map<String, String> {
        val csvData = mutableMapOf<String, String>()
        transformationRules.forEach { (jsonPath, csvHeader) ->
            if (csvHeader.isEmpty()) return@forEach
            csvData[csvHeader] = mapJsonToLegacyCsvFields(jsonNode, jsonPath)
        }
        return csvData
    }

    /**
     * Maps a JSON node to a CSV.
     * @param jsonNode The JSON node
     * @param transformationRules The transformation rules
     * @return A map of CSV headers to values
     */
    fun mapJsonToLegacyCsvFields(
        jsonNode: JsonNode,
        transformationRules: Map<String, String>,
    ): Map<String, String> {
        val csvData = mutableMapOf<String, String>()
        transformationRules.forEach { (csvHeader, jsonPath) ->
            if (csvHeader.isEmpty()) return@forEach
            csvData[csvHeader] = mapJsonToLegacyCsvFields(jsonNode, jsonPath)
        }
        return csvData
    }

    /**
     * Converts the data class into a JSON object.
     * @param companyAssociatedData The company associated data
     * @return The JSON representation of the data
     */
    fun convertDataToJson(companyAssociatedData: CompanyAssociatedDataSfdrData): JsonNode {
        val objectMapper = jacksonObjectMapper().findAndRegisterModules()
        objectMapper.dateFormat = SimpleDateFormat("yyyy-MM-dd")
        val jsonData = objectMapper.writeValueAsString(companyAssociatedData.data)
        val data = ObjectMapper().readTree(jsonData)
        return data
    }
}
