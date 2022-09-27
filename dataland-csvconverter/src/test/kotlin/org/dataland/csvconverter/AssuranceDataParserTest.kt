package org.dataland.csvconverter

import org.dataland.csvconverter.csv.commonfieldparsers.AssuranceDataParser
import org.dataland.csvconverter.csv.commonfieldparsers.CompanyReportParser
import org.dataland.csvconverter.csv.commonfieldparsers.DataPointParser
import org.dataland.csvconverter.csv.utils.EnumCsvParser
import org.dataland.datalandbackend.model.CompanyReportReference
import org.dataland.datalandbackend.model.enums.eutaxonomy.AssuranceOptions
import org.dataland.datalandbackend.model.enums.eutaxonomy.YesNoNa
import org.dataland.datalandbackend.model.eutaxonomy.AssuranceData
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

/**
 * Different functions which test the single Parser components
 */

class AssuranceDataParserTest {

    private val yesNoNaParser = EnumCsvParser(
        mapOf(
            "Yes" to YesNoNa.Yes,
            "No" to YesNoNa.No,
            "N/A" to YesNoNa.NA
        )
    )
    private val assuranceParser = AssuranceDataParser(DataPointParser(CompanyReportParser(yesNoNaParser)))

    private fun buildDataRow(
        assurance: String,
        assuranceProvider: String,
        assuranceReport: String,
        assurancePage: String
    ): Map<String, String> {
        return mapOf(
            "assurance" to assurance,
            "assurance provider" to assuranceProvider,
            "assurance report" to assuranceReport,
            "assurance page" to assurancePage
        )
    }

    @Test
    fun `test AssuranceDataParser`() {
        val row = buildDataRow("reasonable", "Baker", "Annual Report", "123")
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
    fun `test AssuranceDataParser with empty row`() {
        val rowWithNoAssurance = buildDataRow("", "", "", "")
        assertEquals(
            null,
            assuranceParser.buildSingleAssuranceData(rowWithNoAssurance),
        )
    }

    @Test
    fun `test AssuranceDataParser with missing arguments`() {
        val rowWithProviderOnly = buildDataRow("", "Baker", "", "")
        assertThrows<IllegalArgumentException> {
            assuranceParser.buildSingleAssuranceData(rowWithProviderOnly)
        }
    }
}
