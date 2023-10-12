package db.migration

import db.migration.utils.TestUtils
import org.junit.jupiter.api.Test

class V10__MigrateRefactoredDataPointClassesLksgTest {
    @Test
    fun `test migration of refactored data point classes in Lksg framework`() {
        TestUtils().testMigrationOfSingleDataset(
            "V10/OriginalMockDatabaseEntry.json",
            "V10/expectedTransformedMockDatabaseEntry.json",
            V10__MigrateRefactoredDataPointClassesLksg()::migrateRefactoredDataPointClasses,
        )
        TestUtils().testMigrationOfSingleDataset(
            "V10/OriginalResponseWithEdgeCases.json",
            "V10/expectedTransformedResponseWithEdgeCases.json",
            V10__MigrateRefactoredDataPointClassesLksg()::migrateRefactoredDataPointClasses,
        )
    }
}
