package db.migration

import db.migration.utils.TestUtils
import org.junit.jupiter.api.Test

@Suppress("ClassName")
class V14__MigrateSubstantialContributionToClimateChangeAdaptationTest {
    @Test
    fun `check migration script for field name change in eu taxonomy non financials works properly`() {
        val dataType = "eutaxonomy-non-financials"
        TestUtils().testMigrationOfSingleDataset(
            dataType,
            "V14/originalDatabaseEntry.json",
            "V14/expectedDatabaseEntry.json",
            V14__MigrateSubstantialContributionToClimateChangeAdaptation()
                ::migrateSubstantialContributionToClimateChangeAdaptation,
        )
        TestUtils().testMigrationOfSingleDataset(
            dataType,
            "V14/originalDatabaseEntryWithNullAlignedActivities.json",
            "V14/expectedDatabaseEntryWitNullAlignedActivities.json",
            V14__MigrateSubstantialContributionToClimateChangeAdaptation()
                ::migrateSubstantialContributionToClimateChangeAdaptation,
        )
    }
}
