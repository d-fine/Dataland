package db.migration

import db.migration.utils.TestUtils
import org.junit.jupiter.api.Test

class V6__MigrateFromAmountWithCurrencyToUnitTest {

    @Test
    fun `test eu taxonomy for non financials migration script works as expected for migrating old data`() {
        TestUtils().testMigrationOfSingleDataset(
            "V6/oldOriginalDatabaseEntry.json",
            "V6/expectedTransformedDatabaseEntry.json",
            V6__MigrateFromAmountWithCurrencyToUnit()::migrateEuTaxonomyAmountWithCurrencyData
        )
        TestUtils().testMigrationOfSingleDataset(
            "V6/oldOriginalDatabaseEntry2.json",
            "V6/expectedTransformedDatabaseEntry2.json",
            V6__MigrateFromAmountWithCurrencyToUnit()::migrateEuTaxonomyAmountWithCurrencyData
        )
    }
}
