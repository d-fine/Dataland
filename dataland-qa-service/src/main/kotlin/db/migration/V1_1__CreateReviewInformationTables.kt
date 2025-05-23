package db.migration

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context

/**
 * This migration script handles the creation of the initial databases
 */
@Suppress("ClassName")
class V1_1__CreateReviewInformationTables : BaseJavaMigration() {
    override fun migrate(context: Context?) {
        context!!.connection.createStatement().execute(
            "CREATE TABLE review_information (" +
                "data_id varchar(255) NOT NULL, " +
                "reception_time smallint NOT NULL, " +
                "qa_status smallint NOT NULL, " +
                "reviewer_keycloak_id varchar(255) NOT NULL, " +
                "message varchar(255), " +
                "PRIMARY KEY (data_id)" +
                ")",
        )
    }
}
