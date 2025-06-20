package db.migration

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
        val tableName = "data_point_qa_review"
        val reviewResultSet = connection.metaData.getTables(null, null, tableName, null)

        if (reviewResultSet.next()) {
            migrateNfrdMandatoryField(context, tableName)
        }
    }

    /**
     * Migrates all rows of backend tables corresponding to NFRD Mandatory Field
     * @context the context of the migration script
     */
    fun migrateNfrdMandatoryField(
        context: Context,
        tableName: String,
    ) {
        val statement = context.connection.createStatement()

        val count =
            statement.executeUpdate(
                "UPDATE $tableName SET data_point_type = 'extendedEnumYesNoIsNfrdMandatory' " +
                    "WHERE data_point_type = 'extendedEnumYesNoNfrdMandatory'",
            )

        logger
            .info(
                "Updated $count rows in $tableName from" +
                    " \"extendedEnumYesNoNfrdMandatory\" to \"extendedEnumYesNoIsNfrdMandatory\"",
            )

        statement?.close()
    }
}
