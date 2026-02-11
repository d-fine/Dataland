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
import org.dataland.datasourcingservice.services.ExistingRequestsManager.CombinedHistoryEntryDefault

/**
 * Deletes consecutive entries with the same displayedState, keeping the order stable.
 *
 * @param entries - The combined history entries from which repeating displayed states should be removed.
 * @returns The filtered combined history entries with repeating displayed states removed.
 */
fun deleteRepeatingDisplayedStates(entries: List<CombinedHistoryEntryDefault>): List<CombinedHistoryEntryDefault> {
    if (entries.isEmpty()) {
        return entries
    }

    val result = mutableListOf<CombinedHistoryEntryDefault>()
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
 * Filters out entries that are in a transient state, such as 'Processing' without a data sourcing state
 * or 'DataVerification' with a 'Processed' request state. These states are not meaningful for display
 * and can be confusing to users.
 *
 * @param entries - The combined history entries to be filtered.
 * @returns The filtered combined history entries.
 */
fun filterHistory(entries: List<CombinedHistoryEntryDefault>): List<CombinedHistoryEntryDefault> =
    entries.filter { entry ->
        !(
            (entry.dataSourcingState == null && entry.requestState == RequestState.Processing) ||
                (
                    entry.dataSourcingState == DataSourcingState.DataVerification &&
                        entry.requestState == RequestState.Processed
                )
        )
    }

/**
 * Fills in missing requestState and dataSourcingState values in the combined history entries.
 * It iterates through the entries and uses the last known states to fill in any gaps.
 * Using both the request state and data sourcing state, it also calculates the displayed state for each entry.
 * The admin comment is also filled in based on the last admin comment from a request state.
 *
 * @param entries - The combined history entries to be processed.
 * @returns The combined history entries with filled gaps.
 */
fun fillHistoryGaps(entries: List<CombinedHistoryEntryDefault>): List<CombinedHistoryEntryDefault> {
    var lastRequestState: RequestState = RequestState.Open
    var lastDataSourcingState: DataSourcingState? = null
    var lastAdminComment: String? = null
    val result = mutableListOf<CombinedHistoryEntryDefault>()
    for (entry in entries) {
        val currentRequestState = entry.requestState ?: lastRequestState
        val currentDataSourcingState = entry.dataSourcingState ?: lastDataSourcingState
        val currentAdminComment = entry.adminComment ?: lastAdminComment
        result.add(
            entry.copy(
                requestState = currentRequestState,
                dataSourcingState = currentDataSourcingState,
                adminComment = currentAdminComment,
                displayedState = getDisplayedState(currentRequestState, currentDataSourcingState),
            ),
        )
        lastRequestState = currentRequestState
        lastDataSourcingState = currentDataSourcingState
        lastAdminComment = currentAdminComment
    }
    return result
}

/**
 * Builds a combined history of request states and data sourcing states based on the provided data request
 * revisions and data sourcing history.
 * It creates a unified list of history entries that include both request state changes and data sourcing state
 * changes, sorted by timestamp.
 * The method also checks that the first entry in the combined history has the request state 'Open', as this is
 * expected for all requests.
 *
 * @param requestHistory - A list of pairs containing RequestEntity objects and their corresponding
 * revision numbers, representing the history of request state changes.
 * @param dataSourcingHistory - A list of DataSourcingWithoutReferences objects representing the history of
 * data sourcing state changes associated with the request.
 * @returns A list of CombinedHistoryEntryDefault objects representing the combined history of request states
 * and data sourcing states, with gaps filled and filtered for display.
 */
fun buildCombinedHistory(
    requestHistory: List<Pair<RequestEntity, Long>>,
    dataSourcingHistory: List<DataSourcingWithoutReferences>,
): List<CombinedHistoryEntryDefault> {
    val requestEntries: List<CombinedHistoryEntryDefault> =
        requestHistory.map { (entity, _) ->
            CombinedHistoryEntryDefault(
                timestamp = entity.lastModifiedDate,
                requestState = entity.state,
                dataSourcingState = null,
                displayedState = null,
                adminComment = entity.adminComment,
            )
        }

    val dataSourcingEntries: List<CombinedHistoryEntryDefault> =
        dataSourcingHistory.map { entry ->
            CombinedHistoryEntryDefault(
                timestamp = entry.lastModifiedDate,
                requestState = null,
                dataSourcingState = entry.state,
                displayedState = null,
                adminComment = null,
            )
        }

    val combinedHistory = (requestEntries + dataSourcingEntries).sortedBy { it.timestamp }
    check(combinedHistory[0].requestState == RequestState.Open) {
        "The first entry in the history should have the request state 'Open'."
    }
    val filled = fillHistoryGaps(combinedHistory)
    return filterHistory(filled)
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

    val requestStateHistory = mutableListOf<ExtendedRequestHistoryEntry>() // Andere Liste

    while (requestHistorySorted.isNotEmpty() || dataSourcingHistorySorted.isNotEmpty()) {
        if (requestHistorySorted[0].first.lastModifiedDate < // Switch
            dataSourcingHistorySorted[0].lastModifiedDate
        ) {
            requestStateHistory.add(
                ExtendedRequestHistoryEntryData(
                    requestHistorySorted[0].first,
                    requestStateHistory.lastOrNull()?.dataSourcingState,
                ),
            )
            requestHistorySorted = requestHistorySorted.drop(1)
        } else if (requestHistorySorted[0].first.lastModifiedDate >
            dataSourcingHistorySorted[0].lastModifiedDate
        ) {
            requestStateHistory.add(
                ExtendedRequestHistoryEntryData(
                    dataSourcingHistorySorted[0],
                    requestStateHistory.last().requestState,
                    requestStateHistory.last().adminComment,
                ),
            )
            dataSourcingHistorySorted = dataSourcingHistorySorted.drop(1)
        } else {
            requestStateHistory.add(
                ExtendedRequestHistoryEntryData(
                    requestHistorySorted[0].first,
                    dataSourcingHistorySorted[0].state,
                ),
            )
            requestHistorySorted = requestHistorySorted.drop(1)
            dataSourcingHistorySorted = dataSourcingHistorySorted.drop(1)
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

    val requestStateHistory = mutableListOf<RequestHistoryEntry>()

    while (requestHistorySorted.isNotEmpty() || dataSourcingHistorySorted.isNotEmpty()) {
        if (requestHistorySorted[0].first.lastModifiedDate <
            dataSourcingHistorySorted[0].lastModifiedDate
        ) {
            requestStateHistory.add(
                RequestHistoryEntryData(requestHistorySorted[0].first),
            )
            requestHistorySorted = requestHistorySorted.drop(1)
        } else if (requestHistorySorted[0].first.lastModifiedDate >
            dataSourcingHistorySorted[0].lastModifiedDate
        ) {
            requestStateHistory.add(
                RequestHistoryEntryData(
                    dataSourcingHistorySorted[0],
                    requestStateHistory.last().displayedState,
                ),
            )
            dataSourcingHistorySorted = dataSourcingHistorySorted.drop(1)
        } else {
            requestStateHistory.add(
                RequestHistoryEntryData(
                    dataSourcingHistorySorted[0],
                    requestStateHistory.last().displayedState,
                ),
            )
            requestHistorySorted = requestHistorySorted.drop(1)
            dataSourcingHistorySorted = dataSourcingHistorySorted.drop(1)
        }
    }
    return requestStateHistory
}
