package db.migration

import db.migration.utils.TestUtils
import org.junit.jupiter.api.Test

class V23__MigratePageZeroToNullTest {

    @Test
    fun `check migration script for eu taxonomy financials`() {
        TestUtils().testMigrationOfSingleDataset(
            "sfdr",
            "V23/euTaxonomyFinancialsOriginal.json",
            "V23/euTaxonomyFinancialsExpected.json",
            { dataTableEntity -> V23__MigratePageZeroToNull().migratePageFields(dataTableEntity) },
        )
    }

    @Test
    fun `check migration script for additional company information`() {
        TestUtils().testMigrationOfSingleDataset(
            "sfdr",
            "V23/additionalCompanyInformationOriginal.json",
            "V23/additionalCompanyInformationExpected.json",
            { dataTableEntity -> V23__MigratePageZeroToNull().migratePageFields(dataTableEntity) },
        )
    }

    @Test
    fun `check migration script for eu taxonomy non financials`() {
        TestUtils().testMigrationOfSingleDataset(
            "sfdr",
            "V23/euTaxonomyNonFinancialsOriginal.json",
            "V23/euTaxonomyNonFinancialsExpected.json",
            { dataTableEntity -> V23__MigratePageZeroToNull().migratePageFields(dataTableEntity) },
        )
    }

    @Test
    fun `check migration script for sfdr`() {
        TestUtils().testMigrationOfSingleDataset(
            "sfdr",
            "V23/sfdrOriginal.json",
            "V23/sfdrExpected.json",
            { dataTableEntity -> V23__MigratePageZeroToNull().migratePageFields(dataTableEntity) },
        )
    }
}
