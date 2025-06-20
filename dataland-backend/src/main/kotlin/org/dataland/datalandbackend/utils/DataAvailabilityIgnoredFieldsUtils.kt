package org.dataland.datalandbackend.utils

/**
 * Utility functions for the exclusion list of data point types that are ignored when asking whether data for a
 * particular data dimension is available.
 */
object DataAvailabilityIgnoredFieldsUtils {
    private val dataAvailabilityIgnoredFields =
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
        )

    /**
     * Return a list of data point types that are not unique to a single framework.
     */
    @JvmStatic
    fun getIgnoredFields(): Set<String> = dataAvailabilityIgnoredFields

    /**
     * Checks if a list of data point types contains at least element not part of the exclusion list
     */
    @JvmStatic
    fun containsNonIgnoredDataPoints(dataPointTypes: Collection<String>): Boolean = dataPointTypes.subtract(getIgnoredFields()).isNotEmpty()
}
