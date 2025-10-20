package org.dataland.datalandbackend.utils

/**
 * Object for mapping frameworks to human readable names
 */
object DataTypeNameMapper {
    private val mapping =
        mapOf(
            "sfdr" to "SFDR",
            "eutaxonomy-financials" to "EU Taxonomy Financials",
            "eutaxonomy-non-financials" to "EU Taxonomy Non-Financials",
            "nuclear-and-gas" to "EU Taxonomy Nuclear and Gas",
            "lksg" to "LkSG",
            "vsme" to "VSME",
        )

    /**
     * Fetches the name which is displayed in the download
     */
    fun getDisplayName(dataType: String): String? = mapping[dataType]
}
