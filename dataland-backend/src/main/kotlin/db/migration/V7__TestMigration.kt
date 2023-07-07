package db.migration

import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context

class V7__TestMigration: BaseJavaMigration() {
    override fun migrate(context: Context?) {
//    fun migrate(context: Context?) {
        println("DADADADADADA 7 is a test db.migrations DADADADADADA")
    }
}