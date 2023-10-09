package db.migration

import db.migration.utils.TestUtils
import org.junit.jupiter.api.Test

class V11__MigrateRefactoredDataPointClassesEuTaxoFinancialsTest {
    @Test
    fun `test migration of refactored data point classes in Eu Taxonomy financials framework`() {
        TestUtils().testMigrationOfSingleDataset(
            "V11/oldDatabaseEntryFromBackendResponse.json",
            "V11/transformedDatabaseEntryFromBackendResponse.json",
            V11__MigrateRefactoredDataPointClassesEuTaxoFinancials()::migrateRefactoredDataPointClasses,
        )
    }
}
