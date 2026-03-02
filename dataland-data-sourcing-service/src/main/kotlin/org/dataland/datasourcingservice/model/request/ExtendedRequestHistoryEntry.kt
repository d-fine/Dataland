package org.dataland.datasourcingservice.model.request

import org.dataland.datasourcingservice.model.enums.DataSourcingState
import org.dataland.datasourcingservice.model.enums.RequestState

/**
 * A class that holds the combined states of a request and its associated data sourcing entry, used for the "mixedState"
 */
interface ExtendedRequestHistoryEntry : RequestHistoryEntry {
    val requestState: RequestState
    val dataSourcingState: DataSourcingState?
    val adminComment: String?
}
