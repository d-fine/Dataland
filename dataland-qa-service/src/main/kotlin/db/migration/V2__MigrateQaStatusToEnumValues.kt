package db.migration

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context

/**
 * This class migrates the QA Status Values in the ReviewInformation Table from Int to their actual ENUM.Values
 */
@Suppress("ClassName")
class V2__MigrateQaStatusToEnumValues : BaseJavaMigration() {
    override fun migrate(context: Context?) {
        context!!.connection.createStatement().execute(
            "ALTER TABLE review_information " +
                "ADD qa_status_new VARCHAR(255);",
        )

        context.connection.createStatement().execute(
            "UPDATE review_information " +
                "SET qa_status_new = CASE " +
                "WHEN qa_status = 1 THEN 'Accepted' " +
                "WHEN qa_status = 2 THEN 'Rejected' " +
                "END;",
        )

        context.connection.createStatement().execute(
            "ALTER TABLE review_information " +
                "DROP COLUMN qa_status, " +
                "RENAME COLUMN qa_status_new TO qa_status",
        )
    }
}
