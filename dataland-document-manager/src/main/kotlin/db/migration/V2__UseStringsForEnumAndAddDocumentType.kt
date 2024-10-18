package db.migration

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context

/**
 * This migration script handles the creation of the initial databases
 */
@Suppress("ClassName")
class V2__UseStringsForEnumAndAddDocumentType : BaseJavaMigration() {
    override fun migrate(context: Context?) {
        migrateEnum(context!!)
        addDocumentType(context)
    }

    private fun addDocumentType(context: Context) {
        context.connection.createStatement().execute(
            """  
            ALTER TABLE document_meta_info
            ADD COLUMN document_type varchar(255) NOT NULL DEFAULT 'Pdf'
            """.trimIndent(),
        )
    }

    private fun migrateEnum(context: Context) {
        context.connection.createStatement().execute(
            """
            CREATE OR REPLACE FUNCTION change_status(status smallint) RETURNS varchar(255)
            LANGUAGE SQL
            IMMUTABLE
            RETURNS NULL ON NULL INPUT
            RETURN CASE
             WHEN status = 0 THEN 'Pending' 
                WHEN status = 1 THEN 'Accepted' 
                ELSE 'Rejected'
            END;
            
            ALTER TABLE document_meta_info
            DROP CONSTRAINT IF EXISTS document_meta_info_qa_status_check,
            ALTER COLUMN qa_status TYPE varchar (255) USING change_status(qa_status),
            ADD CONSTRAINT document_meta_info_qa_status_check CHECK (qa_status in ('Pending', 'Accepted', 'Rejected'))
            """.trimIndent(),
        )
    }
}
