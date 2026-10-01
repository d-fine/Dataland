package db.migration

import db.migration.utils.TestUtils
import org.junit.jupiter.api.Test

@Suppress("ClassName")
class V34__RenameDeleteAndAddSfdrFieldsTest {
    @Test
    fun `check migration script for SFDR renames, deletions, and restructuring`() {
        TestUtils().testMigrationOfSingleDataset(
            "sfdr",
            "V34/originalSfdrOne.json",
            "V34/expectedSfdrOne.json",
            V34__RenameDeleteAndAddSfdrFields()::migrateSfdrData,
        )
    }
}
