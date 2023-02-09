package org.dataland.csvconverter

import org.dataland.csvconverter.csv.commonfieldparsers.AssuranceDataParser
import org.dataland.csvconverter.csv.commonfieldparsers.CompanyReportParser
import org.dataland.csvconverter.csv.commonfieldparsers.DataPointParser
import org.dataland.csvconverter.csv.utils.YesNoNaParser
import org.dataland.datalandbackend.model.CompanyReportReference
import org.dataland.datalandbackend.model.enums.eutaxonomy.AssuranceOptions
import org.dataland.datalandbackend.model.eutaxonomy.AssuranceData
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

/**
 * Different functions which test the single Parser components
 */

class AssuranceDataParserTest {

    private val assuranceParser = AssuranceDataParser(DataPointParser(CompanyReportParser(YesNoNaParser())))

    private fun buildDataRow(
        assurance: String,
        assuranceProvider: String,
        assuranceReport: String,
        assurancePage: String,
        assuranceTag: String,
    ): Map<String, String> {
        return mapOf(
            "assurance" to assurance,
            "assurance provider" to assuranceProvider,
            "assurance report" to assuranceReport,
            "assurance page" to assurancePage,
            "assurance tag" to assuranceTag,
        )
    }

    @Test
    fun `test AssuranceDataParser`() {
        val row = buildDataRow("reasonable", "Baker", "Annual Report", "123", "here")
        assertEquals(
            AssuranceData(
                assurance = AssuranceOptions.ReasonableAssurance,
                provider = "Baker",
                CompanyReportReference(report = "AnnualReport", page = 123, tagName = "here"),
            ),
            assuranceParser
                .buildSingleAssuranceData(row),
        )
    }

    @Test
    fun `test AssuranceDataParser with empty row`() {
        val rowWithNoAssurance = buildDataRow("", "", "", "", "")
        assertEquals(
            null,
            assuranceParser.buildSingleAssuranceData(rowWithNoAssurance),
        )
    }

    @Test
    fun `test AssuranceDataParser with missing arguments`() {
        val rowWithProviderOnly = buildDataRow("", "Baker", "", "", "")
        assertThrows<IllegalArgumentException> {
            assuranceParser.buildSingleAssuranceData(rowWithProviderOnly)
        }
    }
}
