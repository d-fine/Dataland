package org.dataland.datalandqaservice.db.migration

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context

/**
 * This class migrates the QA Status Values in the ReviewInformation Table from Int to their actual ENUM.Values
 */
@Suppress("ClassName")
class V1__MigrateQaStatusToEnumValues : BaseJavaMigration() {
    override fun migrate(context: Context?) {
        context!!.connection.createStatement().execute(
            "CREATE TYPE qa_status AS ENUM('Pending', 'Accepted', 'Rejected')",
        )
        context.connection.createStatement().execute(
            "ALTER TABLE review_information " +
                "ALTER COLUMN qa_status SET DATA TYPE qa_status " +
                "USING qa_status::qa_status",
        )
    }
}
