package org.dataland.datalanduserservice.model

/**
 * Interface for the Portfolio Monitoring API models
 */
interface PortfolioMonitoring {
    val isMonitored: Boolean
    val startingMonitoringPeriod: String?
    val monitoredFrameworks: Set<String>?
}
