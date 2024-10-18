package db.migration

import db.migration.utils.TestUtils
import org.junit.jupiter.api.Test

@Suppress("ClassName")
class V10__MigrateRefactoredDataPointClassesLksgTest {
    @Test
    fun `test migration of refactored data point classes in Lksg framework`() {
        val dataType = "lksg"
        TestUtils().testMigrationOfSingleDataset(
            dataType,
            "V10/V10_OriginalMockDatabaseEntry.json",
            "V10/V10_expectedTransformedMockDatabaseEntry.json",
            V10__MigrateRefactoredDataPointClassesLksg()::migrateRefactoredDataPointClasses,
        )
        TestUtils().testMigrationOfSingleDataset(
            dataType,
            "V10/V10_OriginalResponseWithEdgeCases.json",
            "V10/V10_expectedTransformedResponseWithEdgeCases.json",
            V10__MigrateRefactoredDataPointClassesLksg()::migrateRefactoredDataPointClasses,
        )
    }
}
