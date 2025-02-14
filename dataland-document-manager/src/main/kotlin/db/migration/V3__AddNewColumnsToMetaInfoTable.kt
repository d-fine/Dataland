package db.migration

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context

/**
 * This migration script handles the creation of the initial databases
 */
@Suppress("ClassName")
class V3__AddNewColumnsToMetaInfoTable : BaseJavaMigration() {
    override fun migrate(context: Context?) {
        addDocumentName(context!!)
        addDocumentCategory(context)
        addCompanyIds(context)
        addPublicationDate(context)
        addReportingPeriod(context)
    }

    private fun addDocumentName(context: Context) {
        context.connection.createStatement().execute(
            """
            ALTER TABLE document_meta_info
            ADD COLUMN document_name VARCHAR(255) DEFAULT NULL
            """.trimIndent(),
        )
    }

    private fun addDocumentCategory(context: Context) {
        context.connection.createStatement().execute(
            """
            ALTER TABLE document_meta_info
            ADD COLUMN document_category VARCHAR(255) DEFAULT NULL
            """.trimIndent(),
        )
    }

    private fun addCompanyIds(context: Context) {
        context.connection.createStatement().execute(
            """
            ALTER TABLE document_meta_info
            ADD COLUMN company_ids TEXT NOT NULL
            """.trimIndent(),
        )
    }

    private fun addPublicationDate(context: Context) {
        context.connection.createStatement().execute(
            """
            ALTER TABLE document_meta_info
            ADD COLUMN publication_date DATE DEFAULT NULL
            """.trimIndent(),
        )
    }

    private fun addReportingPeriod(context: Context) {
        context.connection.createStatement().execute(
            """
            ALTER TABLE document_meta_info
            ADD COLUMN reporting_period VARCHAR(255) DEFAULT NULL
            """.trimIndent(),
        )
    }
}
