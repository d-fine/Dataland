package db.migration

import db.migration.utils.TestUtils
import org.junit.jupiter.api.Test

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
}
