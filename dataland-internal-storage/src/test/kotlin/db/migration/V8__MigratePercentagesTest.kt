package db.migration

import db.migration.utils.TestUtils
import org.junit.jupiter.api.Test

class V8__MigratePercentagesTest {

    @Test
    fun `test eu taxonomy for non financials migration script works as expected for migrating old data`() {
        TestUtils().testMigrationOfSingleDataset(
            "V8/originalEuTaxonomyFinancialsData.json",
            "V8/transformedEuTaxonomyFinancialsData.json",
            V8__MigratePercentages()::migrateEuTaxonomyFinancials,
        )
    }
}
