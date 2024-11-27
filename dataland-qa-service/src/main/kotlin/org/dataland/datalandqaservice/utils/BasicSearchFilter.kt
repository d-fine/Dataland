package org.dataland.datalandqaservice.org.dataland.datalandqaservice.utils

/**
 * A filter class used in the searching for unreviewed datasets which allows
 * convenient usage of SEPL instructions in the query
 */
data class BasicSearchFilter(
    val dataType: String,
    val reportingPeriod: String,
    val companyId: String,
    val qaStatus: String,
)
