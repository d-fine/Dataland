package org.dataland.datasourcingservice.utils

import org.dataland.datasourcingservice.entities.RequestEntity
import org.dataland.datasourcingservice.model.datasourcing.DataSourcingWithoutReferences
import org.dataland.datasourcingservice.model.enums.DataSourcingState
import org.dataland.datasourcingservice.model.enums.DisplayedState
import org.dataland.datasourcingservice.model.enums.RequestState
import org.dataland.datasourcingservice.model.request.ExtendedRequestHistoryEntryData
import org.dataland.datasourcingservice.model.request.RequestHistoryEntryData
import kotlin.collections.last
import kotlin.collections.lastOrNull
import kotlin.math.abs
import kotlin.math.sign

/**
 * A utility class for processing request state history and data sourcing state history.
 */
object RequestStateHistoryUtils {
    private const val TIME_DIFFERENCE_THRESHOLD_MS = 250

    /**
     * Deletes consecutive entries with the same displayedState, keeping the order stable.
     *
     * @param entries - The combined history entries from which repeating displayed states should be removed.
     * @returns The filtered combined history entries with repeating displayed states removed.
     */
    private fun deleteRepeatingDisplayedStates(entries: List<RequestHistoryEntryData>): List<RequestHistoryEntryData> {
        if (entries.isEmpty()) {
            return entries
        }
        val result = mutableListOf<RequestHistoryEntryData>()
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
     * Calculates the time difference between the next request state change and the next data sourcing state change.
     * It retrieves the last modified dates of the next entries in both histories and returns the difference.
     *
     * @param requestHistorySorted - A list of pairs containing RequestEntity objects and their corresponding revision numbers,
     * sorted by last modified date, representing the history of request state changes.
     * @param dataSourcingHistorySorted - A list of DataSourcingWithoutReferences objects sorted by last modified date,
     * representing the history of data sourcing state changes associated with the request.
     * @returns The time difference in milliseconds between the next request state change and the next data sourcing state change.
     */
    private fun compareNextRequestAndDataSourcingTimes(
        requestHistorySorted: List<RequestEntity>,
        dataSourcingHistorySorted: List<DataSourcingWithoutReferences>,
    ): Int =
        if (requestHistorySorted.isNotEmpty() && dataSourcingHistorySorted.isNotEmpty()) {
            val timeDifference = requestHistorySorted[0].lastModifiedDate - dataSourcingHistorySorted[0].lastModifiedDate

            if (abs(timeDifference) < TIME_DIFFERENCE_THRESHOLD_MS) {
                0
            } else {
                timeDifference.sign
            }
        } else if (dataSourcingHistorySorted.isNotEmpty()) {
            1
        } else {
            -1
        }

    /**
     * Builds a combined history of request state changes and data sourcing state changes, sorted by modification date.
     *
     * Here the admin view history is created. Each entry has five fields: data sourcing state, request state,
     * displayed state, admin comment, and modification date.
     *
     * @param sortedRequestHistory - A list of RequestEntity objects representing the history of state changes
     * for a specific request, sorted by lastModifiedDate in ascending order.
     * @param sortedDataSourcingHistoryAfterProcessing - A list of DataSourcingWithoutReferences objects representing
     * the history of data sourcing state changes associated with the request, sorted by lastModifiedDate in ascending order,
     * and filtered to include only entries after the request entered processing.
     * @return A list of ExtendedRequestHistoryEntryData objects representing the combined history of request
     * state changes and data sourcing state changes, sorted by modification date.
     */
    private fun buildExtendedHistory(
        sortedRequestHistory: MutableList<RequestEntity>,
        sortedDataSourcingHistoryAfterProcessing: MutableList<DataSourcingWithoutReferences>,
    ): List<ExtendedRequestHistoryEntryData> {
        val history =
            buildList<ExtendedRequestHistoryEntryData> {
                while (sortedRequestHistory.isNotEmpty() || sortedDataSourcingHistoryAfterProcessing.isNotEmpty()) {
                    val timeDifferenceSign =
                        compareNextRequestAndDataSourcingTimes(
                            sortedRequestHistory,
                            sortedDataSourcingHistoryAfterProcessing,
                        )
                    when {
                        timeDifferenceSign < 0 -> {
                            add(
                                ExtendedRequestHistoryEntryData(
                                    sortedRequestHistory[0],
                                    lastOrNull()?.dataSourcingState,
                                ),
                            )
                            sortedRequestHistory.removeAt(0)
                        }
                        timeDifferenceSign > 0 -> {
                            add(
                                ExtendedRequestHistoryEntryData(
                                    sortedDataSourcingHistoryAfterProcessing[0],
                                    last().requestState,
                                    last().adminComment,
                                ),
                            )
                            sortedDataSourcingHistoryAfterProcessing.removeAt(0)
                        }
                        else -> {
                            add(
                                ExtendedRequestHistoryEntryData(
                                    sortedRequestHistory[0],
                                    sortedDataSourcingHistoryAfterProcessing[0].state,
                                ),
                            )
                            sortedRequestHistory.removeAt(0)
                            sortedDataSourcingHistoryAfterProcessing.removeAt(0)
                        }
                    }
                }
            }
        return history
    }

    /**
     * Retrieves the timestamp of the first entry in the request history where the request state is "Processing".
     *
     * @param sortedRequestHistory - A list of RequestEntity objects representing the history of state changes for a specific request.
     * Sorted by lastModifiedDate in ascending order.
     * @return The timestamp of the first entry where the request state is "Processing", or null
     * if there are no entries with the "Processing" state in the request history.
     */
    fun getTimestampOfFirstProcessingState(sortedRequestHistory: List<RequestEntity>): Long? =
        sortedRequestHistory
            .filter { it.state == RequestState.Processing }
            .minByOrNull { it.lastModifiedDate }
            ?.lastModifiedDate

    /**
     * Builds a data sourcing history where the first entry reflects the last state before processing,
     * stamped at the request processing time, followed by all entries after processing.
     *
     * @param firstProcessingTimestamp - The timestamp when the request entered processing
     * @param sortedDataSourcingHistory - The full data sourcing history to derive the adjusted list from,
     * sorted bv lastModifiedDate in ascending order.
     * @return A list starting with the pre-processing state at requestProcessingTime, then all later entries
     */
    fun getDataSourcingHistoryStartingAtProcessing(
        firstProcessingTimestamp: Long,
        sortedDataSourcingHistory: List<DataSourcingWithoutReferences>,
    ): List<DataSourcingWithoutReferences> {
        val dataSourcingHistoryAfterRequestProcessing =
            sortedDataSourcingHistory.filter { it.lastModifiedDate >= firstProcessingTimestamp }
        val dataSourcingEntryBeforeRequestProcessing =
            sortedDataSourcingHistory
                .filter { it.lastModifiedDate < firstProcessingTimestamp }
                .maxByOrNull { it.lastModifiedDate }
        return if (
            dataSourcingEntryBeforeRequestProcessing == null ||
            dataSourcingEntryBeforeRequestProcessing.state == DataSourcingState.Done ||
            dataSourcingEntryBeforeRequestProcessing.state == DataSourcingState.NonSourceable
        ) {
            dataSourcingHistoryAfterRequestProcessing
        } else {
            val entryAtRequestProcessingTime =
                dataSourcingEntryBeforeRequestProcessing.copy(lastModifiedDate = firstProcessingTimestamp)
            listOf(entryAtRequestProcessingTime) + dataSourcingHistoryAfterRequestProcessing
        }
    }

    /**
     * Create a combined history of request state changes and data sourcing state changes, sorted by modification date.
     *
     * @param requestHistory - A list of RequestEntity objects representing the history of state changes for a specific request
     * @param dataSourcingHistory - A list of DataSourcingWithoutReferences representing the history of
     * data sourcing state changes associated with a data sourcing object
     * @returns A list of ExtendedRequestHistoryEntryData objects representing the combined history
     */
    fun getExtendedRequestHistory(
        requestHistory: List<RequestEntity>,
        dataSourcingHistory: List<DataSourcingWithoutReferences>,
    ): List<ExtendedRequestHistoryEntryData> {
        val requestHistorySorted = requestHistory.sortedBy { it.lastModifiedDate }
        val dataSourcingHistorySorted = dataSourcingHistory.sortedBy { it.lastModifiedDate }
        val firstProcessingTimestamp = getTimestampOfFirstProcessingState(requestHistory)

        return if (firstProcessingTimestamp == null) {
            buildExtendedHistory(
                requestHistorySorted.toMutableList(),
                mutableListOf(),
            )
        } else {
            val reducedDataSourcingHistorySorted =
                getDataSourcingHistoryStartingAtProcessing(
                    firstProcessingTimestamp,
                    dataSourcingHistorySorted,
                )
            buildExtendedHistory(
                requestHistorySorted.toMutableList(),
                reducedDataSourcingHistorySorted.toMutableList(),
            )
        }
    }

    /**
     * Create a combined history of request state changes and data sourcing state changes, sorted by modification date.
     *
     * @param requestHistory - A list of RequestEntity objects representing the history of state changes for a specific request
     * @param dataSourcingHistory - A list of DataSourcingWithoutReferences representing the history of
     * data sourcing state changes associated with a data sourcing object
     * @returns A list of RequestHistoryEntryData objects representing the combined history
     */
    fun getRequestHistory(
        requestHistory: List<RequestEntity>,
        dataSourcingHistory: List<DataSourcingWithoutReferences>,
    ): List<RequestHistoryEntryData> {
        val extendedRequestHistory = getExtendedRequestHistory(requestHistory, dataSourcingHistory)
        val requestHistoryEntries =
            extendedRequestHistory.map { entry ->
                RequestHistoryEntryData(entry.modificationDate, entry.displayedState)
            }
        return deleteRepeatingDisplayedStates(requestHistoryEntries)
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
        when (requestState) {
            RequestState.Withdrawn -> DisplayedState.Withdrawn
            RequestState.Open -> DisplayedState.Open
            RequestState.Processing, RequestState.Processed -> {
                requireNotNull(
                    dataSourcingState,
                    { "Data sourcing state cannot be null when request state is Processing or Processed" },
                )
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
        }
}
