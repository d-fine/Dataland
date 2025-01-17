package org.dataland.datalandcommunitymanager.services

import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandcommunitymanager.model.dataRequest.AggregatedDataRequest
import org.dataland.datalandcommunitymanager.model.dataRequest.AggregatedRequestPriority
import org.dataland.datalandcommunitymanager.model.dataRequest.RequestPriority
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class RequestPriorityAggregatorTest {
    private var requestPriorityAggregator = RequestPriorityAggregator()
    private val companyIdLowPriority = "Test Low"
    private val companyIdNormalPriority = "Test Normal"
    private val companyIdBaselinePriority = "Test Baseline"
    private val companyIdHighPriority = "Test High"
    private val companyIdVeryHighPriority = "Test Very High"
    private val companyIdUrgentPriority = "Test Urgent"
    private val dummyAggregatedRequests =
        listOf(
            AggregatedDataRequest(DataTypeEnum.sfdr, "2023", companyIdLowPriority, RequestPriority.Low, "Open", 1),
            AggregatedDataRequest(DataTypeEnum.sfdr, "2023", companyIdLowPriority, RequestPriority.High, "Open", 0),
            AggregatedDataRequest(DataTypeEnum.sfdr, "2023", companyIdLowPriority, RequestPriority.Urgent, "Open", 0),
            AggregatedDataRequest(DataTypeEnum.sfdr, "2024", companyIdNormalPriority, RequestPriority.Low, "Open", 2),
            AggregatedDataRequest(DataTypeEnum.sfdr, "2024", companyIdNormalPriority, RequestPriority.High, "Open", 0),
            AggregatedDataRequest(
                DataTypeEnum.sfdr, "2024", companyIdNormalPriority, RequestPriority.Urgent, "Open", 0,
            ),
            AggregatedDataRequest(
                DataTypeEnum.sfdr, "2023", companyIdBaselinePriority, RequestPriority.Baseline, "Open", 2,
            ),
            AggregatedDataRequest(
                DataTypeEnum.sfdr, "2024", companyIdBaselinePriority, RequestPriority.Low, "Open", 2,
            ),
            AggregatedDataRequest(
                DataTypeEnum.sfdr, "2024", companyIdBaselinePriority, RequestPriority.Baseline, "Open", 1,
            ),
            AggregatedDataRequest(DataTypeEnum.p2p, "2023", companyIdHighPriority, RequestPriority.Low, "Open", 2),
            AggregatedDataRequest(DataTypeEnum.p2p, "2023", companyIdHighPriority, RequestPriority.Baseline, "Open", 1),
            AggregatedDataRequest(DataTypeEnum.p2p, "2023", companyIdHighPriority, RequestPriority.High, "Open", 1),
            AggregatedDataRequest(DataTypeEnum.p2p, "2023", companyIdHighPriority, RequestPriority.Urgent, "Open", 0),
            AggregatedDataRequest(DataTypeEnum.p2p, "2024", companyIdVeryHighPriority, RequestPriority.Low, "Open", 2),
            AggregatedDataRequest(
                DataTypeEnum.p2p, "2024", companyIdVeryHighPriority, RequestPriority.Baseline, "Open", 2,
            ),
            AggregatedDataRequest(DataTypeEnum.p2p, "2024", companyIdVeryHighPriority, RequestPriority.High, "Open", 2),
            AggregatedDataRequest(
                DataTypeEnum.p2p, "2024", companyIdVeryHighPriority, RequestPriority.Urgent, "Open", 0,
            ),
            AggregatedDataRequest(DataTypeEnum.vsme, "2023", companyIdUrgentPriority, RequestPriority.Low, "Open", 0),
            AggregatedDataRequest(DataTypeEnum.vsme, "2023", companyIdUrgentPriority, RequestPriority.High, "Open", 0),
            AggregatedDataRequest(
                DataTypeEnum.vsme, "2023", companyIdUrgentPriority, RequestPriority.Urgent, "Open", 1,
            ),
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
                companyIdLowPriority to AggregatedRequestPriority.Low,
                companyIdNormalPriority to AggregatedRequestPriority.Normal,
                companyIdBaselinePriority to AggregatedRequestPriority.Baseline,
                companyIdHighPriority to AggregatedRequestPriority.High,
                companyIdVeryHighPriority to AggregatedRequestPriority.VeryHigh,
                companyIdUrgentPriority to AggregatedRequestPriority.Urgent,
            )

        expectedPriorities.forEach { (companyId, expectedPriority) ->
            val aggregatedRequest =
                aggregatedRequestsWithAggregatedPriority.filter {
                    it.datalandCompanyId == companyId
                }
            aggregatedRequest.forEach { it -> assertEquals(expectedPriority, it.aggregatedPriority) }
        }
    }

    @Test
    fun `validate that filtering the aggregated priorities works as expected`() {
        val aggregatedRequestsWithAggregatedPriority =
            requestPriorityAggregator.aggregateRequestPriority(dummyAggregatedRequests)

        val filteredAggregatedRequestsWithAggregatedPriority =
            requestPriorityAggregator
                .filterBasedOnAggregatedPriority(aggregatedRequestsWithAggregatedPriority, AggregatedRequestPriority.Low)

        filteredAggregatedRequestsWithAggregatedPriority.forEach {
            assertEquals(AggregatedRequestPriority.Low, it.aggregatedPriority)
        }
    }
}
