package db.migration

import db.migration.utils.TestUtils
import org.junit.jupiter.api.Test

@Suppress("ClassName")
class V12__MigrateHighImpactClimateSectorsSfdrTest {
    @Test
    fun `check migration script for high impact climate sectors in sfdr works properly`() {
        TestUtils().testMigrationOfSingleDataset(
            "sfdr",
            "V12/originalDatabaseEntry.json",
            "V12/expectedDatabaseEntry.json",
            V12__MigrateHighImpactClimateSectorsSfdr()::migrateSfdrHighImpactClimateSectors,
        )
    }
}
