package org.dataland.csvconverter.csv

import org.dataland.csvconverter.csv.CsvUtils.getCsvValue
import org.dataland.datalandbackend.model.enums.eutaxonomy.AssuranceOptions
import org.dataland.datalandbackend.model.enums.eutaxonomy.YesNo

/**
 * This class provides parsing methods for columns that are required by both EU-Taxonomy frameworks
 * but don't belong to the companyInformation parser
 */
class EuTaxonomyCommonFieldParser {

    companion object {
        private const val REPORT_OBLIGATION_YES = "Yes"
        private const val REPORT_OBLIGATION_NO = "No"
        private const val REPORT_OBLIGATION_NA = "n/a"

        private const val ATTESTATION_REASONABLE = "reasonable"
        private const val ATTESTATION_LIMITED = "limited"
        private const val ATTESTATION_NA = "n/a"
        private const val ATTESTATION_NONE = "none"
    }

    private val columnMappingEuTaxonomyUtils = mapOf(
        "reportObligation" to "NFRD mandatory",
        "attestation" to "Assurance",
        "companyType" to "IS/FS",
    )

    /**
     * This function parses the reportingObligation field from the EU-Taxonomy framework CSV file
     */
    fun getReportingObligation(csvLineData: Map<String, String>): YesNo {
        return when (
            val rawReportObligation = columnMappingEuTaxonomyUtils.getCsvValue("reportObligation", csvLineData)
        ) {
            REPORT_OBLIGATION_YES -> YesNo.Yes
            REPORT_OBLIGATION_NO, REPORT_OBLIGATION_NA -> YesNo.No
            else -> {
                throw java.lang.IllegalArgumentException(
                    "Could not determine reportObligation: Found $rawReportObligation, " +
                        "but expect one of $REPORT_OBLIGATION_YES, $REPORT_OBLIGATION_NO or $REPORT_OBLIGATION_NA"
                )
            }
        }
    }

    /**
     * This function parses the attestation field from the EU-Taxonomy framework CSV file
     */
    fun getAttestation(csvLineData: Map<String, String>): AssuranceOptions {
        return when (
            val rawAttestation = columnMappingEuTaxonomyUtils.getCsvValue("attestation", csvLineData)
        ) {
            ATTESTATION_REASONABLE -> AssuranceOptions.ReasonableAssurance
            ATTESTATION_LIMITED -> AssuranceOptions.LimitedAssurance
            ATTESTATION_NA, ATTESTATION_NONE -> AssuranceOptions.None
            else -> {
                throw java.lang.IllegalArgumentException(
                    "Could not determine attestation: Found $rawAttestation, " +
                        "but expect one of $ATTESTATION_REASONABLE, $ATTESTATION_LIMITED, " +
                        "$ATTESTATION_NA or $ATTESTATION_NONE "
                )
            }
        }
    }

    /**
     * Returns the relevant company type for the EU-Taxonomy framework (IS/FS)
     */
    fun getCompanyType(csvLineData: Map<String, String>): String {
        return columnMappingEuTaxonomyUtils.getCsvValue("companyType", csvLineData)
    }
}
