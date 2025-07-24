package org.dataland.datalandbackend.services

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.NullNode
import com.fasterxml.jackson.dataformat.csv.CsvSchema
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.services.DataExportOrderingUtils.Companion.expandOrderedHeadersForEuTaxonomyActivities
import org.dataland.datalandbackend.services.DataExportOrderingUtils.Companion.getOrderedHeaders
import org.dataland.datalandbackend.utils.DataPointUtils
import org.dataland.datalandbackend.utils.NonFinancialsMapping
import org.dataland.datalandbackend.utils.NuclearAndGasMapping
import org.dataland.datalandbackend.utils.ReferencedReportsUtilities
import org.dataland.datalandbackend.utils.SfdrMapping
import org.dataland.datalandbackendutils.utils.JsonUtils
import org.dataland.specificationservice.openApiClient.api.SpecificationControllerApi
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import kotlin.collections.map
import kotlin.text.contains

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
        @Autowired
        lateinit var specificationApi: SpecificationControllerApi

        private fun getResolvedSchemaNode(framework: String): JsonNode? {
            val resolvedSchemaDto = specificationApi.getResolvedFrameworkSpecification(framework)
            return objectMapper.valueToTree(resolvedSchemaDto.resolvedSchema)
        }

        companion object {
            val STATIC_ALIASES =
                mapOf(
                    "companyName" to "COMPANY_NAME",
                    "companyLei" to "COMPANY_LEI",
                    "reportingPeriod" to "REPORTING_PERIOD",
                )
            const val DATA = "data"
            const val VALUE = "value"
            private const val ALIAS_EXPORT = "aliasExport"
            private const val QUALITY = "quality"
            private const val COMMENT = "comment"
            private const val DATA_SOURCE = "dataSource"
            private const val PREFIX = "$DATA."
            private const val SUFFIX = ".$VALUE"
            private const val FIRST_PLACE = -3
            private const val SECOND_PLACE = -2
            private const val THIRD_PLACE = -1
        }

        private val objectMapper = JsonUtils.defaultObjectMapper

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
         * A data class containing the export data as a list of maps and the csv schema definition.
         * Each entry in csvData represents one row in the corresponding excel or csv file.
         *
         * @param csvData: List<Map<String, String?>>
         *     The data to be exported
         *     Each entry in the list represents a column in the excel or csv file that is to be exported
         * @param csvSchema: CsvSchema: This contains all allowed column names for the excel or csv file to be exported
         *     in the correct order
         */
        data class PreparedExportData(
            val csvData: List<Map<String, String?>>,
            val csvSchema: CsvSchema,
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
            includeAliases: Boolean,
        ): PreparedExportData {
            val frameworkTemplate = getFrameworkTemplate(dataType.toString())
            val isAssembledDatasetParam = (frameworkTemplate != null)

            val (csvData, nonEmptyHeaderFields) = getCsvDataAndNonEmptyFields(portfolioExportRows, keepValueFieldsOnly)

            val orderedHeaderFields =
                if (isAssembledDatasetParam) {
                    val resolvedSchemaNode = getResolvedSchemaNode(dataType.toString())
                    JsonUtils.getLeafNodeFieldNames(
                        resolvedSchemaNode ?: NullNode.instance,
                        keepEmptyFields = true,
                        dropLastFieldName = false,
                    )
                } else {
                    LinkedHashSet(
                        nonEmptyHeaderFields.sortedWith(
                            compareBy<String> {
                                when {
                                    it.startsWith("companyName") -> FIRST_PLACE
                                    it.startsWith("companyLei") -> SECOND_PLACE
                                    it.startsWith("reportingPeriod") -> THIRD_PLACE
                                    else -> 0
                                }
                            }.then(naturalOrder()),
                        ),
                    )
                }
            val expandedOrderedHeaders = expandOrderedHeadersForEuTaxonomyActivities(orderedHeaderFields.toList())
            val orderedHeaders = getOrderedHeaders(nonEmptyHeaderFields, expandedOrderedHeaders, isAssembledDatasetParam)
            val readableHeaders =
                if (includeAliases) {
                    applyAliasRenaming(orderedHeaders, frameworkTemplate ?: portfolioExportRows.first())
                } else {
                    orderedHeaders.associateWith { it }
                }

            val mappedCsvData = mapReadableHeadersToCsvData(csvData, readableHeaders)

            val usedReadableHeaders =
                mappedCsvData
                    .flatMap { it.entries }
                    .filterNot { it.value.isNullOrBlank() }
                    .map { it.key }
                    .toSet()

            val filteredReadableHeaders = readableHeaders.filterValues { it in usedReadableHeaders }

            val csvSchema =
                createCsvSchemaBuilder(
                    filteredReadableHeaders.values.toSet(),
                    orderedHeaders.mapNotNull { filteredReadableHeaders[it] },
                    isAssembledDatasetParam,
                )

            return PreparedExportData(mappedCsvData, csvSchema)
        }

        /**
         * Applies alias renaming to a list of field headers based on a framework template.
         *
         * @param orderedHeaders list of original field header names in the order they appear.
         * @param frameworkTemplate JSON node containing alias definitions used for renaming.
         */
        fun applyAliasRenaming(
            orderedHeaders: List<String>,
            frameworkTemplate: JsonNode,
        ): Map<String, String> {
            val aliasExportMap = extractAliasExportFields(frameworkTemplate)
            val aliasHeaders = mutableMapOf<String, String>()

            orderedHeaders.forEach { field ->
                val staticAlias = STATIC_ALIASES[field]
                val alias = staticAlias ?: stripFieldNames(field, aliasExportMap)
                aliasHeaders[field] = alias
            }

            return aliasHeaders
        }

        /**
         * Replaces the old header names (json paths) with human-readable header names
         * @param csvData the data to be exported with the original json path headers
         * @param readableHeaders a map json path -> human-readable name
         * @return The provided csv data with the hman-readable names
         */
        fun mapReadableHeadersToCsvData(
            csvData: List<Map<String, String?>>,
            readableHeaders: Map<String, String>,
        ): List<Map<String, String?>> =
            csvData.map { row ->
                row
                    .mapNotNull { (originalKey, value) ->
                        val mappedKey =
                            readableHeaders[originalKey]
                                ?: readableHeaders.entries.firstOrNull { (jsonPath, _) -> originalKey.endsWith(jsonPath) }?.value
                        mappedKey?.let { it to value }
                    }.toMap()
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
            return field.endsWith(separator + COMMENT) ||
                field.endsWith(separator + QUALITY) ||
                field.contains(separator + DATA_SOURCE + separator) ||
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
        ): Pair<List<Map<String, String?>>, Set<String>> {
            val csvData =
                nodes.map { node ->
                    val nonEmptyNodes =
                        JsonUtils
                            .getAllLeafNodesAsMapping(node)
                            .filterKeys { !isReferencedReportsField(it) }
                            .toMutableMap()

                    if (keepValueFieldsOnly) {
                        processQualityFields(nonEmptyNodes)
                    } else {
                        nonEmptyNodes
                    }
                }

            val nonEmptyFields =
                csvData
                    .flatMap { it.entries }
                    .filterNot { it.value.isNullOrBlank() }
                    .map { it.key }
                    .toSet()

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
        private fun processQualityFields(nodes: MutableMap<String, String?>): MutableMap<String, String?> {
            val filteredNodes = mutableMapOf<String, String>()
            val separator = JsonUtils.getPathSeparator()

            val keys = nodes.keys.toList()
            for (field in keys) {
                val value = nodes[field] ?: continue

                when {
                    field.endsWith("${separator}$QUALITY") -> {
                        val basePath = field.removeSuffix("${separator}$QUALITY")
                        val valuePath = "${basePath}${separator}$VALUE"
                        if (valuePath in nodes && nodes[valuePath].isNullOrEmpty()) {
                            filteredNodes[valuePath] = value
                        }
                    }

                    field.endsWith("${separator}$VALUE") -> {
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
                    node.has(ALIAS_EXPORT) -> {
                        val aliasExport = node.get(ALIAS_EXPORT)?.asText()
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
        fun stripFieldNames(
            fullFieldName: String,
            aliasExportMap: Map<String, String?>,
        ): String {
            val coreField = fullFieldName.removePrefix(PREFIX).removeSuffix(SUFFIX)

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
                        "share", "amount", VALUE -> null
                        "absoluteShare" -> "ABS"
                        else ->
                            NonFinancialsMapping.aliasMap[part]
                                ?: NuclearAndGasMapping.aliasMap[part]
                                ?: SfdrMapping.aliasMap[part]
                                ?: part.uppercase()
                    }
                }

            return (listOf(aliasPrefix) + transformedSuffix).joinToString("_")
        }
    }
