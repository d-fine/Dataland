package org.dataland.datalandbackendutils.model

import org.dataland.datalandbackendutils.interfaces.BaseDimensions

/**
 * Data class that contains an instance of the base dimensions.
 * @param companyId unique identifier to identify the company the data is associated with
 * @param reportingPeriod the year for which the data was provided for
 */
data class BasicBaseDimensions(
    override val companyId: String,
    override val reportingPeriod: String,
) : BaseDimensions
