package org.dataland.datalandcommunitymanager.services

import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandcommunitymanager.entities.DataRequestEntity
import org.dataland.datalandcommunitymanager.model.dataRequest.AccessStatus
import org.dataland.datalandcommunitymanager.model.dataRequest.RequestPriority
import org.dataland.datalandcommunitymanager.model.dataRequest.RequestStatus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

class DataRequestEntityTest {
    private val testUserId = UUID.randomUUID().toString()
    private val testDataType = DataTypeEnum.eutaxonomyMinusNonMinusFinancials
    private val testReportingPeriod = "2024"
    private val testCompanyId = UUID.randomUUID().toString()

    private val dataRequest =
        DataRequestEntity(
            userId = testUserId,
            dataType = testDataType.value,
            reportingPeriod = testReportingPeriod,
            datalandCompanyId = testCompanyId,
            creationTimestamp = Instant.now().toEpochMilli(),
        )

    @Test
    fun `validate that a new request has priority initialized to normal`() {
        assertEquals(RequestPriority.Normal, dataRequest.requestPriority)
    }

    @Test
    fun `validate that a new request has no admin comment`() {
        assertNull(dataRequest.adminComment)
    }

    @Test
    fun `validate that a new request has request status open`() {
        assertEquals(RequestStatus.Open, dataRequest.requestStatus)
    }

    @Test
    fun `validate that a new request has access status public`() {
        assertEquals(AccessStatus.Public, dataRequest.accessStatus)
    }

    @Test
    fun `validate that a new request has no request status change reason`() {
        assertNull(dataRequest.requestStatusChangeReason)
    }
}
