package db.migration

import db.migration.utils.TestUtils
import org.junit.jupiter.api.Test

@Suppress("ClassName")
class V26__UpdateSfdrModelTest {
    @Test
    fun `check rate of accidents migration for SFDR one`() {
        TestUtils().testMigrationOfSingleDataset(
            "sfdr",
            "V25/originalSfdrOne.json",
            "V25/expectedSfdrOne.json",
            V26__UpdateSfdrModel()::migrateRateOfAccidents,
        )
    }

    @Test
    fun `check rate of accidents migration for SFDR two`() {
        TestUtils().testMigrationOfSingleDataset(
            "sfdr",
            "V25/originalSfdrTwo.json",
            "V25/expectedSfdrTwo.json",
            V26__UpdateSfdrModel()::migrateRateOfAccidents,
        )
    }

    @Test
    fun `check excessive ceo pay gap ratio migration for SFDR three`() {
        TestUtils().testMigrationOfSingleDataset(
            "sfdr",
            "V25/originalSfdrThree.json",
            "V25/expectedSfdrThree.json",
            V26__UpdateSfdrModel()::migrateExcessiveCeoPayGapRatio,
        )
    }

    @Test
    fun `check excessive ceo pay gap ratio migration for SFDR four`() {
        TestUtils().testMigrationOfSingleDataset(
            "sfdr",
            "V25/originalSfdrFour.json",
            "V25/expectedSfdrFour.json",
            V26__UpdateSfdrModel()::migrateExcessiveCeoPayGapRatio,
        )
    }

    @Test
    fun `check excessive ceo pay gap ratio migration for SFDR five`() {
        TestUtils().testMigrationOfSingleDataset(
            "sfdr",
            "V25/originalSfdrFive.json",
            "V25/expectedSfdrFive.json",
            V26__UpdateSfdrModel()::migrateExcessiveCeoPayGapRatio,
        )
    }

    @Test
    fun `check excessive ceo pay gap ratio migration for SFDR six`() {
        TestUtils().testMigrationOfSingleDataset(
            "sfdr",
            "V25/originalSfdrSix.json",
            "V25/expectedSfdrSix.json",
            V26__UpdateSfdrModel()::migrateExcessiveCeoPayGapRatio,
        )
    }
}
