package org.dataland.datasourcingservice.utils

import org.dataland.datasourcingservice.entities.RequestEntity
import org.dataland.datasourcingservice.model.datasourcing.DataSourcingWithoutReferences
import org.dataland.datasourcingservice.model.enums.DataSourcingState
import org.dataland.datasourcingservice.model.enums.DisplayedState
import org.dataland.datasourcingservice.model.enums.RequestState
import org.dataland.datasourcingservice.model.request.ExtendedRequestHistoryEntry
import org.dataland.datasourcingservice.model.request.ExtendedRequestHistoryEntryData
import org.dataland.datasourcingservice.model.request.RequestHistoryEntry
import org.dataland.datasourcingservice.model.request.RequestHistoryEntryData

/**
 * Deletes consecutive entries with the same displayedState, keeping the order stable.
 *
 * @param entries - The combined history entries from which repeating displayed states should be removed.
 * @returns The filtered combined history entries with repeating displayed states removed.
 */
fun deleteRepeatingDisplayedStates(entries: List<RequestHistoryEntry>): List<RequestHistoryEntry> {
    if (entries.isEmpty()) {
        return entries
    }
    val result = mutableListOf<RequestHistoryEntry>()
    var lastDisplayedState: DisplayedState? = null

    for (entry in entries) {
        val currentDisplayedState = entry.displayedState
        if (currentDisplayedState != lastDisplayedState) {
            result.add(entry)
            lastDisplayedState = currentDisplayedState
        }
    }

    return result
}

/**
 * Determines the displayed state based on the request state and data sourcing state.
 * The displayed state is a user-friendly representation of the current status of the request,
 * taking into account both the request state and the data sourcing state.
 *
 * @param requestState - The current state of the request.
 * @param dataSourcingState - The current state of the data sourcing process, which can be null if not applicable.
 * @returns The calculated displayed state for the given request and data sourcing states.
 */
fun getDisplayedState(
    requestState: RequestState,
    dataSourcingState: DataSourcingState?,
): DisplayedState =
    when {
        requestState == RequestState.Withdrawn -> DisplayedState.Withdrawn
        requestState == RequestState.Open -> DisplayedState.Open
        dataSourcingState == null -> DisplayedState.Open
        else ->
            when (dataSourcingState) {
                DataSourcingState.Initialized -> DisplayedState.Validated
                DataSourcingState.DocumentSourcing -> DisplayedState.DocumentSourcing
                DataSourcingState.DocumentSourcingDone -> DisplayedState.DocumentVerification
                DataSourcingState.DataExtraction -> DisplayedState.DataExtraction
                DataSourcingState.DataVerification -> DisplayedState.DataVerification
                DataSourcingState.NonSourceable -> DisplayedState.NonSourceable
                DataSourcingState.Done -> DisplayedState.Done
            }
    }

/**
 * Combines the request history and data sourcing history into a unified list of ExtendedRequestHistoryEntry objects for admin view.
 * It iterates through both histories, comparing timestamps, and creates ExtendedRequestHistoryEntryData objects that include
 * both request state and data sourcing state information, as well as admin comments.
 * The resulting list is sorted by modification date and includes entries from both histories in chronological order.
 *
 * @param requestHistory - A list of pairs containing RequestEntity objects and their corresponding revision numbers,
 * representing the history of request state changes.
 * @param dataSourcingHistory - A list of DataSourcingWithoutReferences objects representing the history of data
 * sourcing state changes associated with the request.
 * @returns A list of ExtendedRequestHistoryEntry objects representing the combined history of request states and data
 * sourcing states for admin view, sorted by modification date.
 */
fun getExtendedRequestHistory(
    requestHistory: List<Pair<RequestEntity, Long>>,
    dataSourcingHistory: List<DataSourcingWithoutReferences>,
): List<ExtendedRequestHistoryEntry> {
    var requestHistorySorted = requestHistory.sortedBy { it.first.lastModifiedDate }
    var dataSourcingHistorySorted = dataSourcingHistory.sortedBy { it.lastModifiedDate }

    val requestStateHistory =
        buildList<ExtendedRequestHistoryEntry> {
            while (requestHistorySorted.isNotEmpty() || dataSourcingHistorySorted.isNotEmpty()) {
                val timeDifferenceBetweenNextRequestAndDataSourcingState =
                    (requestHistorySorted.getOrNull(0)?.first?.lastModifiedDate ?: Long.MAX_VALUE) -
                        (dataSourcingHistorySorted.getOrNull(0)?.lastModifiedDate ?: Long.MAX_VALUE)
                when {
                    timeDifferenceBetweenNextRequestAndDataSourcingState < 0 -> {
                        add(
                            ExtendedRequestHistoryEntryData(
                                requestHistorySorted[0].first,
                                lastOrNull()?.dataSourcingState,
                            ),
                        )
                        requestHistorySorted = requestHistorySorted.drop(1)
                    }
                    timeDifferenceBetweenNextRequestAndDataSourcingState > 0 -> {
                        add(
                            ExtendedRequestHistoryEntryData(
                                dataSourcingHistorySorted[0],
                                last().requestState,
                                last().adminComment,
                            ),
                        )
                        dataSourcingHistorySorted = dataSourcingHistorySorted.drop(1)
                    }
                    else -> {
                        add(
                            ExtendedRequestHistoryEntryData(
                                requestHistorySorted[0].first,
                                dataSourcingHistorySorted[0].state,
                            ),
                        )
                        requestHistorySorted = requestHistorySorted.drop(1)
                        dataSourcingHistorySorted = dataSourcingHistorySorted.drop(1)
                    }
                }
            }
        }
    return requestStateHistory
}

/**
 * Combines the request history and data sourcing history into a unified list of RequestHistoryEntry objects for regular view.
 * It iterates through both histories, comparing timestamps, and creates RequestHistoryEntryData objects that include
 * both request state and data sourcing state information for display.
 * The resulting list is sorted by modification date and includes entries from both histories in chronological order.
 *
 * @param requestHistory - A list of pairs containing RequestEntity objects and their corresponding revision numbers,
 * representing the history of request state changes.
 * @param dataSourcingHistory - A list of DataSourcingWithoutReferences objects representing the history of data
 * sourcing state changes associated with the request.
 * @returns A list of RequestHistoryEntry objects representing the combined history of request states and data
 * sourcing states for regular view, sorted by modification date.
 */
fun getRequestHistory(
    requestHistory: List<Pair<RequestEntity, Long>>,
    dataSourcingHistory: List<DataSourcingWithoutReferences>,
): List<RequestHistoryEntry> {
    var requestHistorySorted = requestHistory.sortedBy { it.first.lastModifiedDate }
    var dataSourcingHistorySorted = dataSourcingHistory.sortedBy { it.lastModifiedDate }

    val requestStateHistory =
        buildList<RequestHistoryEntry> {
            while (requestHistorySorted.isNotEmpty() || dataSourcingHistorySorted.isNotEmpty()) {
                val timeDifferenceBetweenNextRequestAndDataSourcingState =
                    (requestHistorySorted.getOrNull(0)?.first?.lastModifiedDate ?: Long.MAX_VALUE) -
                        (dataSourcingHistorySorted.getOrNull(0)?.lastModifiedDate ?: Long.MAX_VALUE)
                when {
                    timeDifferenceBetweenNextRequestAndDataSourcingState < 0 -> {
                        add(
                            RequestHistoryEntryData(requestHistorySorted[0].first),
                        )
                        requestHistorySorted = requestHistorySorted.drop(1)
                    } timeDifferenceBetweenNextRequestAndDataSourcingState > 0 -> {
                        add(
                            RequestHistoryEntryData(
                                dataSourcingHistorySorted[0],
                                last().displayedState,
                            ),
                        )
                        dataSourcingHistorySorted = dataSourcingHistorySorted.drop(1)
                    } else -> {
                        add(
                            RequestHistoryEntryData(
                                dataSourcingHistorySorted[0],
                                last().displayedState,
                            ),
                        )
                        requestHistorySorted = requestHistorySorted.drop(1)
                        dataSourcingHistorySorted = dataSourcingHistorySorted.drop(1)
                    }
                }
            }
        }

    return requestStateHistory
}
