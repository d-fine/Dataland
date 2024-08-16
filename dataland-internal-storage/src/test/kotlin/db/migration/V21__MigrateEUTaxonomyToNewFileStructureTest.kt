package db.migration

import db.migration.utils.TestUtils
import org.junit.jupiter.api.Test

class V21__MigrateEUTaxonomyToNewFileStructureTest {

    private val frameworkEutaxonomy = "eutaxonomy"

    @Test
    fun `check if the new EU taxonomy fields have been implemented`() {
        TestUtils().testMigrationOfSingleDataset(
            frameworkEutaxonomy,
            "V21/originalCompanyInformationWithEutaxonomyNonFinancialsData.json",
            "V21/expectedCompanyInformationWithEutaxonomyNonFinancialsData.json",
            V21__MigrateEUTaxonomyToNewFilestructure()::migrateEutaxonomyNonFinancialsData,
        )
    }

    @Test
    fun `check the data set from prod that broke migration`() {
        TestUtils().testMigrationOfSingleDataset(
            frameworkEutaxonomy,
            "V21/originalBrokenProdExample.json",
            "V21/expectedBrokenProdExample.json",
            V21__MigrateEUTaxonomyToNewFilestructure()::migrateEutaxonomyNonFinancialsData,
        )
    }

}
