package db.migration

import db.migration.utils.TestUtils
import org.junit.jupiter.api.Test

class V19__MigrateEutaxonomyNonFinancialsExtendedDatapointsTest {
    private val frameworkEutaxonomyNonFinancials = "eutaxonomy-non-financials"

    @Test
    fun `dummy test for EU taxonomy data`() {
        TestUtils().testMigrationOfSingleDataset(
            dataType = frameworkEutaxonomyNonFinancials,
            oldDataFileLocation = "V19/BaseInput.json",
            migratedDataFileLocation = "V19/BaseExpectedInput.json",
            migration = V19__MigrateEutaxonomyNonFinancialsExtendedDatapoints()::migrateEutaxonomyNonFinancialsData,
        )
    }
}
