package org.dataland.datalandbackend.utils

object DataTypeNameMapper {
    private val mapping =
        mapOf(
            "sfdr" to "SFDR",
            "eutaxonomy-financials" to "EU Taxonomy Financials",
            "eutaxonomy-non-financials" to "EU Taxonomy Non-Financials",
            "nuclear-and-gas" to "EU Taxonomy Non-Financials Nuclear and Gas",
            "p2p" to "WWF Pathways to Paris",
            "lksg" to "Lksg",
            "additional-company-information" to "Additional Company Information",
            "vsme" to "Vsme",
            "esg-datenkatalog" to "Esg Datenkatalog für Großunternehmen",
            "heimathafen" to "Heimathafen",
        )

    fun getDisplayName(dataType: String): String? = mapping[dataType]
}
