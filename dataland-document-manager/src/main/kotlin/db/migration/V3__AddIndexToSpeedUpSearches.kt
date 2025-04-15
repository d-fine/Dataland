package db.migration

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context
import org.slf4j.LoggerFactory
import java.sql.ResultSet

/**
 * This migration script adds an index for the company ID search
 */
@Suppress("ClassName")
class V3__AddIndexToSpeedUpSearches : BaseJavaMigration() {
    private val logger = LoggerFactory.getLogger("Migration V3")

    override fun migrate(context: Context?) {
        logger.info("Adding index to speed up document searches if possible.")
        if (tableExists(context!!, "document_meta_info_entity_company_ids")) {
            logger.info("Target table exists. Creating index.")
            createIndexForCompanyIds(context)
        }
    }

    private fun tableExists(
        context: Context,
        tableName: String,
    ): Boolean {
        val connection = context.connection
        val resultSet: ResultSet = connection.metaData.getTables(null, null, tableName, null)
        return resultSet.next()
    }

    private fun createIndexForCompanyIds(context: Context) {
        context.connection.createStatement().execute(
            "CREATE INDEX idx_company_ids ON document_meta_info_entity_company_ids (company_ids)",
        )
    }
}
