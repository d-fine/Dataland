package org.dataland.datalandcommunitymanager.services

import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandcommunitymanager.model.dataRequest.AggregatedDataRequest
import org.dataland.datalandcommunitymanager.model.dataRequest.AggregatedRequestPriority
import org.dataland.datalandcommunitymanager.model.dataRequest.RequestPriority
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class RequestPriorityAggregatorTest {
    private lateinit var requestPriorityAggregator: RequestPriorityAggregator
    private val dummyAggregatedRequests =
        listOf(
            AggregatedDataRequest(DataTypeEnum.sfdr, "2023", "Test Low", RequestPriority.Low, 1),
            AggregatedDataRequest(DataTypeEnum.sfdr, "2023", "Test Low", RequestPriority.High, 0),
            AggregatedDataRequest(DataTypeEnum.sfdr, "2023", "Test Low", RequestPriority.Urgent, 0),
            AggregatedDataRequest(DataTypeEnum.sfdr, "2024", "Test Normal", RequestPriority.Low, 2),
            AggregatedDataRequest(DataTypeEnum.sfdr, "2024", "Test Normal", RequestPriority.High, 0),
            AggregatedDataRequest(DataTypeEnum.sfdr, "2024", "Test Normal", RequestPriority.Urgent, 0),
            AggregatedDataRequest(DataTypeEnum.p2p, "2023", "Test High", RequestPriority.Low, 2),
            AggregatedDataRequest(DataTypeEnum.p2p, "2023", "Test High", RequestPriority.High, 1),
            AggregatedDataRequest(DataTypeEnum.p2p, "2023", "Test High", RequestPriority.Urgent, 0),
            AggregatedDataRequest(DataTypeEnum.p2p, "2024", "Test Very High", RequestPriority.Low, 2),
            AggregatedDataRequest(DataTypeEnum.p2p, "2024", "Test Very High", RequestPriority.High, 2),
            AggregatedDataRequest(DataTypeEnum.p2p, "2024", "Test Very High", RequestPriority.Urgent, 0),
            AggregatedDataRequest(DataTypeEnum.vsme, "2023", "Test Urgent", RequestPriority.Low, 0),
            AggregatedDataRequest(DataTypeEnum.vsme, "2023", "Test Urgent", RequestPriority.High, 0),
            AggregatedDataRequest(DataTypeEnum.vsme, "2023", "Test Urgent", RequestPriority.Urgent, 1),
        )

    @BeforeEach
    fun setup() {
        requestPriorityAggregator = RequestPriorityAggregator()
    }

    @Test
    fun `validate that the aggregated request priority is calculated correctly`() {
        val aggregatedRequestsWithAggregatedPriority =
            requestPriorityAggregator.aggregateRequestPriority(dummyAggregatedRequests)

        val expectedPriorities =
            mapOf(
                "Test Low" to AggregatedRequestPriority.Low,
                "Test Normal" to AggregatedRequestPriority.Normal,
                "Test High" to AggregatedRequestPriority.High,
                "Test Very High" to AggregatedRequestPriority.VeryHigh,
                "Test Urgent" to AggregatedRequestPriority.Urgent,
            )

        expectedPriorities.forEach { (companyId, expectedPriority) ->
            val aggregatedRequest =
                aggregatedRequestsWithAggregatedPriority.find {
                    it.datalandCompanyId == companyId
                }
            assertEquals(expectedPriority, aggregatedRequest?.aggregatedPriority)
        }
    }
}
