package org.dataland.datalandbackend.utils

object SharedFrameworkFieldsUtils {
    private val sharedFields =
        setOf(
            "customEnumEuTaxonomyReportingAssurance",
            "extendedDateFiscalYearEnd",
            "extendedDecimalNumberOfEmployees",
            "extendedEnumFiscalYearDeviation",
            "extendedEnumYesNoIsNfrdMandatory",
            "extendedEnumYesNoHumanRightsDueDiligence",
            "extendedEnumYesNoIloCoreLabourStandards",
            "extendedEnumYesNoOecdGuidelinesForMultinationalEnterprisesCompliancePolicy",
            "extendedEnumYesNoUnGlobalCompactPrinciplesCompliancePolicy",
            "general.general.referencedReports",
        )

    /**
     * Return a list of data point types that are not unique to a single framework.
     */
    @JvmStatic
    fun getSharedFields(): Set<String> = sharedFields
}
