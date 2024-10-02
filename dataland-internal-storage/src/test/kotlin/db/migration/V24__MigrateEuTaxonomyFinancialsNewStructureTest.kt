package db.migration

import db.migration.utils.TestUtils
import org.junit.jupiter.api.Test

class V24__MigrateEuTaxonomyFinancialsNewStructureTest {
    private val framework = "eutaxonomy-financials"

    @Test
    fun `test with companyForAllTypes data`() {
        TestUtils().testMigrationOfSingleDataset(
            dataType = framework,
            oldDataFileLocation = "V24/EuTaxonomyFinancialsOriginal.json",
            migratedDataFileLocation = "V24/EuTaxonomyFinancialsExpected.json",
            migration = V24__MigrateEuTaxonomyFinancialsNewStructure()::migrateEuTaxonomyFinancialsData,
        )
    }
}
