package org.dataland.datalandcommunitymanager.services

import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandcommunitymanager.model.dataRequest.AggregatedDataRequest
import org.dataland.datalandcommunitymanager.model.dataRequest.AggregatedDataRequestWithAggregatedPriority
import org.dataland.datalandcommunitymanager.model.dataRequest.AggregatedRequestPriority
import org.dataland.datalandcommunitymanager.model.dataRequest.RequestPriority
import org.springframework.stereotype.Service

/**
 * Implementation of a request priority aggregator to determine the aggregated priority
 * and filter for a certain aggregated priority
 */
@Service
class RequestPriorityAggregator {
    /**
     * This method determines the aggregated priorities of aggregated data requests.
     * @param aggregatedDataRequests the aggregated data requests
     * @returns aggregated data requests with their aggregated priority
     */
    fun aggregateRequestPriority(aggregatedDataRequests: List<AggregatedDataRequest>): List<AggregatedDataRequestWithAggregatedPriority> {
        val aggregatedMap = createMapOfCumulatedPriorities(aggregatedDataRequests)
        return aggregatedMap
            .map { (key, priorityCountMap) ->
                createAggregatedDataRequestWithPriority(key, priorityCountMap)
            }
    }

    /**
     * Creates a map where the key is a dataset defined by data type, reporting period, and company ID.
     * The value is a map of request priorities with their respective counts.
     *
     * @param aggregatedDataRequests the list of aggregated data requests to process
     * @returns a map of request counts for each dataset
     */
    private fun createMapOfCumulatedPriorities(
        aggregatedDataRequests: List<AggregatedDataRequest>,
    ): Map<Triple<DataTypeEnum?, String?, String>, Map<RequestPriority, Long>> {
        val cumulatedPrioritiesMap =
            mutableMapOf<Triple<DataTypeEnum?, String?, String>, MutableMap<RequestPriority, Long>>()

        for (request in aggregatedDataRequests) {
            val key = Triple(request.dataType, request.reportingPeriod, request.datalandCompanyId)
            cumulatedPrioritiesMap
                .getOrPut(key) { mutableMapOf() }
                .merge(
                    request.priority,
                    request.count,
                ) { oldCount, newCount -> oldCount + newCount }
        }

        return cumulatedPrioritiesMap.mapValues { it.value.toMap() }
    }

    /**
     * Creates an instance of AggregatedDataRequestWithAggregatedPriority from the provided key and priority counts.
     *
     * @param key a tuple containing data type, reporting period, and company ID
     * @param priorityCountMap a map of request priorities and their counts for the aggregated data request
     * @returns an AggregatedDataRequestWithAggregatedPriority instance or null if no valid priority is found
     */
    private fun createAggregatedDataRequestWithPriority(
        key: Triple<DataTypeEnum?, String?, String>,
        priorityCountMap: Map<RequestPriority, Long>,
    ): AggregatedDataRequestWithAggregatedPriority {
        val aggregatedPriority = calculateAggregatedPriority(priorityCountMap)
        val totalCount = priorityCountMap.values.sum()

        return aggregatedPriority?.let {
            AggregatedDataRequestWithAggregatedPriority(
                dataType = key.first,
                reportingPeriod = key.second,
                datalandCompanyId = key.third,
                aggregatedPriority = it,
                count = totalCount,
            )
        } ?: throw IllegalStateException("Aggregated priority cannot be null for key: $key")
    }

    /**
     * Determines the aggregated priority based on the counts of request priorities.
     *
     * @param priorityCountMap a map of request priorities to their counts
     * @returns the aggregated request priority or null if no valid priority can be determined
     */
    private fun calculateAggregatedPriority(priorityCountMap: Map<RequestPriority, Long>): AggregatedRequestPriority? =
        when {
            priorityCountMap[RequestPriority.Urgent]?.let { it > 0L } == true -> AggregatedRequestPriority.Urgent
            priorityCountMap[RequestPriority.High]?.let { it > 1L } == true -> AggregatedRequestPriority.VeryHigh
            priorityCountMap[RequestPriority.High]?.let { it == 1L } == true -> AggregatedRequestPriority.High
            priorityCountMap[RequestPriority.Baseline]?.let { it > 0L } == true -> AggregatedRequestPriority.Baseline
            priorityCountMap[RequestPriority.Low]?.let { it > 1L } == true -> AggregatedRequestPriority.Normal
            priorityCountMap[RequestPriority.Low]?.let { it == 1L } == true -> AggregatedRequestPriority.Low
            else -> null
        }

    /**
     * This method filters the results for a certain aggregated priority
     * @param aggregatedDataRequestsWithAggregatedPriority the aggregated data requests with their aggregated priority
     * @returns filtered list with only the chosen aggregated priority
     */
    fun filterBasedOnAggregatedPriority(
        aggregatedDataRequestsWithAggregatedPriority: List<AggregatedDataRequestWithAggregatedPriority>,
        aggregatedPriority: AggregatedRequestPriority?,
    ): List<AggregatedDataRequestWithAggregatedPriority> =
        if (aggregatedPriority == null) {
            aggregatedDataRequestsWithAggregatedPriority
        } else {
            aggregatedDataRequestsWithAggregatedPriority.filter { request ->
                request.aggregatedPriority == aggregatedPriority
            }
        }
}
