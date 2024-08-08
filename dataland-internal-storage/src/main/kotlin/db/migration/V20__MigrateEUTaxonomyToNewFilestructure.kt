package db.migration

import db.migration.utils.DataTableEntity
import db.migration.utils.getOrJavaNull
import db.migration.utils.migrateCompanyAssociatedDataOfDatatype
import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context
import org.json.JSONArray
import org.json.JSONObject

/**
 * This migration script updates existing EUTaxonomyNonFinancial Files to the new
 * EUTaxonomyNonFinancials structure
 */


class V20__MigrateEUTaxonomyToNewFilestructure : BaseJavaMigration() {

    private val oldFieldsToRename = listOf(
        // check if this has to be written all together
        "substantialContributionToClimateChangeMitigationInPercent",
        "substantialContributionToClimateChangeAdaptationInPercent",
        "substantialContributionToSustainableUseAndProtectionOfWaterAndMarineResourcesInPercent",
        "substantialContributionToTransitionToACircularEconomyInPercent",
        "substantialContributionToPollutionPreventionAndControlInPercent",
        "substantialContributionToProtectionAndRestorationOfBiodiversityAndEcosystemsInPercent"
        )

    private val newFieldsRenamed = listOf(
        "substantialContributionToClimateChangeMitigationInPercentAligned",
        "Substantial Contribution to Climate Change Adaptation In Percent - Aligned",
        "Substantial Contribution to Sustainable Use and Protection of Water and Marine Resources In Percent - Aligned",
        "Substantial Contribution to Transition to a Circular Economy In Percent - Aligned",
        "Substantial Contribution to Pollution Prevention and Control In Percent - Aligned",
        "Substantial Contribution to Protection and Restoration of Biodiversity and Ecosystems In Percent - Aligned",
    )

    private val newFieldAddition = listOf(
        "Substantial Contribution to Climate Change Mitigation In Percent - Eligible",
        "Substantial Contribution to Climate Change Mitigation In Percent - Of which use of proceeds",
        "Substantial Contribution to Climate Change Mitigation In Percent - Enabling Share",
        "Substantial Contribution to Climate Change Mitigation In Percent - Transitional Share",
        "Substantial Contribution to Climate Change Adaptation In Percent - Eligible",
        "Substantial Contribution to Climate Change Adaptation In Percent - Of which use of proceeds",
        "Substantial Contribution to Climate Change Adaptation In Percent - Enabling Share",
        "Substantial Contribution to Sustainable Use and Protection of Water and Marine Resources In Percent - Eligible",
        "Substantial Contribution to Sustainable Use and Protection of Water and Marine Resources In Percent - Of which use of proceeds",
        "Substantial Contribution to Sustainable Use and Protection of Water and Marine Resources In Percent - Enabling Share",
        "Substantial Contribution to Transition to a Circular Economy In Percent - Eligible",
        "Substantial Contribution to Transition to a Circular Economy In Percent - Of which use of proceeds",
        "Substantial Contribution to Transition to a Circular Economy In Percent - Enabling Share",
        "Substantial Contribution to Pollution Prevention and Control In Percent - Eligible",
        "Substantial Contribution to Pollution Prevention and Control In Percent - Of which use of proceeds",
        "Substantial Contribution to Pollution Prevention and Control In Percent - Enabling Share",
        "Substantial Contribution to Protection and Restoration of Biodiversity and Ecosystems In Percent - Eligible",
        "Substantial Contribution to Protection and Restoration of Biodiversity and Ecosystems In Percent - Of which use of proceeds",
        "Substantial Contribution to Protection and Restoration of Biodiversity and Ecosystems In Percent - Enabling Share"
    )

    /**
     * find elements of the list in general
     * change the name to the '-alligned' one
     * if found add new elements and let them stay empty
     *
     * for the final green block just rename
     */

    fun checkForRelevantFieldsInJsonObjectKeys(jsonObject: JSONObject) {

        var keylist = jsonObject.keys()
        keylist.forEach {
            println("ALLE KEYS:" + it)

            if (it == "substantialContributionToClimateChangeMitigationInPercent") {
                // funktion welche diesen Eintrag durch den neuen ersetzt
            }
            checkRecursivelyForBaseDataPointsInJsonObject(jsonObject, it)
        }
    }

    private fun checkRecursivelyForBaseDataPointsInJsonArray(jsonArray: JSONArray) {
        for (i in 0 until jsonArray.length()) {
            val element = jsonArray[i]
            if (element != null && element is JSONObject) {
                checkForRelevantFieldsInJsonObjectKeys(element)
            }
        }
    }

    fun checkRecursivelyForBaseDataPointsInJsonObject(jsonObject: JSONObject, key: String){
        val obj = jsonObject.getOrJavaNull(key)
        if (obj != null && obj is JSONObject) {
            checkForRelevantFieldsInJsonObjectKeys(obj)
        } else if (obj != null && obj is JSONArray) {
            checkRecursivelyForBaseDataPointsInJsonArray(obj)
        }
    }

    fun migrateEutaxonomyNonFinancialsData(dataTableEntity: DataTableEntity) {
        val jsonObject = dataTableEntity.dataJsonObject
        jsonObject.keys().forEach{
            println("TOP KEYS:" + it)
            checkRecursivelyForBaseDataPointsInJsonObject(jsonObject, it)
        }
    }


    override fun migrate(context: Context?) {
        migrateCompanyAssociatedDataOfDatatype(
            context = context,
            dataType = "eutaxonomy-non-financials",
            migrate = this::migrateEutaxonomyNonFinancialsData,
        )
    }
}