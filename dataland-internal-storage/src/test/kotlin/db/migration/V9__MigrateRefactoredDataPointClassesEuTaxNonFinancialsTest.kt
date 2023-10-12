package db.migration

import db.migration.utils.TestUtils
import org.junit.jupiter.api.Test

class V9__MigrateRefactoredDataPointClassesEuTaxNonFinancialsTest {
    @Test
    fun `test migration of refactored data point classes in eu taxonomy non financials framework`() {
        TestUtils().testMigrationOfSingleDataset(
            "V9/V9_mockDataFromBackendResponse.json",
            "V9/V9_expectedTransformedMockDataFromBackendResponse.json",
            V9__MigrateRefactoredDataPointClassesEuTaxNonFinancials()::migrateRefactoredDataPointClasses,
        )
    }
}
