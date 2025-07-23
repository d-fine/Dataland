package org.dataland.datalandbackend.services

import org.dataland.datalandbackend.model.enums.eutaxonomy.nonfinancials.Activity
import org.dataland.datalandbackendutils.utils.JsonUtils
import org.springframework.stereotype.Component

@Component
class OrderingUtils {
    companion object {
        internal val STATIC_ALIASES =
            mapOf(
                "companyName" to "COMPANY_NAME",
                "companyLei" to "COMPANY_LEI",
                "reportingPeriod" to "REPORTING_PERIOD",
            )
        internal const val DATA = "data"
        internal const val VALUE = "value"
        internal const val ACTIVITIES_STRING = "Activities"
        internal const val ACTIVITIES_PATTERN = "$ACTIVITIES_STRING.$VALUE.0."

        /**
         * Creates the CSV schema based on the provided headers
         * The first parameter determines which fields are used to create columns; the second parameter determines the
         * order of the columns.
         * @param orderedHeaderFields a set of column names used as the headers in the CSV in the correct order
         * @return the csv schema builder
         */
        fun getOrderedHeaders(
            usedHeaderFields: Set<String>,
            orderedHeaderFields: Collection<String>,
            isAssembledDataset: Boolean,
        ): List<String> {
            require(usedHeaderFields.isNotEmpty()) { "After filtering, CSV data is empty." }
            val resultList = mutableListOf<String>()

            if (isAssembledDataset) {
                STATIC_ALIASES.keys.forEach { staticFieldName ->
                    usedHeaderFields
                        .filter { usedHeaderField ->
                            usedHeaderField == staticFieldName
                        }.forEach { resultList.add(it) }
                }

                orderedHeaderFields.forEach { orderedHeaderFieldsEntry ->
                    usedHeaderFields
                        .filter { usedHeaderField ->
                            usedHeaderField.startsWith(DATA + JsonUtils.getPathSeparator() + orderedHeaderFieldsEntry)
                        }.forEach {
                            resultList.add(it)
                        }
                }
            } else {
                orderedHeaderFields.forEach {
                    resultList.add(it)
                }
            }
            return resultList
        }

        /**
         * Makes sure that all possible json paths are included in the ordered headers. This includes in particular the arrays
         * for the eu taxonomy non financials framework
         * @param orderedHeaderFields a set of column names used as the headers in the CSV derived by the schemata
         * @return an expanded version of the ordered headers that now also contains the headers for repeating fields in an array
         */
        fun expandOrderedHeadersForEuTaxonomyActivities(orderedHeaderFields: List<String>): List<String> {
            val expandedOrderedHeaders = mutableListOf<String>()
            val arrayFields = mutableListOf<String>()
            // Iterate through input strings
            for ((index, input) in orderedHeaderFields.withIndex()) {
                if (input.contains(ACTIVITIES_PATTERN)) {
                    arrayFields.add(input) // collect all properties of the aligned activities
                } else {
                    // Add unmodified strings directly to the output list
                    expandedOrderedHeaders.add(input)
                }
                // expand the output so that it contains all possible json paths for the activities array
                if (input.contains(ACTIVITIES_PATTERN) && !orderedHeaderFields[index + 1].contains(ACTIVITIES_PATTERN)) {
                    addAllArrayFieldsToOutput(arrayFields, expandedOrderedHeaders)
                    arrayFields.clear()
                }
            }
            return expandedOrderedHeaders
        }

        /**
         * Creates new entries in the ordered header in case the activities arrays are long
         * @param arrayFields all attribute fields for one entry in an activities array
         * @param outputList the list of ordered header fields that is appended with extra entries in case there are more activities
         */
        fun addAllArrayFieldsToOutput(
            arrayFields: MutableList<String>,
            outputList: MutableList<String>,
        ) {
            for (activityIndex in 0..Activity.entries.size) {
                for (t in arrayFields) {
                    outputList.add(t.replace(ACTIVITIES_PATTERN, "$ACTIVITIES_STRING.$VALUE.$activityIndex."))
                }
            }
        }
    }
}
