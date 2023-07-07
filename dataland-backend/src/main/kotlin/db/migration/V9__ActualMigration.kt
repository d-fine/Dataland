package db.migration

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context

class V9__ActualMigration: BaseJavaMigration() {
    override fun migrate(context: Context?) {
        val resultSet = context!!.connection.createStatement().executeQuery("SELECT * FROM stored_companies")!!
        while(resultSet.next()) {
            val companyName = resultSet.getString("company_name")
            if (companyName.contains("sdf")) {
                println(companyName)
                val companyId = resultSet.getString("company_id")
                context.connection.createStatement().execute("UPDATE stored_companies SET country_code = 'banana' WHERE company_id = '$companyId'")
            }
        }
    }
}