package org.dataland.datalandbackend.model.gdv

data class YearlyTimeseriesData<ContentDataType>(
    val currentYear: Int,
    val yearlyData: Map<Int, ContentDataType>,
)
