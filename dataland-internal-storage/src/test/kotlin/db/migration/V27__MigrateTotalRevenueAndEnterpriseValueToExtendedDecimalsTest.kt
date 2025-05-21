package db.migration

import db.migration.utils.TestUtils
import org.junit.jupiter.api.Test

@Suppress("ClassName")
class V27__MigrateTotalRevenueAndEnterpriseValueToExtendedDecimalsTest {
    @Test
    fun `check delection of currency field and renaming of extendedCurrencyTotalRevenue`() {
        TestUtils().testMigrationOfSingleDatapoint(
            "extendedCurrencyTotalRevenue",
            "extendedDecimalTotalRevenueInEUR",
            "V27/original.json",
            "V27/expected.json",
            V27__MigrateTotalRevenueAndEnterpriseValueToExtendedDecimals()::migrateCurrencyToDecimal,
        )
    }

    @Test
    fun `check delection of currency field and renaming of extendedCurrencyEnterpriseValue`() {
        TestUtils().testMigrationOfSingleDatapoint(
            "extendedCurrencyEnterpriseValue",
            "extendedDecimalEnterpriseValueInEUR",
            "V27/original.json",
            "V27/expected.json",
            V27__MigrateTotalRevenueAndEnterpriseValueToExtendedDecimals()::migrateCurrencyToDecimal,
        )
    }

    @Test
    fun `check that non-matching data points remain unchanged`() {
        TestUtils().testMigrationOfSingleDatapoint(
            "typeThatDoesntExist",
            "typeThatDoesntExist",
            "V27/original.json",
            "V27/original.json",
            V27__MigrateTotalRevenueAndEnterpriseValueToExtendedDecimals()::migrateCurrencyToDecimal,
        )
    }
}
