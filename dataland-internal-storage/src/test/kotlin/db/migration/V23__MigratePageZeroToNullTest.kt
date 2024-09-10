package db.migration

import db.migration.utils.TestUtils
import org.junit.jupiter.api.Test

class V23__MigratePageZeroToNullTest {

    @Test
    fun `check migration script for page set to null where page is 0`() {
        TestUtils().testMigrationOfSingleDataset(
            "sfdr",
            "V23/originalSfdrOne.json",
            "V23/expectedSfdrOne.json",
            { dataTableEntity -> V23__MigratePageZeroToNull().migratePageFields(dataTableEntity, "sfdr") },
        )
    }

    @Test
    fun `check migration script for multiple page values set to null`() {
        TestUtils().testMigrationOfSingleDataset(
            "sfdr",
            "V23/originalSfdrMultiple.json",
            "V23/expectedSfdrMultiple.json",
            { dataTableEntity -> V23__MigratePageZeroToNull().migratePageFields(dataTableEntity, "sfdr") },
        )
    }

    @Test
    fun `check migration script for nested page field`() {
        TestUtils().testMigrationOfSingleDataset(
            "sfdr",
            "V23/originalSfdrNested.json",
            "V23/expectedSfdrNested.json",
            { dataTableEntity -> V23__MigratePageZeroToNull().migratePageFields(dataTableEntity, "sfdr") },
        )
    }

    @Test
    fun `check migration script for invalid page ranges`() {
        TestUtils().testMigrationOfSingleDataset(
            "sfdr",
            "V23/originalSfdrInvalidRanges.json",
            "V23/expectedSfdrInvalidRanges.json",
            { dataTableEntity -> V23__MigratePageZeroToNull().migratePageFields(dataTableEntity, "sfdr") },
        )
    }
}
