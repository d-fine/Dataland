package org.dataland.datalanddataexporter.utils

import com.fasterxml.jackson.databind.JsonNode
import org.dataland.datalandbackend.openApiClient.model.CompanyInformation
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Properties

/**
 * A class containing utility methods for transforming data from JSON to CSV.
 */
object TransformationUtils {

    const val LEI_IDENTIFIER = "Lei"
    private const val ISIN_IDENTIFIER = "Isin"
    const val LEI_HEADER = "LEI"
    const val ISIN_HEADER = "ISIN"
    const val COMPANY_ID_HEADER = "Company ID"
    const val COMPANY_NAME_HEADER = "Company Name"
    const val REPORTING_PERIOD_HEADER = "Reporting Period"

    /**
     * Method to get the current timestamp in the format yyyyMMdd
     * @return the current timestamp
     */
    fun getTimestamp(): String {
        val currentDate = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("yyyyMMdd")
        return currentDate.format(formatter)
    }

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
     * Reads a transformation configuration file and returns a map of JSON paths to CSV headers.
     * @param fileName The name of the transformation configuration file
     * @return A map of JSON paths to CSV headers
     */
    fun readTransformationConfig(fileName: String): Map<String, String> {
        val props = Properties()
        props.load(this.javaClass.classLoader.getResourceAsStream(fileName))
        return props
            .map { (jsonPath, csvHeader) -> jsonPath.toString() to csvHeader.toString() }
            .toMap()
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
    private fun getCompanyRelatedHeaders(): List<String> {
        return listOf(COMPANY_ID_HEADER, COMPANY_NAME_HEADER, REPORTING_PERIOD_HEADER, LEI_HEADER)
    }


    /**
     * Checks the consistency of the transformation rules with the JSON data.
     * @param node The JSON node
     * @param transformationRules The transformation rules
     */
    fun checkConsistency(node: JsonNode, transformationRules: Map<String, String>) {
        val leafNodesInJsonNode = getNonArrayLeafNodeFieldNames(node, "")
        require(transformationRules.keys.containsAll(leafNodesInJsonNode)) {
            "Transformation rules do not cover all leaf nodes in the data."
        }
    }

    /**
     * Gets all leaf node field names from a JSON node ignoring entries in arrays.
     * @param node The JSON node
     * @param currentPath The current path
     * @return A list of leaf node field names
     */
    fun getNonArrayLeafNodeFieldNames(node: JsonNode, currentPath: String): MutableList<String> {
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
}
