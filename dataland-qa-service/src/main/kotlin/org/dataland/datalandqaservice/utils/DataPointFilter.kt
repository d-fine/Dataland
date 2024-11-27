package org.dataland.datalandqaservice.org.dataland.datalandqaservice.utils

import org.dataland.datalandbackendutils.interfaces.DataPointDimensions

/**
 * A filter class used in the searching for specific QA entries
 */
data class DataPointFilter(
    override val companyId: String,
    override val dataPointIdentifier: String,
    override val reportingPeriod: String,
    val qaStatus: String,
) : DataPointDimensions
