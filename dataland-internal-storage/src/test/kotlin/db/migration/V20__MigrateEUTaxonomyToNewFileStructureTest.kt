package db.migration

import db.migration.utils.TestUtils
import org.junit.jupiter.api.Test

class V20__MigrateEUTaxonomyToNewFileStructureTest {

    private val frameworkEutaxonomy = "eutaxonomy"

    @Test
    fun `check if the new EU taxonomy fields have been implemented`() {
        TestUtils().testMigrationOfSingleDataset(
            frameworkEutaxonomy,
            "V20/originalCompanyInformationWithEutaxonomyNonFinancialsData.json",
            "V20/expectedCompanyInformationWithEutaxonomyNonFinancialsData.json",
            V20__MigrateEUTaxonomyToNewFilestructure()::migrateEutaxonomyNonFinancialsData,
        )
    }
}
