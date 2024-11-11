package org.dataland.datalandcommunitymanager.services.messaging

/**
 * A class that provided utility for generating emails messages for data requests
 */
open class DataRequestEmailMessageSenderBase {
    // TODO delete this
    protected fun formatReportingPeriods(reportingPeriods: Set<String>) = reportingPeriods.toList().sorted().joinToString(", ")
}
