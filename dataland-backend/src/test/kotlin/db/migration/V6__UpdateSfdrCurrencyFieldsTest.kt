package db.migration

import db.migration.utils.TestUtilsBackendMigration
import org.junit.jupiter.api.Test

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
}
