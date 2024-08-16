package db.migration

import db.migration.utils.DataTableEntity
import db.migration.utils.migrateCompanyAssociatedDataOfDatatype
import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context
import org.json.JSONObject

/**
 * This migration script updates all sfdr datasets to match the new sfdr data model.
 */
class V22__IntroduceNewFieldsInSfdr : BaseJavaMigration() {

    override fun migrate(context: Context?) {
        migrateCompanyAssociatedDataOfDatatype(
            context,
            "sfdr",
            this::migrateSfdr,
        )
    }

    private fun migrateSfdr(dataTableEntity: DataTableEntity) {
        val dataset = dataTableEntity.dataJsonObject

        migrateSocialAndEmployeeMatters(dataset)
        migrateGreenhouseGasEmissions(dataset)
    }

    private fun migrateGreenhouseGasEmissions(dataset: JSONObject) {
        val environmentalObject = dataset.getJSONObject("environmental")
        val greenhouseGasEmissionsObject = environmentalObject.getJSONObject("greenhouseGasEmissions")

        greenhouseGasEmissionsObject.put("scope3UpstreamGhgEmissionsInTonnes", JSONObject.NULL)
        greenhouseGasEmissionsObject.put("scope3DownstreamGhgEmissionsInTonnes", JSONObject.NULL)
        greenhouseGasEmissionsObject.put("ghgIntensityScope1InTonnesPerMillionEURRevenue", JSONObject.NULL)
        greenhouseGasEmissionsObject.put("ghgIntensityScope2InTonnesPerMillionEURRevenue", JSONObject.NULL)
        greenhouseGasEmissionsObject.put("ghgIntensityScope3InTonnesPerMillionEURRevenue", JSONObject.NULL)
        greenhouseGasEmissionsObject.put("ghgIntensityScope4InTonnesPerMillionEURRevenue", JSONObject.NULL)
    }

    private fun migrateSocialAndEmployeeMatters(dataset: JSONObject) {
        val socialObject = dataset.getJSONObject("social")
        val socialAndEmployeeMattersObject = socialObject.getJSONObject("socialAndEmployeeMatters")

        val femaleBoardMembers = socialAndEmployeeMattersObject.remove("femaleBoardMembers")
        val maleBoardMembers = socialAndEmployeeMattersObject.remove("maleBoardMembers")
        val boardGenderDiversity = socialAndEmployeeMattersObject.remove("boardGenderDiversityInPercent")

        socialAndEmployeeMattersObject.put("femaleBoardMembersSupervisoryBoard", femaleBoardMembers)
        socialAndEmployeeMattersObject.put("femaleBoardMembersBoardOfDirectors", JSONObject.NULL)
        socialAndEmployeeMattersObject.put("maleBoardMembersSupervisoryBoard", maleBoardMembers)
        socialAndEmployeeMattersObject.put("maleBoardMembersBoardOfDirectors", JSONObject.NULL)
        socialAndEmployeeMattersObject.put("boardGenderDiversitySupervisoryBoardInPercent", boardGenderDiversity)
        socialAndEmployeeMattersObject.put("boardGenderDiversityBoardOfDirectorsInPercent", JSONObject.NULL)
    }
}
