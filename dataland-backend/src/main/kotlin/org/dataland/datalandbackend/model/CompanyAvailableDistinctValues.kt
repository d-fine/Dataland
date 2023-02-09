package org.dataland.datalandbackend.model

/**
 * --- API model ---
 * Class that returns all available countryCodes and sectors
 * @param countryCodes List of available countryCodes
 * @param sectors List of available sectors
 */
data class CompanyAvailableDistinctValues(
    val countryCodes: Set<String>,
    val sectors: Set<String>,
)
