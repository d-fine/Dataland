package db.migration

import db.migration.utils.TestUtils
import org.junit.jupiter.api.Test

@Suppress("ClassName")
class V25__UpdateSfdrModelTest {
    @Test
    fun `check migration script for SFDR one`() {
        TestUtils().testMigrationOfSingleDataset(
            "sfdr",
            "V25/originalSfdrOne.json",
            "V25/expectedSfdrOne.json",
            V25__UpdateSfdrModel()::migrateRateOfAccidents,
        )
    }

    @Test
    fun `check migration script for SFDR two`() {
        TestUtils().testMigrationOfSingleDataset(
            "sfdr",
            "V25/originalSfdrTwo.json",
            "V25/expectedSfdrTwo.json",
            V25__UpdateSfdrModel()::migrateRateOfAccidents,
        )
    }

    @Test
    fun `check migration script for SFDR three`() {
        TestUtils().testMigrationOfSingleDataset(
            "sfdr",
            "V25/originalSfdrThree.json",
            "V25/expectedSfdrThree.json",
            V25__UpdateSfdrModel()::migrateRateOfAccidents,
        )
    }
}
