package org.dataland.datalandcommunitymanager.services

import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandcommunitymanager.model.dataRequest.AggregatedDataRequest
import org.dataland.datalandcommunitymanager.model.dataRequest.AggregatedDataRequestWithAggregatedPriority
import org.dataland.datalandcommunitymanager.model.dataRequest.AggregatedRequestPriority
import org.dataland.datalandcommunitymanager.model.dataRequest.RequestPriority

/**
 * Implementation of a request priority aggregator to determine the aggregated priority
 * and filter for a certain aggregated priority
 */
class RequestPriorityAggregator {
    /**
     * This method determines the aggregated priorities of aggregated data requests.
     * @param aggregatedDataRequests the aggregated data requests
     * @returns aggregated data requests with their aggregated priority
     */
    fun aggregateRequestPriority(aggregatedDataRequests: List<AggregatedDataRequest>): List<AggregatedDataRequestWithAggregatedPriority> {
        val aggregatedMap = createAggregatedMap(aggregatedDataRequests)
        return aggregatedMap
            .map { (key, priorityCounts) ->
                createAggregatedDataRequestWithPriority(key, priorityCounts)
            }
    }

    /**
     * Creates a map where the key is a dataset defined by data type, reporting period, and company ID.
     * The value is a map of request priorities with their respective counts.
     *
     * @param aggregatedDataRequests the list of aggregated data requests to process
     * @returns a map of request counts for each dataset
     */
    private fun createAggregatedMap(
        aggregatedDataRequests: List<AggregatedDataRequest>,
    ): Map<Triple<DataTypeEnum?, String?, String>, Map<RequestPriority, Long>> {
        val aggregatedMap = mutableMapOf<Triple<DataTypeEnum?, String?, String>, MutableMap<RequestPriority, Long>>()

        for (request in aggregatedDataRequests) {
            val key = Triple(request.dataType, request.reportingPeriod, request.datalandCompanyId)
            aggregatedMap
                .getOrPut(key) { mutableMapOf() }
                .merge(request.priority, request.count) { oldCount, newCount -> oldCount + newCount }
        }

        return aggregatedMap.mapValues { it.value.toMap() }
    }

    /**
     * Creates an instance of AggregatedDataRequestWithAggregatedPriority from the provided key and priority counts.
     *
     * @param key a tuple containing data type, reporting period, and company ID
     * @param priorityCounts a map of request priorities and their counts for the aggregated data request
     * @returns an AggregatedDataRequestWithAggregatedPriority instance or null if no valid priority is found
     */
    private fun createAggregatedDataRequestWithPriority(
        key: Triple<DataTypeEnum?, String?, String>,
        priorityCounts: Map<RequestPriority, Long>,
    ): AggregatedDataRequestWithAggregatedPriority {
        val aggregatedPriority = calculateAggregatedPriority(priorityCounts)
        val totalCount = priorityCounts.values.sum()

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
     * @param priorityCounts a map of request priorities to their counts
     * @returns the aggregated request priority or null if no valid priority can be determined
     */
    private fun calculateAggregatedPriority(priorityCounts: Map<RequestPriority, Long>): AggregatedRequestPriority? =
        when {
            priorityCounts[RequestPriority.Urgent]?.let { it > 0 } == true -> AggregatedRequestPriority.Urgent
            priorityCounts[RequestPriority.High]?.let { it > 1 } == true -> AggregatedRequestPriority.VeryHigh
            priorityCounts[RequestPriority.High]?.let { it.toInt() == 1 } == true -> AggregatedRequestPriority.High
            priorityCounts[RequestPriority.Low]?.let { it > 1 } == true -> AggregatedRequestPriority.Normal
            priorityCounts[RequestPriority.Low]?.let { it.toInt() == 1 } == true -> AggregatedRequestPriority.Low
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
