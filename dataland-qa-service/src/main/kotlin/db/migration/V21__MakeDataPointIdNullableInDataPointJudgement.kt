package db.migration

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context
import java.sql.DatabaseMetaData

/**
 * Makes the data_point_id column of data point judgements nullable.
 * Data points that a judge adds as custom values although they are missing in the original dataset
 * have no original data point and therefore no data point id.
 */
@Suppress("ClassName")
class V21__MakeDataPointIdNullableInDataPointJudgement : BaseJavaMigration() {
    companion object {
        const val DATA_POINT_JUDGEMENT_TABLE = "dataset_judgement_entity_data_point_judgement"
        const val DATA_POINT_ID_COLUMN = "data_point_id"
    }

    override fun migrate(context: Context) {
        val metaData = context.connection.metaData
        val columns = metaData.getColumns(null, null, DATA_POINT_JUDGEMENT_TABLE, DATA_POINT_ID_COLUMN)
        val isAlreadyNullable = !columns.next() || columns.getInt("NULLABLE") == DatabaseMetaData.columnNullable
        if (isAlreadyNullable) {
            return
        }

        context.connection.createStatement().execute(
            """
            ALTER TABLE $DATA_POINT_JUDGEMENT_TABLE
                ALTER COLUMN $DATA_POINT_ID_COLUMN DROP NOT NULL
            """.trimIndent(),
        )
    }
}
