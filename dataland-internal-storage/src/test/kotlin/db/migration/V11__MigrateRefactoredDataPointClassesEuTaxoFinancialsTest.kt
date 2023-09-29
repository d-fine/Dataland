package db.migration

import db.migration.utils.TestUtils
import org.junit.jupiter.api.Test

class V11__MigrateRefactoredDataPointClassesEuTaxoFinancialsTest {
    @Test
    fun `test migration of refactored data point classes in Eu Taxonomy financials framework`() {
        TestUtils().testMigrationOfSingleDataset(
            "V11/oldOriginalDatabaseEntry.json",
            "V11/expectedTransformedDatabaseEntry.json",
            V11_MigrateRefactoredDataPointClassesEuTaxoFinancials()::migrateRefactoredDataPointClasses,
        )
    }
}
