package org.dataland.datasourcingservice.model.request

import org.dataland.datasourcingservice.model.enums.DisplayedState

/**
 * A class that holds the combined states of a request and its associated data sourcing entry, used for the "mixedState"
 */
interface BasicStateHistoryEntry {
    val modificationDate: Long
    val displayedState: DisplayedState
}
