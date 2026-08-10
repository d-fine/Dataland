package org.dataland.datalandbackend.services.datapoints

/**
 * Configuration of the EU taxonomy activity groups that are relevant for the EU taxonomy share conversions.
 */
internal object EuTaxonomyRulesConfig {
    /**
     * The data point base type identifying the non-aligned activities input of an EU taxonomy rules.
     */
    const val NON_ALIGNED_ACTIVITIES_BASE_TYPE = "extendedEuTaxonomyNonAlignedActivitiesComponent"

    /**
     * The data point base type identifying the aligned activities input of an EU taxonomy rules.
     */
    const val ALIGNED_ACTIVITIES_BASE_TYPE = "extendedEuTaxonomyAlignedActivitiesComponent"
}
