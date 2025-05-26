package db.migration

import db.migration.utils.DataPointTableEntityMigration
import db.migration.utils.TestUtils
import org.flywaydb.core.api.migration.Context
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.sql.Connection
import java.sql.DatabaseMetaData
import java.sql.ResultSet

@Suppress("ClassName")
class V27__UpdateSfdrCurrencyFieldsTest {
    val original = "V27/original.json"

    @Test
    fun `check deletion of currency field and renaming of extendedCurrencyTotalRevenue`() {
        TestUtils().testMigrationOfSingleDatapoint(
            "extendedCurrencyTotalRevenue",
            "extendedDecimalTotalRevenueInEUR",
            "V27/original.json",
            "V27/expected.json",
            V27__UpdateSfdrCurrencyFields()
                ::updateCurrencyFieldsToDecimals,
        )
    }

    @Test
    fun `check deletion of currency field and renaming of extendedCurrencyEnterpriseValue`() {
        TestUtils().testMigrationOfSingleDatapoint(
            "extendedCurrencyEnterpriseValue",
            "extendedDecimalEnterpriseValueInEUR",
            original,
            "V27/expected.json",
            V27__UpdateSfdrCurrencyFields()
                ::updateCurrencyFieldsToDecimals,
        )
    }

    @Test
    fun `check renaming of extendedDecimalCarbonFootprintInTonnesPerMillionEURRevenue`() {
        TestUtils().testMigrationOfSingleDatapoint(
            "extendedDecimalCarbonFootprintInTonnesPerMillionEURRevenue",
            "extendedDecimalCarbonFootprintInTonnesPerMillionEUREnterpriseValue",
            original,
            original,
            V27__UpdateSfdrCurrencyFields()
                ::updateCarbonFootprint,
        )
    }

    @Test
    fun `check that non matching data points remain unchanged`() {
        TestUtils().testMigrationOfSingleDatapoint(
            "typeThatDoesntExist",
            "typeThatDoesntExist",
            original,
            original,
            V27__UpdateSfdrCurrencyFields()
                ::updateCurrencyFieldsToDecimals,
        )
    }

    @Test
    fun `migrate calls migrateDataPointTableEntities if table exists`() {
        val mockContext = mock<Context>()
        val mockConnection = mock<Connection>()
        val mockMetaData = mock<DatabaseMetaData>()
        val mockResultSet = mock<ResultSet>()
        val mockMigration = mock<DataPointTableEntityMigration>()

        whenever(mockContext.connection).thenReturn(mockConnection)
        whenever(mockConnection.metaData).thenReturn(mockMetaData)
        whenever(mockMetaData.getTables(null, null, "data_point_meta_information", null))
            .thenReturn(mockResultSet)
        whenever(mockResultSet.next()).thenReturn(true)

        val migration = V27__UpdateSfdrCurrencyFields(mockMigration)
        migration.migrate(mockContext)

        verify(mockMigration).migrateDataPointTableEntities(
            eq(mockContext),
            eq("extendedCurrencyTotalRevenue"),
            any(),
        )
        verify(mockMigration).migrateDataPointTableEntities(
            eq(mockContext),
            eq("extendedCurrencyEnterpriseValue"),
            any(),
        )
        verify(mockMigration).migrateDataPointTableEntities(
            eq(mockContext),
            eq("extendedDecimalCarbonFootprintInTonnesPerMillionEURRevenue"),
            any(),
        )
    }
}
