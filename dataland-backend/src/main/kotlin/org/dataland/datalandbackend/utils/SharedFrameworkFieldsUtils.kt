package org.dataland.datalandbackend.utils

object SharedFrameworkFieldsUtils {
    private val sharedFields =
        setOf(
            "customEnumEuTaxonomyReportingAssurance",
            "extendedDateFiscalYearEnd",
            "extendedDecimalNumberOfEmployees",
            "extendedEnumFiscalYearDeviation",
            "extendedEnumYesNoHumanRightsDueDiligence",
            "extendedEnumYesNoIloCoreLabourStandards",
            "extendedEnumYesNoUnGlobalCompactPrinciplesCompliancePolicy",
            "general.general.referencedReports",
        )

    /**
     * Return a list of data point types that are not unique to a single framework.
     */
    @JvmStatic
    fun getSharedFields(): Set<String> = sharedFields
}
