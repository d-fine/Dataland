package db.migration

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context

class V2__TestMigration: BaseJavaMigration() {
    override fun migrate(context: Context?) {
//    fun migrate(context: Context?) {
        println("DADADADADADA this is a test db.migrations DADADADADADA")
    }
}