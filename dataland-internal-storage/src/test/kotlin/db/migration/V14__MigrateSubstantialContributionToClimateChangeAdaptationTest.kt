package db.migration

import db.migration.utils.TestUtils
import org.junit.jupiter.api.Test

class V14__MigrateSubstantialContributionToClimateChangeAdaptationTest {
    @Test
    fun `check migration script for field name change in eu taxonomy non financials works properly`() {
        TestUtils().testMigrationOfSingleDataset(
            "eutaxonomy-non-financials",
            "V14/originalDatabaseEntry.json",
            "V14/expectedDatabaseEntry.json",
            V14__MigrateSubstantialContributionToClimateChangeAdaptation()
            ::migrateSubstantialContributionToClimateChangeAdaptation,
        )
    }
}
