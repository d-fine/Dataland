package org.dataland.datalanduserservice.model

import org.dataland.datalanduserservice.model.enums.NotificationFrequency

/**
 * Interface for the Portfolio Monitoring API models
 */
interface PortfolioMonitoring {
    val isMonitored: Boolean
    val monitoredFrameworks: Set<String>
    val notificationFrequency: NotificationFrequency?
}
