package db.migration

import db.migration.utils.TestUtils
import org.junit.jupiter.api.Test

class V19__MigrateEutaxonomyNonFinancialsExtendedDatapointsTest {
    private val frameworkEutaxonomyNonFinancials = "eutaxonomy-non-financials"

    @Test
    fun `basic test for the migration of EU taxonomy non financials data`() {
        TestUtils().testMigrationOfSingleDataset(
            dataType = frameworkEutaxonomyNonFinancials,
            oldDataFileLocation = "V19/BaseInput.json",
            migratedDataFileLocation = "V19/ExpectedBaseInput.json",
            migration = V19__MigrateEutaxonomyNonFinancialsExtendedDatapoints()::migrateEutaxonomyNonFinancialsData,
        )
    }

    @Test
    fun `extensive test for the migration of EU taxonomy non financials data`() {
        TestUtils().testMigrationOfSingleDataset(
            dataType = frameworkEutaxonomyNonFinancials,
            oldDataFileLocation = "V19/PreparedInput.json",
            migratedDataFileLocation = "V19/ExpectedPreparedInput.json",
            migration = V19__MigrateEutaxonomyNonFinancialsExtendedDatapoints()::migrateEutaxonomyNonFinancialsData,
        )
    }
}
