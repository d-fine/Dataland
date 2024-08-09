package db.migration

import db.migration.utils.DataTableEntity
import db.migration.utils.getOrJavaNull
import db.migration.utils.migrateCompanyAssociatedDataOfDatatype
import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context
import org.json.JSONArray
import org.json.JSONObject
import java.nio.file.CopyOption

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
        "substantialContributionToClimateChangeAdaptationInPercentAligned",
        "substantialContributionToSustainableUseAndProtectionOfWaterAndMarineResourcesInPercentAligned",
        "substantialContributionToTransitionToACircularEconomyInPercentAligned",
        "substantialContributionToPollutionPreventionAndControlInPercentAligned",
        "substantialContributionToProtectionAndRestorationOfBiodiversityAndEcosystemsInPercentAligned",
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


    private fun replaceOldKeyWithNewOne(oldString: String, jsonObject: JSONObject, jsonCopy: JSONObject) {
        println("I want to see what gets into this function: " + oldString)


        val value = jsonObject.get(oldString)

        val indexOfOldString = oldFieldsToRename.indexOf(oldString)
        jsonCopy.put(newFieldsRenamed[indexOfOldString], value)


    }

    private fun migrateToAligned(jsonObject: JSONObject, it: String) {
        val value = jsonObject.getOrJavaNull(it)
        val valueAligned = jsonObject.getOrJavaNull("substantialContributionToClimateChangeMitigationInPercentAligned") as JSONObject? ?: JSONObject()
        valueAligned.put("substantialContributionToClimateChangeMitigationInPercentAligned",value)

        jsonObject.put("substantialContributionToClimateChangeMitigationInPercentAligned",valueAligned)
    }

    fun checkForRelevantFieldsInJsonObjectKeys(jsonObject: JSONObject) {

        val keysToBeRemoved: MutableList<String> = mutableListOf()
        val keyStringList = jsonObject.keys().iterator().asSequence().toList()
        val valueAligned = jsonObject.getOrJavaNull("zzzzzzzzzzzzzz") as JSONObject? ?: JSONObject()

        for(i in 0 until keyStringList.size) {

            if(oldFieldsToRename.contains(keyStringList[i])) {

                replaceOldKeyWithNewOne(keyStringList[i], jsonObject, valueAligned)

                // if the keyListString element is in the list of leemnts to be replaced ONLY THEN do something
//                println("=======================================ULTRA TEST")
//                val value = jsonObject.getOrJavaNull("substantialContributionToClimateChangeMitigationInPercent")
//                valueAligned.put("substantialContributionToClimateChangeMitigationInPercentAligned", value)

            }
            checkRecursivelyForBaseDataPointsInJsonObject(jsonObject, keyStringList[i])
        }

        if(valueAligned.length() > 0){
            if(valueAligned.length() ==6){
                println("==============================================================lets see it")
            }
        }

        println("What happens in the end?")

            /*
            .forEach {
            println("ALLE KEYS:" + it)

            if(it == "substantialContributionToClimateChangeMitigationInPercent"){
                println("!!!WATCH FROM HERE!!!")
                migrateToAligned(jsonObject, it)
            }

            checkRecursivelyForBaseDataPointsInJsonObject(jsonObject, it)
        }

             */
/*
            if (it == "substantialContributionToClimateChangeMitigationInPercent") {
                replaceOldKeyForNewKey(jsonObject,it)
            }

        }
         */
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