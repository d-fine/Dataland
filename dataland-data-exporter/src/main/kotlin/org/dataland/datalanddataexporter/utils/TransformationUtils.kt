package org.dataland.datalanddataexporter.utils

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.dataland.datalandbackend.openApiClient.model.CompanyAssociatedDataSfdrData
import org.dataland.datalandbackend.openApiClient.model.CompanyInformation
import org.dataland.datalandbackendutils.utils.JsonUtils
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
    fun getCurrentHeaders(transformationRules: Map<String, String>): List<String> {
        val headers = mutableListOf<String>()
        transformationRules.forEach { (_, csvHeader) -> if (csvHeader.isNotEmpty()) headers.add(csvHeader) }
        require(headers.isNotEmpty()) { "No headers found in transformation rules." }
        headers.addAll(getCompanyRelatedHeaders())
        require(headers.distinct().size == headers.size) { "Duplicate headers found in transformation rules." }
        return headers
    }

    /**
     * Gets the headers from the legacy rules.
     * @param legacyRules The transformation rules
     * @return A list of headers
     */
    fun getLegacyHeaders(legacyRules: Map<String, String>): List<String> {
        val headers = mutableListOf<String>()
        legacyRules.forEach { (csvHeader, _) ->
            if (csvHeader.isNotEmpty()) {
                headers.add(csvHeader)
            }
        }
        require(headers.distinct().size == headers.size) { "Duplicate headers found in legacy rules." }
        return headers
    }

    /**
     * Gets the headers for company-related entries that are independent of the data transformation rules.
     * @return A list of headers
     */
    private fun getCompanyRelatedHeaders(): List<String> =
        listOf(COMPANY_ID_HEADER, COMPANY_NAME_HEADER, REPORTING_PERIOD_HEADER, LEI_HEADER)

    /**
     * Checks the consistency of the JSON data with the transformation rules.
     * @param node The JSON node
     * @param transformationRules The transformation rules
     */
    fun checkConsistencyOfDataAndTransformationRules(
        node: JsonNode,
        transformationRules: Map<String, String>,
    ) {
        val leafNodesInJsonNode: List<String> = JsonUtils.getNonArrayLeafNodeFieldNames(node)
        val filteredNodes = leafNodesInJsonNode.filter { !it.contains(NODE_FILTER) }
        require(transformationRules.keys.containsAll(filteredNodes)) {
            "Transformation rules do not cover all leaf nodes in the data."
        }
    }

    /**
     * Checks the consistency of the transformation rules and the legacy rules.
     * @param transformationRules The transformation rules
     * @param legacyRules The transformation rules
     *
     */
    fun checkConsistencyOfLegacyRulesAndTransformationRules(
        transformationRules: Map<String, String>,
        legacyRules: Map<String, String>,
    ) {
        val legacyValuesNotCovered = legacyRules.values.filter { !transformationRules.keys.contains(it) }
        require(legacyValuesNotCovered.isEmpty()) {
            "Legacy headers require nodes that are not in the data: $legacyValuesNotCovered"
        }

        val legacyKeysInTransformationValues = legacyRules.keys.filter { transformationRules.values.contains(it) }
        require(legacyKeysInTransformationValues.isEmpty()) {
            "Csv headers are not unique as legacy headers contain duplicates: $legacyKeysInTransformationValues"
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
            csvData[csvHeader] = JsonUtils.getValueFromJsonNodeByPath(jsonNode, jsonPath)
        }
        return csvData
    }

    /**
     * Maps a JSON node to a CSV.
     * @param jsonNode The JSON node
     * @param legacyRules The legacy rules
     * @return A map of CSV headers to values
     */
    fun mapJsonToLegacyCsv(
        jsonNode: JsonNode,
        legacyRules: Map<String, String>,
    ): Map<String, String> {
        val csvData = mutableMapOf<String, String>()
        legacyRules.forEach { (csvHeader, jsonPath) ->
            if (csvHeader.isEmpty()) return@forEach
            csvData[csvHeader] = JsonUtils.getValueFromJsonNodeByPath(jsonNode, jsonPath)
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
