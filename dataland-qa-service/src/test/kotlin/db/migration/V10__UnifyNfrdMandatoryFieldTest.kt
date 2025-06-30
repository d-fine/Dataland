package db.migration

import db.migration.utils.BaseDatabaseMigrationTest
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@Suppress("ClassName")
class V10__UnifyNfrdMandatoryFieldTest : BaseDatabaseMigrationTest() {
    private val migration = V10__UnifyNfrdMandatoryField()

    @Test
    fun `check that migration does not start if tables are missing`() {
        whenever(
            mockMetaData.getTables(
                null,
                null,
                "data_point_qa_review",
                null,
            ),
        ).thenReturn(mockResultSet)
        whenever(mockResultSet.next()).thenReturn(false)

        migration.migrate(mockContext)

        verify(mockConnection, never()).prepareStatement(any<String>())
    }

    @Test
    fun `sample check that migrateNfrdMandatoryField updates extendedEnumYesNoNfrdMandatory correctly`() {
        whenever(mockConnection.createStatement()).thenReturn(mockStatement)
        whenever(mockStatement.executeUpdate(any<String>())).thenReturn(2)

        migration.migrateNfrdMandatoryField(mockContext, "data_point_qa_review")

        verify(mockStatement).executeUpdate(
            "UPDATE data_point_qa_review SET data_point_type = 'extendedEnumYesNoIsNfrdMandatory' " +
                "WHERE data_point_type = 'extendedEnumYesNoNfrdMandatory'",
        )
        verify(mockStatement).close()
    }
}
