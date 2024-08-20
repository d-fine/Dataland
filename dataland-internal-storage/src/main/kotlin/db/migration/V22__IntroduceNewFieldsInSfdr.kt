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

    private val oldToNewSocialAndEmployeeMattersKey = mapOf(
        "femaleBoardMembers" to "femaleBoardMembersBoardOfDirectors",
        "maleBoardMembers" to "maleBoardMembersBoardOfDirectors",
        "boardGenderDiversityInPercent" to "boardGenderDiversityBoardOfDirectorsInPercent",
    )

    /**
     * Migrates sfdr data to the new sfdr data model.
     */
    fun migrateBoardFields(dataTableEntity: DataTableEntity) {
        val dataset = dataTableEntity.dataJsonObject

        val socialObject = dataset.optJSONObject("social") ?: return
        val socialAndEmployeeMattersObject = socialObject.optJSONObject("socialAndEmployeeMatters") ?: return

        for ((oldKey, newKey) in oldToNewSocialAndEmployeeMattersKey) {
            val oldValue = socialAndEmployeeMattersObject.remove(oldKey) ?: continue
            socialAndEmployeeMattersObject.put(newKey, oldValue)
        }

        dataTableEntity.companyAssociatedData.put("data", dataset.toString())
    }
}
