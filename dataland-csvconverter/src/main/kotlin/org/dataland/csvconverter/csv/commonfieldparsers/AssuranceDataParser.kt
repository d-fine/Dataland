package org.dataland.csvconverter.csv.commonfieldparsers

import org.dataland.csvconverter.csv.CsvUtils.checkIfAnyFieldHasValue
import org.dataland.csvconverter.csv.CsvUtils.getCsvValueAllowingNull
import org.dataland.csvconverter.csv.utils.EnumCsvParser
import org.dataland.datalandbackend.model.enums.eutaxonomy.AssuranceOptions
import org.dataland.datalandbackend.model.eutaxonomy.AssuranceData

/**
 * This class provides the methods to retrieve assurance data from a csv row
 */
class AssuranceDataParser(private val dataPointParser: DataPointParser) {

    /**
     * parses one single AssuranceData (if existing) based on the functions of the DataPointParser
     */

    private val columnMappingAssurance = mapOf(
        "assurance" to "Assurance",
        "assuranceProvider" to "Assurance Provider",
        "assurancePage" to "Assurance Page",
        "assuranceReport" to "Assurance Report",
    )

    private val assuranceOptionsParser = EnumCsvParser<AssuranceOptions>(
        mapOf(
            "reasonable" to AssuranceOptions.ReasonableAssurance,
            "limited" to AssuranceOptions.LimitedAssurance,
            "none" to AssuranceOptions.None,
        ),
    )

    /**
     * This method builds the single assurance data
     */
    fun buildSingleAssuranceData(row: Map<String, String>): AssuranceData? {
        if (!columnMappingAssurance.checkIfAnyFieldHasValue(columnMappingAssurance.keys.toList(), row)) {
            return null
        }

        return AssuranceData(
            assurance = columnMappingAssurance
                .getCsvValueAllowingNull("assurance", row)
                .let { assuranceOptionsParser.parse("Assurance", it) },
            provider = columnMappingAssurance.getCsvValueAllowingNull("assuranceProvider", row),
            dataSource = dataPointParser.buildSingleCompanyReportReference(
                columnMappingAssurance,
                row,
                "assurance",
            ),
        )
    }
}
