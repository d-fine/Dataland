package db.migration

import db.migration.utils.TestUtils
import org.junit.jupiter.api.Test

class V15__MigrateGetRidOfFaultyDatasourcesTest {
    @Test
    fun `check migration script for faulty file references in sfdr works properly`() {
        TestUtils().testMigrationOfSingleDataset(
            "sfdr",
            "V15/originalDatabaseEntry.json",
            "V15/expectedDatabaseEntry.json",
            V15__MigrateGetRidOfFaultyDatasources()::migrateFaultyFileReferences,
        )
    }
}