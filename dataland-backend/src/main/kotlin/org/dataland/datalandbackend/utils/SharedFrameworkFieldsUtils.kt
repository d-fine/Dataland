package org.dataland.datalandbackend.utils

object SharedFrameworkFieldsUtils {
    private val sharedFields =
        setOf<String>(
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
    fun getSharedFields(): Set<String> = SharedFrameworkFieldsUtils.sharedFields
}
