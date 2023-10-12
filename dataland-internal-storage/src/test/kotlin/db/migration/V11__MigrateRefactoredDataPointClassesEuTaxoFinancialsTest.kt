package db.migration

import db.migration.utils.TestUtils
import org.junit.jupiter.api.Test

class V11__MigrateRefactoredDataPointClassesEuTaxoFinancialsTest {
    @Test
    fun `test migration of refactored data point classes in Eu Taxonomy financials framework`() {
        TestUtils().testMigrationOfSingleDataset(
            "V11/oldDatabaseEntryFromBackendResponse.json",
            "V11/expectedTransformedDatabaseEntryFromBackendResponse.json",
            V11__MigrateRefactoredDataPointClassesEuTaxoFinancials()::migrateRefactoredDataPointClasses,
        )
        TestUtils().testMigrationOfSingleDataset(
            "V11/mockDataFromBackendResponseWithEdgeCases.json",
            "V11/expectedTransformedMockDataFromBackendResponseWithEdgeCases.json",
            V11__MigrateRefactoredDataPointClassesEuTaxoFinancials()::migrateRefactoredDataPointClasses,
        )
        TestUtils().testMigrationOfSingleDataset(
            "V11/realDataResponseWithEdgeCases.json",
            "V11/expectedTransformedRealDataResponseWithEdgeCases.json",
            V11__MigrateRefactoredDataPointClassesEuTaxoFinancials()::migrateRefactoredDataPointClasses,
        )
    }
}
