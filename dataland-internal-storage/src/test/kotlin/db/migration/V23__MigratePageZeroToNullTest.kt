package db.migration

import db.migration.utils.TestUtils
import org.junit.jupiter.api.Test

class V23__MigratePageZeroToNullTest {

    @Test
    fun `check migration script for page set to null where page is 0`() {
        TestUtils().testMigrationOfSingleDataset(
            "sfdr",
            "V23/originalSfdrOne.json", // Original dataset where page is 0
            "V23/expectedSfdrOne.json", // Expected dataset with page set to null
            V23__MigratePageZeroToNull()::migratePageFields,
        )
    }

    @Test
    fun `check migration script for multiple page values set to null`() {
        TestUtils().testMigrationOfSingleDataset(
            "sfdr",
            "V23/originalSfdrMultiple.json", // Original dataset with multiple invalid page values
            "V23/expectedSfdrMultiple.json", // Expected dataset where invalid page values are set to null
            V23__MigratePageZeroToNull()::migratePageFields,
        )
    }

    @Test
    fun `check migration script for nested page field`() {
        TestUtils().testMigrationOfSingleDataset(
            "sfdr",
            "V23/originalSfdrNested.json", // Original dataset where nested page is 0 or invalid
            "V23/expectedSfdrNested.json", // Expected dataset where nested page is set to null
            V23__MigratePageZeroToNull()::migratePageFields,
        )
    }

    @Test
    fun `check migration script for valid page ranges`() {
        TestUtils().testMigrationOfSingleDataset(
            "sfdr",
            "V23/originalSfdrValidRanges.json", // Original dataset with valid page ranges
            "V23/expectedSfdrValidRanges.json", // Expected dataset where valid ranges remain unchanged
            V23__MigratePageZeroToNull()::migratePageFields,
        )
    }

    @Test
    fun `check migration script for invalid page ranges`() {
        TestUtils().testMigrationOfSingleDataset(
            "sfdr",
            "V23/originalSfdrInvalidRanges.json", // Original dataset with invalid page ranges
            "V23/expectedSfdrInvalidRanges.json", // Expected dataset where invalid page ranges are set to null
            V23__MigratePageZeroToNull()::migratePageFields,
        )
    }
}
