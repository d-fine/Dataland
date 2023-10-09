package db.migration

import db.migration.utils.TestUtils
import org.junit.jupiter.api.Test

class V10__MigrateRefactoredDataPointClassesLksgTest {
    @Test
    fun `test migration of refactored data point classes in Lksg framework`() {
        TestUtils().testMigrationOfSingleDataset(
            "V10/oldOriginalDatabaseEntry.json",
            "V10/expectedTransformedDatabaseEntry.json",
            V10__MigrateRefactoredDataPointClassesLksg()::migrateRefactoredDataPointClasses,
        )
    }
}
