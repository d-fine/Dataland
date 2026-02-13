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
import org.springframework.stereotype.Component
import kotlin.collections.last
import kotlin.collections.lastOrNull
import kotlin.math.sign

/**
 * A utility class that provides functions for processing and combining request history entries and data sourcing
 * history entries.
 */
@Component("RequestStateHistoryUtils")
class RequestStateHistoryUtils {
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
     * A companion object that contains utility functions related to request state history processing.
     */
    companion object {
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
    fun compareRequestAndDataSourcingTime(
        requestHistorySorted: List<Pair<RequestEntity, Long>>,
        dataSourcingHistorySorted: List<DataSourcingWithoutReferences>,
    ): Int =
        if (requestHistorySorted.isNotEmpty() && dataSourcingHistorySorted.isNotEmpty()) {
            (requestHistorySorted[0].first.lastModifiedDate - dataSourcingHistorySorted[0].lastModifiedDate).sign
        } else if (dataSourcingHistorySorted.isNotEmpty()) {
            1
        } else {
            -1
        }

    /**
     * A sealed interface representing the different types of input that can be used to create a history entry.
     * It has three implementations: RequestOnly, DataSourcingOnly, and Both, which represent the different combinations
     * of request and data sourcing information that can be used to create a history entry.
     *
     * @param T - The type of the previous history entry, which can be either RequestHistoryEntry or ExtendedRequestHistoryEntry,
     * depending on the context in which the HistoryEntryInput is being used.
     */
    sealed interface HistoryEntryInput<out T> {
        /**
         * Represents a history entry input that contains only request information, along with an optional previous history entry.
         *
         * @param requestEntity - The RequestEntity object containing the request information for this history entry.
         * @param previousHistoryEntry - An optional previous history entry of type T, which can
         * be used to provide context for the request information when creating the new history entry.
         */
        data class RequestOnly<T>(
            val requestEntity: RequestEntity,
            val previousHistoryEntry: T?,
        ) : HistoryEntryInput<T>

        /**
         * Represents a history entry input that contains only data sourcing information, along with a previous history entry.
         *
         * @param dataSourcingWithoutReferences - The DataSourcingWithoutReferences object containing the data sourcing
         * information for this history entry.
         * @param previousHistoryEntry - A previous history entry of type T, which provides context
         *  for the data sourcing information when creating the new history entry. This parameter is required for this
         *  type of input, as the data sourcing
         *  information alone may not be sufficient to determine the displayed state without the context of the
         *  previous history entry.
         */
        data class DataSourcingOnly<T>(
            val dataSourcingWithoutReferences: DataSourcingWithoutReferences,
            val previousHistoryEntry: T,
        ) : HistoryEntryInput<T>

        /**
         * Represents a history entry input that contains both request information and data sourcing information.
         *
         * This type of input is used when both the request state and the data sourcing state change at the same time,
         * allowing for the creation of a history entry that reflects both changes simultaneously.
         * @param requestEntity - The RequestEntity object containing the request information for this history entry.
         * @param dataSourcingWithoutReferences - The DataSourcingWithoutReferences object containing the
         *
         */
        data class Both(
            val requestEntity: RequestEntity,
            val dataSourcingWithoutReferences: DataSourcingWithoutReferences,
        ) : HistoryEntryInput<Nothing>
    }

    /**
     * Creates an ExtendedRequestHistoryEntry based on the given input
     *
     * It determines the appropriate constructor to use for creating the ExtendedRequestHistoryEntryData object based on
     * which parameters are provided.
     *
     * @param input - A HistoryEntryInput object that can be of type RequestOnly, DataSourcingOnly, or Both,
     *                  containing the necessary information to create an ExtendedRequestHistoryEntry.
     * @returns An ExtendedRequestHistoryEntry object representing the combined state of the request and data sourcing
     */
    fun createExtendedHistoryEntry(input: HistoryEntryInput<ExtendedRequestHistoryEntry>): ExtendedRequestHistoryEntry =
        when (input) {
            is HistoryEntryInput.RequestOnly ->
                ExtendedRequestHistoryEntryData(
                    input.requestEntity,
                    input.previousHistoryEntry?.dataSourcingState,
                )

            is HistoryEntryInput.DataSourcingOnly ->
                ExtendedRequestHistoryEntryData(
                    input.dataSourcingWithoutReferences,
                    input.previousHistoryEntry.requestState,
                    input.previousHistoryEntry.adminComment,
                )

            is HistoryEntryInput.Both ->
                ExtendedRequestHistoryEntryData(
                    input.requestEntity,
                    input.dataSourcingWithoutReferences.state,
                )
        }

    /**
     * Creates a RequestHistoryEntry based on the given input
     *
     * It determines the appropriate constructor to use for creating the RequestHistoryEntryData object based on
     * which parameters are provided.
     *
     * @param input - A HistoryEntryInput object that can be of type RequestOnly, DataSourcingOnly, or Both,
     *                  containing the necessary information to create an ExtendedRequestHistoryEntry.
     * @returns A RequestHistoryEntry object representing the combined state of the request and data sourcing
     */
    fun createHistoryEntry(input: HistoryEntryInput<RequestHistoryEntry>): RequestHistoryEntry =
        when (input) {
            is HistoryEntryInput.RequestOnly ->
                RequestHistoryEntryData(input.requestEntity)

            is HistoryEntryInput.DataSourcingOnly ->
                RequestHistoryEntryData(
                    input.dataSourcingWithoutReferences,
                    input.previousHistoryEntry.displayedState,
                )

            is HistoryEntryInput.Both ->
                RequestHistoryEntryData(
                    input.dataSourcingWithoutReferences,
                    input.requestEntity,
                )
        }

    /**
     * Builds a combined history of request state changes and data sourcing state changes, sorted by modification date.
     *
     * @param requestHistory - A list of pairs containing RequestEntity objects, sorted by last modified date,
     * @param dataSourcingHistory - A list of DataSourcingWithoutReferences objects sorted by last modified date,
     * @param createHistoryEntry - A function that takes a HistoryEntryInput and creates a Request
     *                              HistoryEntry object representing the combined state of the request and data sourcing.
     * @returns A list of RequestHistoryEntry objects representing the combined history of request state
     * changes and data sourcing state changes, sorted by modification date.
     *
     */
    fun <T> buildHistory(
        requestHistory: List<Pair<RequestEntity, Long>>,
        dataSourcingHistory: List<DataSourcingWithoutReferences>,
        createHistoryEntry: (HistoryEntryInput<T>) -> T,
    ): List<T> {
        var requestHistorySorted = requestHistory.sortedBy { it.first.lastModifiedDate }
        var dataSourcingHistorySorted = dataSourcingHistory.sortedBy { it.lastModifiedDate }
        val history =
            buildList<T> {
                while (requestHistorySorted.isNotEmpty() || dataSourcingHistorySorted.isNotEmpty()) {
                    val timeDifferenceSign =
                        compareRequestAndDataSourcingTime(requestHistorySorted, dataSourcingHistorySorted)
                    when {
                        timeDifferenceSign < 0 -> {
                            add(
                                createHistoryEntry(
                                    HistoryEntryInput.RequestOnly(
                                        requestHistorySorted[0].first, lastOrNull(),
                                    ),
                                ),
                            )
                            requestHistorySorted = requestHistorySorted.drop(1)
                        }

                        timeDifferenceSign > 0 -> {
                            add(
                                createHistoryEntry(
                                    HistoryEntryInput.DataSourcingOnly(
                                        dataSourcingHistorySorted[0],
                                        last(),
                                    ),
                                ),
                            )
                            dataSourcingHistorySorted = dataSourcingHistorySorted.drop(1)
                        }

                        else -> {
                            add(
                                createHistoryEntry(
                                    HistoryEntryInput.Both(
                                        requestHistorySorted[0].first,
                                        dataSourcingHistorySorted[0],
                                    ),
                                ),
                            )
                            requestHistorySorted = requestHistorySorted.drop(1)
                            dataSourcingHistorySorted = dataSourcingHistorySorted.drop(1)
                        }
                    }
                }
            }
        return history
    }

    /**
     * Generates a combined history of request state changes and data sourcing state changes, sorted by modification date,
     * and creates ExtendedRequestHistoryEntry objects for each entry in the combined history using the
     * createExtendedHistoryEntry function.
     *
     * @param requestHistory - A list of pairs containing RequestEntity objects, sorted by last modified date,
     * @param dataSourcingHistory - A list of DataSourcingWithoutReferences objects sorted by last modified date,
     * @returns A list of ExtendedRequestHistoryEntry objects representing the combined history
     */
    public fun getExtendedRequestHistory(
        requestHistory: List<Pair<RequestEntity, Long>>,
        dataSourcingHistory: List<DataSourcingWithoutReferences>,
    ): List<ExtendedRequestHistoryEntry> = buildHistory(requestHistory, dataSourcingHistory, ::createExtendedHistoryEntry)

    /**
     * Generates a combined history of request state changes and data sourcing state changes, sorted by modification date,
     * and creates RequestHistoryEntry objects for each entry in the combined history using the
     * createHistoryEntry function.
     *
     * @param requestHistory - A list of pairs containing RequestEntity objects, sorted by last modified date,
     * @param dataSourcingHistory - A list of DataSourcingWithoutReferences objects sorted by last modified date,
     * @returns A list of RequestHistoryEntry objects representing the combined history
     */
    public fun getRequestHistory(
        requestHistory: List<Pair<RequestEntity, Long>>,
        dataSourcingHistory: List<DataSourcingWithoutReferences>,
    ): List<RequestHistoryEntry> {
        val combinedHistory = buildHistory(requestHistory, dataSourcingHistory, ::createHistoryEntry)
        return deleteRepeatingDisplayedStates(combinedHistory)
    }
}
