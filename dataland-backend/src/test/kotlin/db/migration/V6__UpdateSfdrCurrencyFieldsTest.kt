package db.migration

import db.migration.utils.DataPointIdAndDataPointTypeMigration
import db.migration.utils.TestUtilsBackendMigration
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
class V6__UpdateSfdrCurrencyFieldsTest {
    @Test
    fun `check renaming of extendedCurrencyTotalRevenue`() {
        TestUtilsBackendMigration().testMigrationOfDataPointIdAndDataPointTypeEntity(
            "extendedCurrencyTotalRevenue",
            "extendedDecimalTotalRevenueInEUR",
            V6__UpdateSfdrCurrencyFields()
                ::updateRespectiveDataType,
        )
    }

    @Test
    fun `check renaming of extendedCurrencyEnterpriseValue`() {
        TestUtilsBackendMigration().testMigrationOfDataPointIdAndDataPointTypeEntity(
            "extendedCurrencyEnterpriseValue",
            "extendedDecimalEnterpriseValueInEUR",
            V6__UpdateSfdrCurrencyFields()
                ::updateRespectiveDataType,
        )
    }

    @Test
    fun `check renaming of extendedDecimalCarbonFootprintInTonnesPerMillionEURRevenue`() {
        TestUtilsBackendMigration().testMigrationOfDataPointIdAndDataPointTypeEntity(
            "extendedDecimalCarbonFootprintInTonnesPerMillionEURRevenue",
            "extendedDecimalCarbonFootprintInTonnesPerMillionEUREnterpriseValue",
            V6__UpdateSfdrCurrencyFields()
                ::updateRespectiveDataType,
        )
    }

    @Test
    fun `check that non matching data points remain unchanged`() {
        TestUtilsBackendMigration().testMigrationOfDataPointIdAndDataPointTypeEntity(
            "typeThatDoesntExist",
            "typeThatDoesntExist",
            V6__UpdateSfdrCurrencyFields()
                ::updateRespectiveDataType,
        )
    }

    @Test
    fun `migrate calls migrateDataPointIdsAndDataPointTypes if table exists`() {
        val mockContext = mock<Context>()
        val mockConnection = mock<Connection>()
        val mockMetaData = mock<DatabaseMetaData>()
        val mockResultSet = mock<ResultSet>()
        val mockMigration = mock<DataPointIdAndDataPointTypeMigration>()

        whenever(mockContext.connection).thenReturn(mockConnection)
        whenever(mockConnection.metaData).thenReturn(mockMetaData)
        whenever(
            mockMetaData.getTables(
                null,
                null,
                "data_point_meta_information",
                null,
            ),
        ).thenReturn(mockResultSet)
        whenever(mockResultSet.next()).thenReturn(true)

        val migration = V6__UpdateSfdrCurrencyFields(mockMigration)
        migration.migrate(mockContext)

        verify(mockMigration).migrateDataPointIdsAndDataPointTypes(
            eq(mockContext),
            eq("extendedCurrencyTotalRevenue"),
            any(),
        )
        verify(mockMigration).migrateDataPointIdsAndDataPointTypes(
            eq(mockContext),
            eq("extendedCurrencyEnterpriseValue"),
            any(),
        )
        verify(mockMigration).migrateDataPointIdsAndDataPointTypes(
            eq(mockContext),
            eq("extendedDecimalCarbonFootprintInTonnesPerMillionEURRevenue"),
            any(),
        )
    }
}
