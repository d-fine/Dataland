package db.migration

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context

/**
 * Adds storage for the data point base type to data point judgements.
 * Existing rows are intentionally left null.
 */
@Suppress("ClassName")
class V17__AddDataPointBaseTypeToDataPointJudgement : BaseJavaMigration() {
    companion object {
        const val DATA_POINT_JUDGEMENT_TABLE = "dataset_judgement_entity_data_point_judgement"
        const val DATA_POINT_BASE_TYPE_COLUMN = "data_point_base_type"
    }

    override fun migrate(context: Context) {
        val metaData = context.connection.metaData
        val tableExists = metaData.getTables(null, null, DATA_POINT_JUDGEMENT_TABLE, null).next()
        val columnAlreadyExists = metaData.getColumns(null, null, DATA_POINT_JUDGEMENT_TABLE, DATA_POINT_BASE_TYPE_COLUMN).next()
        if (!tableExists || columnAlreadyExists) {
            return
        }

        context.connection.createStatement().execute(
            """
            ALTER TABLE $DATA_POINT_JUDGEMENT_TABLE
                ADD COLUMN $DATA_POINT_BASE_TYPE_COLUMN TEXT
            """.trimIndent(),
        )
    }
}
