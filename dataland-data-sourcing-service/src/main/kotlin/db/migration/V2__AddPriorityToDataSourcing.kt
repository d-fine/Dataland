package db.migration

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context

/**
 * Creates the data_sourcing table if it does not yet exist and adds the priority column.
 */
@Suppress("ClassName")
class V2__AddPriorityToDataSourcing : BaseJavaMigration() {
    override fun migrate(context: Context?) {
        context!!.connection.createStatement().execute(
            "CREATE TABLE IF NOT EXISTS data_sourcing (" +
                "data_sourcing_id uuid NOT NULL, " +
                "company_id uuid NOT NULL, " +
                "reporting_period varchar(255) NOT NULL, " +
                "data_type varchar(255) NOT NULL, " +
                "state varchar(255) NOT NULL, " +
                "date_of_next_document_sourcing_attempt date, " +
                "document_collector uuid, " +
                "data_extractor uuid, " +
                "admin_comment varchar(1000), " +
                "PRIMARY KEY (data_sourcing_id), " +
                "CONSTRAINT uq_data_sourcing UNIQUE (company_id, reporting_period, data_type)" +
                ")",
        )
        context.connection.createStatement().execute(
            "ALTER TABLE data_sourcing ADD COLUMN IF NOT EXISTS priority INTEGER NOT NULL DEFAULT 10;",
        )
    }
}
