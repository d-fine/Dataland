package db.migration

import db.migration.utils.TestUtils
import org.junit.jupiter.api.Test

@Suppress("ClassName")
class V22__IntroduceNewFieldsInSfdrTest {
    @Test
    fun `check migration script for SFDR one`() {
        TestUtils().testMigrationOfSingleDataset(
            "sfdr",
            "V22/originalSfdrOne.json",
            "V22/expectedSfdrOne.json",
            V22__IntroduceNewFieldsInSfdr()::migrateBoardFields,
        )
    }

    @Test
    fun `check migration script for SFDR two`() {
        TestUtils().testMigrationOfSingleDataset(
            "sfdr",
            "V22/originalSfdrTwo.json",
            "V22/expectedSfdrTwo.json",
            V22__IntroduceNewFieldsInSfdr()::migrateBoardFields,
        )
    }

    @Test
    fun `check migration script for SFDR three`() {
        TestUtils().testMigrationOfSingleDataset(
            "sfdr",
            "V22/originalSfdrThree.json",
            "V22/expectedSfdrThree.json",
            V22__IntroduceNewFieldsInSfdr()::migrateBoardFields,
        )
    }
}
