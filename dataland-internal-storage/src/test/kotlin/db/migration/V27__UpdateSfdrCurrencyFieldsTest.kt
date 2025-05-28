package db.migration

import db.migration.utils.TestUtils
import org.junit.jupiter.api.Test

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
}
