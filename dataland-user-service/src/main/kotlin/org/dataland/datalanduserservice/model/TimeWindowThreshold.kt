package org.dataland.datalanduserservice.model

/**
 * Enum class representing time window threshold for portfolio monitoring.
 *
 * Standard: Data requests cover the last 6 months
 * Extended: Data requests cover the last 16 months
 */
enum class TimeWindowThreshold {
    Standard,
    Extended,
}
