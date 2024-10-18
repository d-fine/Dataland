package db.migration

import db.migration.utils.TestUtils
import org.junit.jupiter.api.Test

@Suppress("ClassName")
class V20__MigrateEutaxonomyNonFinancialsExtendedDatapointsTest {
    private val frameworkEutaxonomyNonFinancials = "eutaxonomy-non-financials"

    @Test
    fun `basic test for the migration of EU taxonomy non financials data`() {
        TestUtils().testMigrationOfSingleDataset(
            dataType = frameworkEutaxonomyNonFinancials,
            oldDataFileLocation = "V20/BaseInput.json",
            migratedDataFileLocation = "V20/ExpectedBaseInput.json",
            migration = V20__MigrateEutaxonomyNonFinancialsExtendedDatapoints()::migrateEutaxonomyNonFinancialsData,
        )
    }

    @Test
    fun `extensive test for the migration of EU taxonomy non financials data`() {
        TestUtils().testMigrationOfSingleDataset(
            dataType = frameworkEutaxonomyNonFinancials,
            oldDataFileLocation = "V20/PreparedInput.json",
            migratedDataFileLocation = "V20/ExpectedPreparedInput.json",
            migration = V20__MigrateEutaxonomyNonFinancialsExtendedDatapoints()::migrateEutaxonomyNonFinancialsData,
        )
    }

    @Test
    fun `test with many null entries for the migration of EU taxonomy non financials data`() {
        TestUtils().testMigrationOfSingleDataset(
            dataType = frameworkEutaxonomyNonFinancials,
            oldDataFileLocation = "V20/BrokenData.json",
            migratedDataFileLocation = "V20/ExpectedBrokenData.json",
            migration = V20__MigrateEutaxonomyNonFinancialsExtendedDatapoints()::migrateEutaxonomyNonFinancialsData,
        )
    }

    @Test
    fun `test with broken data from production`() {
        TestUtils().testMigrationOfSingleDataset(
            dataType = frameworkEutaxonomyNonFinancials,
            oldDataFileLocation = "V20/BrokenDataServerError.json",
            migratedDataFileLocation = "V20/ExpectedBrokenDataServerError.json",
            migration = V20__MigrateEutaxonomyNonFinancialsExtendedDatapoints()::migrateEutaxonomyNonFinancialsData,
        )
    }
}
