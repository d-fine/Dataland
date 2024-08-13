package db.migration

import db.migration.utils.TestUtils
import org.junit.jupiter.api.Test

class V20__MigrateEUTaxonomyToNewFileStructureTest {

    private val frameworkEutaxonomy = "eutaxonomy"

    @Test
    fun `this is a test`() {
        TestUtils().testMigrationOfSingleDataset(
            frameworkEutaxonomy,
            "V20/originalSHORTCompanyInformationWithEutaxonomyNonFinancialsData.json",
            "V20/expectedSHORTCompanyInformationWithEutaxonomyNonFinancialsData.json",
            V20__MigrateEUTaxonomyToNewFilestructure()::migrateEutaxonomyNonFinancialsData,
        )
    }
}