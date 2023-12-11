package org.dataland.datalandbackend.model.gdv

/**
 * Used for gdv fields where some content for the current year is stored together with historical data for years before
 * and forecasted data for years after that current year point of time.
 */
data class YearlyTimeseriesData<ContentDataType>(
    val currentYear: Int,
    val yearlyData: Map<Int, ContentDataType>,
)

// TODO Emanuel: Is there a reason that this file is not in the package "frameworks/gdv/model..."?
// TODO Emanuel: Probably because that one is deleted and re-generated on Main-execution?  How tackle this? => Discuss
