package db.migration

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context

/**
 * This migration script renames specific PCAF data point types in the data_point_meta_information table.
 */
@Suppress("ClassName")
class V10__RenamePcafDatapoints : BaseJavaMigration() {
    override fun migrate(context: Context?) {
        if (context!!
                .connection.metaData
                .getTables(
                    null, null, "data_point_meta_information", null,
                ).next()
        ) {
            val statement = context.connection.createStatement()
            statement.execute(
                "UPDATE data_point_meta_information SET data_point_type = 'extendedEnumPcafMainSector'" +
                    " WHERE data_point_type = 'customEnumPcafMainSector';",
            )
            statement.execute(
                "UPDATE data_point_meta_information SET data_point_type = 'extendedEnumCompanyExchangeStatus'" +
                    " WHERE data_point_type = 'customEnumCompanyExchangeStatus';",
            )
        }
    }
}
