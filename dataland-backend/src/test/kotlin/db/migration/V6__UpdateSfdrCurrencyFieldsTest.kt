package db.migration

import db.migration.utils.TestUtils
import org.junit.jupiter.api.Test

@Suppress("ClassName")
class V6__UpdateSfdrCurrencyFieldsTest {
    @Test
    fun `check renaming of extendedCurrencyTotalRevenue`() {
        TestUtils().testMigrationOfDataPointIdAndDataPointTypeEntity(
            "extendedCurrencyTotalRevenue",
            "extendedDecimalTotalRevenueInEUR",
            V6__UpdateSfdrCurrencyFields()
                ::updateRespectiveDataType,
        )
    }

    @Test
    fun `check renaming of extendedCurrencyEnterpriseValue`() {
        TestUtils().testMigrationOfDataPointIdAndDataPointTypeEntity(
            "extendedCurrencyEnterpriseValue",
            "extendedDecimalEnterpriseValueInEUR",
            V6__UpdateSfdrCurrencyFields()
                ::updateRespectiveDataType,
        )
    }

    @Test
    fun `check renaming of extendedDecimalCarbonFootprintInTonnesPerMillionEURRevenue`() {
        TestUtils().testMigrationOfDataPointIdAndDataPointTypeEntity(
            "extendedDecimalCarbonFootprintInTonnesPerMillionEURRevenue",
            "extendedDecimalCarbonFootprintInTonnesPerMillionEUREnterpriseValue",
            V6__UpdateSfdrCurrencyFields()
                ::updateRespectiveDataType,
        )
    }

    @Test
    fun `check that non-matching data points remain unchanged`() {
        TestUtils().testMigrationOfDataPointIdAndDataPointTypeEntity(
            "typeThatDoesntExist",
            "typeThatDoesntExist",
            V6__UpdateSfdrCurrencyFields()
                ::updateRespectiveDataType,
        )
    }
}
