package org.dataland.datalandbackend.interfaces.export

/**
 * Interface for export requests that specify reporting periods.
 */
interface ExportRequestData : ExportLatestRequestData {
    val reportingPeriods: List<String>
}
