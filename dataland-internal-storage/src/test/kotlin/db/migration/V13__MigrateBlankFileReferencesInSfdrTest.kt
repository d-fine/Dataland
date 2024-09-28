package db.migration

import db.migration.utils.TestUtils
import org.junit.jupiter.api.Test

@Suppress("ClassName")
class V13__MigrateBlankFileReferencesInSfdrTest {
    @Test
    fun `check migration script for blank file references in sfdr works properly`() {
        TestUtils().testMigrationOfSingleDataset(
            "sfdr",
            "V13/originalDatabaseEntry.json",
            "V13/expectedDatabaseEntry.json",
            V13__MigrateBlankFileReferencesInSfdr()::migrateBlankFileReferences,
        )
    }
}
