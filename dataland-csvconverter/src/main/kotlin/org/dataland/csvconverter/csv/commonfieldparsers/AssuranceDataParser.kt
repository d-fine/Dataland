package org.dataland.csvconverter.csv.commonfieldparsers

import org.dataland.csvconverter.csv.CsvUtils.checkIfFieldHasValue
import org.dataland.csvconverter.csv.CsvUtils.getCsvValue
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
        "assurance" to "Assurance"
    )

    private val assuranceOptionsMapping = mapOf(
        "reasonable" to AssuranceOptions.ReasonableAssurance,
        "limited" to AssuranceOptions.LimitedAssurance,
        "none" to AssuranceOptions.None
    )


    /**
     * This method builds the single assurance data
     */
    fun buildSingleAssuranceData(row: Map<String, String>): AssuranceData? {
        val baseString = "assurance"
        val generalMap = columnMappingAssurance
        return if (dataPointParser.buildMapForSpecificData(generalMap, baseString).checkIfFieldHasValue(
                baseString, row
            )
        ) {
            AssuranceData(
                assurance = assuranceOptionsMapping[dataPointParser.buildMapForSpecificData(generalMap, baseString).getCsvValue(baseString, row)]
                    ?: throw IllegalArgumentException(
                    "Expected an AssuranceOption but found" +
                        " ${dataPointParser.buildMapForSpecificData(generalMap,baseString).getCsvValue(baseString, row)}" +
                        "which is not a valid Quality Option"
                ),
                provider = dataPointParser.buildMapForSpecificData(generalMap, baseString).getCsvValue(
                    "${baseString}provider", row
                ),
                dataSource = dataPointParser.buildSingleCompanyReportReference(generalMap, row, baseString)
            )
        } else {
            null
        }
    }
}
