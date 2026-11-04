package db.migration

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context

/**
 * Renames the column reason_for_custom_data_point of data point judgements to reviewer_comment, as the comment is
 * no longer tied to the custom source. No data is lost: if Hibernate has already created an empty reviewer_comment
 * column, the existing values are copied over before the old column is dropped.
 */
@Suppress("ClassName")
class V20__RenameReasonForCustomDataPointToReviewerComment : BaseJavaMigration() {
    companion object {
        const val DATA_POINT_JUDGEMENT_TABLE = "dataset_judgement_entity_data_point_judgement"
        const val OLD_COLUMN = "reason_for_custom_data_point"
        const val NEW_COLUMN = "reviewer_comment"
    }

    override fun migrate(context: Context) {
        val metaData = context.connection.metaData
        val tableExists = metaData.getTables(null, null, DATA_POINT_JUDGEMENT_TABLE, null).next()
        val oldColumnExists = metaData.getColumns(null, null, DATA_POINT_JUDGEMENT_TABLE, OLD_COLUMN).next()
        if (!tableExists || !oldColumnExists) {
            return
        }
        val newColumnExists = metaData.getColumns(null, null, DATA_POINT_JUDGEMENT_TABLE, NEW_COLUMN).next()

        context.connection.createStatement().use { statement ->
            if (newColumnExists) {
                statement.execute(
                    """
                    UPDATE $DATA_POINT_JUDGEMENT_TABLE
                        SET $NEW_COLUMN = COALESCE($NEW_COLUMN, $OLD_COLUMN)
                    """.trimIndent(),
                )
                statement.execute("ALTER TABLE $DATA_POINT_JUDGEMENT_TABLE DROP COLUMN $OLD_COLUMN")
            } else {
                statement.execute("ALTER TABLE $DATA_POINT_JUDGEMENT_TABLE RENAME COLUMN $OLD_COLUMN TO $NEW_COLUMN")
            }
        }
    }
}
