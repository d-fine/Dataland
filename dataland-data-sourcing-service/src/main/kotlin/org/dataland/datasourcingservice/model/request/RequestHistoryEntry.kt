package org.dataland.datasourcingservice.model.request

import org.dataland.datasourcingservice.model.enums.DisplayedState

/**
 * An interface that holds the common fields of RequestHistoryEntryData and ExtendedRequestHistoryEntryData
 */
interface RequestHistoryEntry {
    val modificationDate: Long
    val displayedState: DisplayedState
}
