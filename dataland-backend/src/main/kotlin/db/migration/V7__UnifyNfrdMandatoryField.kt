package db.migration

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context
import org.slf4j.LoggerFactory

/**
 * This migration script unifies the previously called "extendedEnumYesNoNfrdMandatory" and
 * "extendedEnumYesNoIsNfrdMandatory" to only one data point, now called "extendedEnumYesNoIsNfrdMandatory"
 */
@Suppress("ClassName")
class V7__UnifyNfrdMandatoryField : BaseJavaMigration() {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun migrate(context: Context?) {
        val connection = context!!.connection
        val metaTable = "data_point_meta_information"
        val uuidTable = "data_point_uuid_map"
        val typeColumn = "data_point_type"
        val identifierColumn = "data_point_identifier"
        val metaResultSet = connection.metaData.getTables(null, null, metaTable, null)
        val uuidResultSet = connection.metaData.getTables(null, null, uuidTable, null)

        if (metaResultSet.next()) {
            migrateNfrdMandatoryField(context, metaTable, typeColumn)
        }
        if (uuidResultSet.next()) {
            migrateNfrdMandatoryField(context, uuidTable, identifierColumn)
        }
    }

    /**
     * Migrates all rows of backend tables corresponding to NFRD Mandatory Field
     * @context the context of the migration script
     * @tableName the name of the table
     */
    fun migrateNfrdMandatoryField(
        context: Context,
        tableName: String,
        columnName: String,
    ) {
        val statement = context.connection.createStatement()
        val count =
            statement.executeUpdate(
                "UPDATE $tableName SET $columnName = 'extendedEnumYesNoIsNfrdMandatory' " +
                    "WHERE $columnName = 'extendedEnumYesNoNfrdMandatory'",
            )

        logger
            .info(
                "Updated $count rows in $tableName from" +
                    " \"extendedEnumYesNoNfrdMandatory\" to \"extendedEnumYesNoIsNfrdMandatory\"",
            )

        statement?.close()
    }
}
