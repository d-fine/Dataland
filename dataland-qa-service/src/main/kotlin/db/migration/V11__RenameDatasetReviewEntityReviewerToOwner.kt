package db.migration

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context
import org.slf4j.LoggerFactory

/**
 * This migration renames the column reviewer_user_id field to owner_id
 * in the dataset_review table.
 */

@Suppress("ClassName")
class V11__RenameDatasetReviewEntityReviewerToOwner : BaseJavaMigration() {
    private val logger = LoggerFactory.getLogger("Migration V11")

    override fun migrate(context: Context) {
        val targetConnection = context!!.connection
        val tableResultSet = targetConnection.metaData.getTables(null, null, "dataset_review", null)
        if (tableResultSet.next()) {
            val statement = targetConnection.createStatement()
            logger.info("Found table dataset_review. Renaming column reviewer_user_id to owner_id.")
            statement.executeUpdate(
                "ALTER TABLE dataset_review " +
                    "RENAME COLUMN reviewer_user_id TO owner_id;",
            )
            statement.close()
        }
    }
}
