package org.dataland.csvconverter

import org.dataland.csvconverter.csv.commonfieldparsers.AssuranceDataParser
import org.dataland.csvconverter.csv.commonfieldparsers.CompanyReportParser
import org.dataland.csvconverter.csv.commonfieldparsers.DataPointParser
import org.dataland.csvconverter.csv.utils.EnumCsvParser
import org.dataland.datalandbackend.model.CompanyReportReference
import org.dataland.datalandbackend.model.DataPoint
import org.dataland.datalandbackend.model.enums.data.QualityOptions
import org.dataland.datalandbackend.model.enums.eutaxonomy.AssuranceOptions
import org.dataland.datalandbackend.model.enums.eutaxonomy.YesNoNa
import org.dataland.datalandbackend.model.eutaxonomy.AssuranceData
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.math.BigDecimal

/**
 * Different functions which test the single Parser components
 */

class ParserUnitTests {

    private val yesNoNaParser = EnumCsvParser(
        mapOf(
            "Yes" to YesNoNa.Yes,
            "No" to YesNoNa.No,
            "N/A" to YesNoNa.NA
        )
    )
    private val companyReportParser = CompanyReportParser(yesNoNaParser)
    private val dataPointParser = DataPointParser(companyReportParser)
    private val assuranceParser = AssuranceDataParser(DataPointParser(CompanyReportParser(yesNoNaParser)))

    private val myParser = EnumCsvParser(
        mapOf(
            "1" to "one",
            "kekse" to "cookies"
        )
    )

    @Test
    fun `Test EnumCsvParser parse function`() {
        assertTrue(myParser.parse("Number", "1") == "one")
    }

    @Test
    fun `Test EnumCsvParser parseAllowingNull function`() {
        assertTrue(myParser.parseAllowingNull("Word", "kekse") == "cookies")
    }

    @Test
    fun `Test EnumCsvParser parse with illegal argument`() {
        assertThrows<IllegalArgumentException> { myParser.parse("Kekse", "keine Kekse") }
    }

    @Test
    fun `Test EnumCsvParser parse with no argument`() {
        assertThrows<IllegalArgumentException> { myParser.parse("Fehler", "") }
    }

    @Test
    fun `Test EnumCsvParser parseAllowingNull with illegal argument`() {
        assertThrows<IllegalArgumentException> { myParser.parse("Kekse", "keine Kekse") }
    }

    @Test
    fun `Test EnumCsvParser parseAllowingNull with no argument`() {
        assertTrue(myParser.parseAllowingNull("", null) == null)
    }

    @Test
    fun `Test that the company report reference parser works on valid data`() {
        val csvMapping = mapOf("rezept" to "recipe")
        val validDataRow = mapOf(
            "recipe" to "111",
            "recipe quality" to "Reported",
            "recipe report" to "Annual Report",
            "recipe page" to "123",
            "recipe comment" to "it's great"
        )
        assertEquals(
            dataPointParser.buildSingleCompanyReportReference(
                csvMapping, validDataRow, "rezept"
            ),
            CompanyReportReference(report = "AnnualReport", page = 123)
        )
    }

    @Test
    fun `Test that the company report parser returns null if no fields have been specified`() {
        val csvMapping = mapOf("rezept" to "recipe")
        val rowWithNoReport = mapOf(
            "recipe" to "",
            "recipe quality" to "",
            "recipe report" to "",
            "recipe page" to "",
            "recipe comment" to ""
        )
        assertEquals(
            dataPointParser.buildSingleCompanyReportReference(
                csvMapping, rowWithNoReport, "rezept"
            ),
            null
        )
    }

    @Test
    fun `Test that the company report parser throws an error when only partial data is supplied`() {
        val csvMapping = mapOf("rezept" to "recipe")
        val roWithNoReportButWithPage = mapOf(
            "recipe" to "",
            "recipe quality" to "",
            "recipe report" to "",
            "recipe page" to "123",
            "recipe comment" to ""
        )
        assertThrows<IllegalArgumentException> {
            dataPointParser.buildSingleCompanyReportReference(csvMapping, roWithNoReportButWithPage, "rezept")
        }
    }

    @Test
    fun `Test that the data point parser works when supplied with valid data`() {
        val csvMapping = mapOf("rezept" to "recipe")
        val validDataRow = mapOf(
            "recipe" to "111",
            "recipe quality" to "Reported",
            "recipe report" to "Annual Report",
            "recipe page" to "123",
            "recipe comment" to "it's great"
        )
        assertEquals(
            dataPointParser.buildDecimalDataPoint(
                csvMapping, validDataRow, "rezept", BigDecimal.ONE
            ),
            DataPoint(
                value = 111.toBigDecimal(), quality = QualityOptions.Reported,
                CompanyReportReference(report = "AnnualReport", page = 123), comment = "it's great"
            )
        )
    }

    @Test
    fun `Test that the data point parser returns null when no data is supplied`() {
        val csvMapping = mapOf("rezept" to "recipe")
        val rowWithNoData = mapOf(
            "recipe" to "",
            "recipe quality" to "",
            "recipe report" to "",
            "recipe page" to "",
            "recipe comment" to ""
        )
        assertEquals(
            dataPointParser.buildDecimalDataPoint(
                csvMapping, rowWithNoData, "rezept", BigDecimal.ONE
            ),
            null
        )
    }

    @Test
    fun `Test that the data point parser returns null when mandatory values are left out`() {
        val csvMapping = mapOf("rezept" to "recipe")
        val rowWithValueOnly = mapOf(
            "recipe" to "111",
            "recipe quality" to "",
            "recipe report" to "",
            "recipe page" to "",
            "recipe comment" to ""
        )
        val rowWithPageOnly = mapOf(
            "recipe" to "",
            "recipe quality" to "",
            "recipe report" to "",
            "recipe page" to "123",
            "recipe comment" to ""
        )
        assertThrows<IllegalArgumentException> {
            dataPointParser.buildDecimalDataPoint(csvMapping, rowWithValueOnly, "rezept", BigDecimal.ONE)
        }
        assertThrows<IllegalArgumentException> {
            dataPointParser.buildDecimalDataPoint(csvMapping, rowWithPageOnly, "rezept", BigDecimal.ONE)
        }
    }

    @Test
    fun `Test AssuranceDataParser`() {
        val row = mapOf(
            "assurance" to "reasonable",
            "assurance provider" to "Baker",
            "assurance report" to "Annual Report",
            "assurance page" to "123"
        )
        assertEquals(
            AssuranceData(
                assurance = AssuranceOptions.ReasonableAssurance,
                provider = "Baker", CompanyReportReference(report = "AnnualReport", page = 123)
            ),
            assuranceParser
                .buildSingleAssuranceData(row),
        )
    }

    @Test
    fun `Test AssuranceDataParser with empty row`() {
        val rowWithNoAssurance = mapOf(
            "assurance" to "",
            "assurance provider" to "",
            "assurance report" to "",
            "assurance page" to "",
        )
        assertEquals(
            null,
            assuranceParser.buildSingleAssuranceData(rowWithNoAssurance),
        )
    }

    @Test
    fun `Test AssuranceDataParser with missing arguments`() {
        val rowWithProviderOnly = mapOf(
            "assurance" to "",
            "assurance provider" to "Baker",
            "assurance report" to "",
            "assurance page" to ""
        )

        assertThrows<IllegalArgumentException> {
            assuranceParser.buildSingleAssuranceData(rowWithProviderOnly)
        }
    }
}
