package db.migration

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context

/**
 * This migration script handles the creation of the initial databases
 */
@Suppress("ClassName")
class V1_1__CreateMetaInfoTable : BaseJavaMigration() {
    override fun migrate(context: Context?) {
        context!!.connection.createStatement().execute(
            """
            CREATE TABLE document_meta_info (
                document_id varchar(255) NOT NULL,
                upload_time bigint NOT NULL,
                uploader_id varchar(255) NOT NULL,
                qa_status smallint NOT NULL,
                CONSTRAINT document_meta_info_pkey PRIMARY KEY (document_id)
            )
            """.trimIndent(),
        )
    }
}
