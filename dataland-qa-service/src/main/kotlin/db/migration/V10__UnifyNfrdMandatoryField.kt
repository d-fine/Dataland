package org.dataland.datalandqaservice.db.migration

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context
import org.slf4j.LoggerFactory

/**
 * This migration script unifies the previous called "extendedEnumYesNoNfrdMandatory" and
 * "extendedEnumYesNoIsNfrdMandatory" to only one data point, now called "extendedEnumYesNoIsNfrdMandatory"
 */
@Suppress("ClassName")
class V10__UnifyNfrdMandatoryField : BaseJavaMigration() {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun migrate(context: Context?) {
        val connection = context!!.connection
        val reviewResultSet = connection.metaData.getTables(null, null, "data_point_qa_review", null)

        if (reviewResultSet.next()) {
            migrateNfrdMandatoryField(context)
        }
    }

    /**
     * Migrates all rows of backend tables corresponding to NFRD Mandatory Field
     * @context the context of the migration script
     * @tableName the name of the table
     */
    fun migrateNfrdMandatoryField(context: Context) {
        val statement = context.connection.createStatement()
        val count =
            statement.executeUpdate(
                "UPDATE 'data_point_qa_review' SET data_point_type = 'extendedEnumYesNoIsNfrdMandatory' " +
                    "WHERE data_point_type = 'extendedEnumYesNoNfrdMandatory'",
            )

        logger
            .info(
                "Updated $count rows in \"data_point_qa_review\" from" +
                    " \"extendedEnumYesNoNfrdMandatory\" to \"extendedEnumYesNoIsNfrdMandatory\"",
            )

        statement?.close()
    }
}
