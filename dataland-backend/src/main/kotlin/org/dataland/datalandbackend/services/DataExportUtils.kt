package org.dataland.datalandbackend.services

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.dataformat.csv.CsvSchema
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.utils.DataPointUtils
import org.dataland.datalandbackend.utils.NonFinancialsMapping
import org.dataland.datalandbackend.utils.NuclearAndGasMapping
import org.dataland.datalandbackend.utils.ReferencedReportsUtilities
import org.dataland.datalandbackendutils.utils.JsonUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import kotlin.collections.map

/**
 * The class holds methods which are used in the data export. Mainly it contains functions to map the
 * correct export aliases to the data that is to be exported. Export aliases for assembled datasets are provided
 * by the specification service, aliases for other datasets are hardcoded.
 *  @param dataPointUtils
 *  @param referencedReportsUtilities
 */
@Component
class DataExportUtils
    @Autowired
    constructor(
        private val dataPointUtils: DataPointUtils,
        private val referencedReportsUtilities: ReferencedReportsUtilities,
    ) {
        companion object {
            private const val PRIORITY_COMPANY_NAME = -3
            private const val PRIORITY_COMPANY_LEI = -2
            private const val PRIORITY_REPORTING_PERIOD = -1
            private const val PRIORITY_DEFAULT = 0
            private val STATIC_ALIASES =
                mapOf(
                    "companyName" to "COMPANY_NAME",
                    "companyLei" to "COMPANY_LEI",
                    "reportingPeriod" to "REPORTING_PERIOD",
                )
            private val SUFFIX = ".value"
        }

        private val objectMapper = JsonUtils.defaultObjectMapper

        /**
         * Return a list of column names for the file export that is ordered according to the framework specification
         *
         * @param csvDataWithReadableHeaders the data to be exported with readable column names
         * @param csvSchema the csvSchema containing the columns in the correct order
         *
         */
        fun findOrderedColumnNamesForNonEmptyCols(
            csvDataWithReadableHeaders: List<Map<String, String>>,
            csvSchema: CsvSchema,
        ): List<String?> {
            val columnsWithValues = mutableSetOf<String>()
            csvDataWithReadableHeaders.forEach { dataMap ->
                dataMap.forEach { (key, value) ->
                    if (value.isNotEmpty()) {
                        columnsWithValues.add(key)
                    }
                }
            }
            return csvSchema.columnNames.filter { it in columnsWithValues }
        }

        /**
         * Return the template of an assembled framework or null if the passed name refers to an old style framework
         *
         * @param framework the framework for which the template shall be returned
         */
        private fun getFrameworkTemplate(framework: String): JsonNode? {
            return dataPointUtils.getFrameworkSpecificationOrNull(framework)?.let {
                val frameworkTemplate = objectMapper.readTree(it.schema)
                referencedReportsUtilities.insertReferencedReportsIntoFrameworkSchema(
                    frameworkTemplate,
                    it.referencedReportJsonPath,
                )
                return frameworkTemplate
            }
        }

        /**
         * A data class for export data
         *
         * @param csvData the data to be exported with the original json path headers
         * @param csvSchema
         * @param readableHeaders a map json path name to export alias
         */
        data class PreparedExportData(
            val csvData: List<Map<String, String>>,
            val csvSchema: CsvSchema,
            val readableHeaders: Map<String, String>,
        )

        /**
         * Prepares the data structure for export formats (CSV and Excel)
         * @param portfolioExportRows passed JSON objects to be exported
         * @param dataType the datatype specifying the framework
         * @param keepValueFieldsOnly if true, non value fields are stripped
         * @param includeAliases if true, human-readable names are used if available
         * @return PreparedExportData containing:
         *   - the CSV data as a list of maps
         *   - the CSV schema
         *   - header fields with human-readable names
         */
        fun prepareExportData(
            portfolioExportRows: List<JsonNode>,
            dataType: DataType,
            keepValueFieldsOnly: Boolean,
            includeAliases: Boolean = true,
        ): PreparedExportData {
            val frameworkTemplate = getFrameworkTemplate(dataType.toString())
            val isAssembledDataset = (frameworkTemplate != null)

            val (csvData, nonEmptyHeaderFields) = getCsvDataAndNonEmptyFields(portfolioExportRows, keepValueFieldsOnly)

            val aliasExportMap =
                if (isAssembledDataset) {
                    extractAliasExportFields(frameworkTemplate)
                } else {
                    emptyMap()
                }

            val readableHeaders = mutableMapOf<String, String>()

            nonEmptyHeaderFields.forEach { fieldName ->
                val strippedField = fieldName.removePrefix("data.").removeSuffix(SUFFIX)
                val isStaticAlias = STATIC_ALIASES.containsKey(strippedField)

                val aliasHeader = STATIC_ALIASES[strippedField] ?: stripFieldNames(fieldName, aliasExportMap)
                val headerKey = if (isStaticAlias) strippedField else "data.$strippedField"

                if (includeAliases) {
                    readableHeaders[headerKey] = aliasHeader
                } else {
                    readableHeaders[headerKey] = fieldName
                }
            }

            val orderedHeaderFields =
                if (isAssembledDataset) {
                    JsonUtils
                        .getLeafNodeFieldNames(
                            frameworkTemplate,
                            keepEmptyFields = true,
                            dropLastFieldName = true,
                        ).mapNotNull {
                            readableHeaders[it] ?: readableHeaders["data.$it"]
                        }
                } else {
                    LinkedHashSet(
                        nonEmptyHeaderFields
                            .sortedWith(
                                compareBy<String> {
                                    when {
                                        it.startsWith("companyName") -> PRIORITY_COMPANY_NAME
                                        it.startsWith("companyLei") -> PRIORITY_COMPANY_LEI
                                        it.startsWith("reportingPeriod") -> PRIORITY_REPORTING_PERIOD
                                        else -> PRIORITY_DEFAULT
                                    }
                                }.then(naturalOrder()),
                            ).map {
                                val strippedField = it.removePrefix("data.").removeSuffix(SUFFIX)
                                val isStaticAlias = STATIC_ALIASES.containsKey(strippedField)
                                val headerKey = if (isStaticAlias) strippedField else "data.$strippedField"
                                readableHeaders[headerKey] ?: headerKey
                            },
                    )
                }

            val csvSchema = createCsvSchemaBuilder(readableHeaders.values.toSet(), orderedHeaderFields, isAssembledDataset)

            return PreparedExportData(csvData, csvSchema, readableHeaders)
        }

        /**
         * Replaces the old header names (json paths) with human-readable header names
         * @param csvData the data to be exported with the original json path headers
         * @param readableHeaders a map json path -> human-readable name
         * @return The provided csv data with the himan-readable names
         */
        fun mapReadableHeadersToCsvData(
            csvData: List<Map<String, String>>,
            readableHeaders: Map<String, String>,
        ): List<Map<String, String>> =
            csvData.map { originalMap ->
                originalMap.mapKeys { (key, _) ->
                    readableHeaders[key.removeSuffix(SUFFIX)] ?: key
                }
            }

        /**
         * Return true if the field contains REFERENCED_REPORTS_ID
         */
        private fun isReferencedReportsField(field: String): Boolean =
            field.contains(JsonUtils.getPathSeparator() + ReferencedReportsUtilities.REFERENCED_REPORTS_ID + JsonUtils.getPathSeparator())

        /**
         * Return true if the provided field name (full path) specifies a meta data field.
         */
        private fun isMetaDataField(field: String): Boolean {
            val separator = JsonUtils.getPathSeparator()
            return field.endsWith(separator + "comment") ||
                field.endsWith(separator + "quality") ||
                field.contains(separator + "dataSource" + separator) ||
                isReferencedReportsField(field)
        }

        /**
         * Parse a list of JSON nodes into a list of (fieldName --> fieldValue)-mappings
         * @param nodes the list of nodes to process
         * @param keepValueFieldsOnly whether meta-information fields should be dropped or kept
         * @return a pair of lists containing (fieldName --> fieldValue)-mappings and a set of all used field names
         */
        fun getCsvDataAndNonEmptyFields(
            nodes: List<JsonNode>,
            keepValueFieldsOnly: Boolean,
        ): Pair<List<Map<String, String>>, Set<String>> {
            val csvData =
                nodes.map { node ->
                    val nonEmptyNodes =
                        JsonUtils
                            .getNonEmptyLeafNodesAsMapping(node)
                            .filterKeys { !isReferencedReportsField(it) }
                            .toMutableMap()

                    if (keepValueFieldsOnly) {
                        processQualityFields(nonEmptyNodes)
                    } else {
                        nonEmptyNodes
                    }
                }
            val nonEmptyFields = csvData.map { it.keys }.fold(emptySet<String>()) { acc, next -> acc.plus(next) }

            csvData.forEach { dataSet ->
                nonEmptyFields.forEach { headerField ->
                    dataSet.getOrPut(headerField) { "" }
                }
            }

            return Pair(csvData, nonEmptyFields)
        }

        /**
         * Process a map of nodes to keep value fields and convert quality fields to value fields
         * when there is no corresponding value.
         * @param nodes The map of nodes to process
         * @return A filtered map containing only value fields (including converted quality fields)
         */
        private fun processQualityFields(nodes: MutableMap<String, String>): MutableMap<String, String> {
            val filteredNodes = mutableMapOf<String, String>()
            val separator = JsonUtils.getPathSeparator()

            val keys = nodes.keys.toList()
            for (field in keys) {
                val value = nodes[field] ?: continue

                when {
                    field.endsWith("${separator}quality") -> {
                        val basePath = field.removeSuffix("${separator}quality")
                        val valuePath = "${basePath}${separator}value"
                        if (valuePath !in nodes) {
                            filteredNodes[valuePath] = value
                        }
                    }
                    field.endsWith("${separator}value") -> {
                        filteredNodes[field] = value
                    }
                    !isMetaDataField(field) -> {
                        filteredNodes[field] = value
                    }
                }
            }

            nodes.clear()
            nodes.putAll(filteredNodes)
            return nodes
        }

        /**
         * Creates the CSV schema based on the provided headers
         * The first parameter determines which fields are used to create columns; the second parameter determines the
         * order of the columns.
         * @param orderedHeaderFields a set of column names used as the headers in the CSV in the correct order
         * @return the csv schema builder
         */
        private fun createCsvSchemaBuilder(
            usedHeaderFields: Set<String>,
            orderedHeaderFields: Collection<String?>,
            isAssembledDataset: Boolean,
        ): CsvSchema {
            require(usedHeaderFields.isNotEmpty()) { "After filtering, CSV data is empty." }

            val csvSchemaBuilder = CsvSchema.builder()

            if (isAssembledDataset) {
                usedHeaderFields
                    .filter {
                        !orderedHeaderFields.contains(it)
                    }.forEach { csvSchemaBuilder.addColumn(it) }

                orderedHeaderFields.forEach { header ->
                    csvSchemaBuilder.addColumn(header)
                }
            } else {
                orderedHeaderFields.forEach {
                    csvSchemaBuilder.addColumn(it)
                }
            }
            return csvSchemaBuilder.build().withHeader()
        }

        /**
         * Returns a map json-path -> export alias. Is called recursively
         * @param schema a json node containing the export aliases
         * @param prefix a part of the json path
         */
        private fun extractAliasExportFields(
            schema: JsonNode,
            prefix: String = "",
        ): Map<String, String?> {
            val separator = JsonUtils.getPathSeparator()
            val result = mutableMapOf<String, String?>()

            val fields = schema.fieldNames()
            while (fields.hasNext()) {
                val field = fields.next()
                val node = schema.get(field)
                val fullPath = if (prefix.isEmpty()) field else "$prefix$separator$field"

                when {
                    node.has("aliasExport") -> {
                        val aliasExport = node.get("aliasExport")?.asText()
                        result[fullPath] = if (aliasExport != "null") aliasExport else null
                    }
                    node.isObject -> {
                        result.putAll(extractAliasExportFields(node, fullPath))
                    }
                }
            }
            return result
        }

        /**
         * Transforms a full field path like "data.revenue.nonAlignedActivities.value.0.share.absoluteShare.amount"
         * into an alias format like "REV_NON_ALIGNED_ACTIVITIES_0_ABS", using aliasExportMap for the prefix.
         */
        private fun stripFieldNames(
            fullFieldName: String,
            aliasExportMap: Map<String, String?>,
        ): String {
            val coreField = fullFieldName.removePrefix("data.").removeSuffix(SUFFIX)

            val parts = coreField.split(".")

            var aliasPrefix: String? = null
            var matchedKey: String? = null

            for (i in parts.size downTo 1) {
                val prefixCandidate = parts.subList(0, i).joinToString(".")
                if (aliasExportMap.containsKey(prefixCandidate)) {
                    aliasPrefix = aliasExportMap[prefixCandidate]
                    matchedKey = prefixCandidate
                    break
                }
            }

            if (aliasPrefix == null) return fullFieldName

            val suffixParts =
                if (matchedKey!!.length < coreField.length) {
                    coreField.removePrefix("$matchedKey.").split(".")
                } else {
                    emptyList()
                }

            val transformedSuffix =
                suffixParts.mapNotNull { part ->
                    when (part) {
                        "share", "amount", "value" -> null
                        "absoluteShare" -> "ABS"
                        else ->
                            NonFinancialsMapping.aliasMap[part]
                                ?: NuclearAndGasMapping.aliasMap[part]
                                ?: part.uppercase()
                    }
                }

            return (listOf(aliasPrefix) + transformedSuffix).joinToString("_")
        }
    }
