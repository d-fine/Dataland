package org.dataland.datasourcingservice.utils

/**
 * Utility object for data sourcing related operations.
 */
object DataSourcingUtils {
    /**
     * Calls the specified setter function on the specified new value as long as the new value is not null.
     * @param newValue the new value to set, or null if no update should be performed
     * @param setter the setter function to call if the new value is not null
     */
    fun <T> updateIfNotNull(
        newValue: T?,
        setter: (T) -> Unit,
    ) {
        newValue?.let { setter(it) }
    }
}
