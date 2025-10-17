package db.migration

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context

/**
 * This migration script renames specific PCAF data point types in the data_point_meta_information table.
 */
@Suppress("ClassName")
class V10__RenamePcafDatapoints : BaseJavaMigration() {
    override fun migrate(context: Context?) {
        val statement = context!!.connection.createStatement()
        statement.execute(
            "UPDATE data_point_meta_information SET data_point_type = 'customEnumPcafMainSector'" +
                " WHERE data_point_type = 'extendedEnumPcafMainSector';",
        )
        statement.execute(
            "UPDATE data_point_meta_information SET data_point_type = 'customEnumCompanyExchangeStatus'" +
                " WHERE data_point_type = 'extendedEnumCompanyExchangeStatus';",
        )
    }
}
