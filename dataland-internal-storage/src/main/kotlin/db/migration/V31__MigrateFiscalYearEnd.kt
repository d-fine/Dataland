package db.migration;

import org.dataland.datalandbackendutils.utils.DataPointUtils
import org.flywaydb.core.api.migration.BaseJavaMigration
import org.flywaydb.core.api.migration.Context

/**
 * Migration to convert Fiscal-Year-End in the form of YYYY-MM-DD to DD-MMM
 */

@Suppress("ClassName")
class V31__MigrateFiscalYearEnd : BaseJavaMigration() {
    private val oldToNewDateFormat =
        mapOf(

        )
}