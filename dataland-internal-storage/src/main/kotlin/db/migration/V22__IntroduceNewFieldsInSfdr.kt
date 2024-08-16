package db.migration

import db.migration.utils.DataTableEntity
import db.migration.utils.migrateCompanyAssociatedDataOfDatatype
import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context

/**
 * This migration script updates all sfdr datasets to match the new sfdr data model.
 */
class V22__IntroduceNewFieldsInSfdr : BaseJavaMigration() {

    override fun migrate(context: Context?) {
        migrateCompanyAssociatedDataOfDatatype(
            context,
            "sfdr",
            this::migrateBoardFields,
        )
    }

    /**
     * Migrates sfdr data to the new sfdr data model.
     */
    fun migrateBoardFields(dataTableEntity: DataTableEntity) {
        val dataset = dataTableEntity.dataJsonObject

        val socialObject = dataset.getJSONObject("social") ?: return

        val socialAndEmployeeMattersObject = socialObject.getJSONObject("socialAndEmployeeMatters") ?: return

        val femaleBoardMembers = socialAndEmployeeMattersObject.remove("femaleBoardMembers")
        socialAndEmployeeMattersObject.put("femaleBoardMembersSupervisoryBoard", femaleBoardMembers)

        val maleBoardMembers = socialAndEmployeeMattersObject.remove("maleBoardMembers")
        socialAndEmployeeMattersObject.put("maleBoardMembersSupervisoryBoard", maleBoardMembers)

        val boardGenderDiversity = socialAndEmployeeMattersObject.remove("boardGenderDiversityInPercent")
        socialAndEmployeeMattersObject.put("boardGenderDiversitySupervisoryBoardInPercent", boardGenderDiversity)

        dataTableEntity.companyAssociatedData.put("data", dataset.toString())
    }
}
