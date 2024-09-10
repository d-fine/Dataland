package db.migration

import db.migration.utils.TestUtils
import org.junit.jupiter.api.Test

class V23__MigratePageZeroToNullTest {

    @Test
    fun `check migration script for page set to null where page is 0`() {
        TestUtils().testMigrationOfSingleDataset(
            "sfdr",
            "V23/euTaxonomyFinancialsOriginal.json",
            "V23/euTaxonomyFinancialsExpected.json",
            { dataTableEntity -> V23__MigratePageZeroToNull().migratePageFields(dataTableEntity, "sfdr") },
        )
    }

    @Test
    fun `check migration script for multiple page values set to null`() {
        TestUtils().testMigrationOfSingleDataset(
            "sfdr",
            "V23/additionalCompanyInformationOriginal.json",
            "V23/additionalCompanyInformationExpected.json",
            { dataTableEntity -> V23__MigratePageZeroToNull().migratePageFields(dataTableEntity, "sfdr") },
        )
    }

    @Test
    fun `check migration script for nested page field`() {
        TestUtils().testMigrationOfSingleDataset(
            "sfdr",
            "V23/euTaxonomyNonFinancialsOriginal.json",
            "V23/euTaxonomyNonFinancialsExpected.json",
            { dataTableEntity -> V23__MigratePageZeroToNull().migratePageFields(dataTableEntity, "sfdr") },
        )
    }

    @Test
    fun `check migration script for invalid page ranges`() {
        TestUtils().testMigrationOfSingleDataset(
            "sfdr",
            "V23/sfdrOriginal.json",
            "V23/sfdrExpected.json",
            { dataTableEntity -> V23__MigratePageZeroToNull().migratePageFields(dataTableEntity, "sfdr") },
        )
    }
}
