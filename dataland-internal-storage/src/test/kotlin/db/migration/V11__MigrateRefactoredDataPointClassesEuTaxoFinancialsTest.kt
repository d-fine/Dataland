package db.migration

import db.migration.utils.TestUtils
import org.junit.jupiter.api.Test

@Suppress("ClassName")
class V11__MigrateRefactoredDataPointClassesEuTaxoFinancialsTest {
    @Test
    fun `test migration of refactored data point classes in Eu Taxonomy financials framework`() {
        val dataType = "eutaxonomy-financials"
        TestUtils().testMigrationOfSingleDataset(
            dataType,
            "V11/V11_oldDatabaseEntryFromBackendResponse.json",
            "V11/V11_expectedTransformedDatabaseEntryFromBackendResponse.json",
            V11__MigrateRefactoredDataPointClassesEuTaxoFinancials()::migrateRefactoredDataPointClasses,
        )
        TestUtils().testMigrationOfSingleDataset(
            dataType,
            "V11/V11_mockDataFromBackendResponseWithEdgeCases.json",
            "V11/V11_expectedTransformedMockDataFromBackendResponseWithEdgeCases.json",
            V11__MigrateRefactoredDataPointClassesEuTaxoFinancials()::migrateRefactoredDataPointClasses,
        )
        TestUtils().testMigrationOfSingleDataset(
            dataType,
            "V11/V11_realDataResponseWithEdgeCases.json",
            "V11/V11_expectedTransformedRealDataResponseWithEdgeCases.json",
            V11__MigrateRefactoredDataPointClassesEuTaxoFinancials()::migrateRefactoredDataPointClasses,
        )
    }
}
