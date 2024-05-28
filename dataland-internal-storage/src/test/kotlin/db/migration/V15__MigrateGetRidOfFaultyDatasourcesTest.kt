package db.migration

import db.migration.utils.TestUtils
import org.junit.jupiter.api.Test

class V15__MigrateGetRidOfFaultyDatasourcesTest {
    @Test
    fun `check migration script for faulty file references in sfdr works properly`() {
        TestUtils().testMigrationOfSingleDataset(
            "lksg",
            "V15/originalLksgDatabaseEntry.json",
            "V15/expectedLksgDatabaseEntry.json",
            V15__MigrateGetRidOfFaultyDatasources()::migrateFaultyFileReferences,
        )
    }
}
