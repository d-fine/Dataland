package org.dataland.datalandbackend.model

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * --- API model ---
 * Interface of the generic base data point
 */
interface BaseDataPointInterface<T> {
    val value: T?
    val dataSource: DocumentReference?
}
//TODO separate those into separete files
/**
 * --- API model ---
 * Fields of a generic base data point and its source
 */
data class BaseDataPoint<T>(
    @field:JsonProperty(required = true)
    override val value: T?,

    override val dataSource: DocumentReference? = null,
): BaseDataPointInterface<T>
