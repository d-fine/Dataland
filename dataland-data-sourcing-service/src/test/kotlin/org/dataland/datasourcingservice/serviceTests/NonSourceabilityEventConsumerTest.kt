package org.dataland.datasourcingservice.serviceTests

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandmessagequeueutils.model.NonSourceabilityEventType
import org.dataland.datalandmessagequeueutils.model.NonSourceabilityLifecycleEvent
import org.dataland.datasourcingservice.model.datasourcing.DataSourcingPatch
import org.dataland.datasourcingservice.model.datasourcing.StoredDataSourcing
import org.dataland.datasourcingservice.model.enums.DataSourcingState
import org.dataland.datasourcingservice.services.DataSourcingManager
import org.dataland.datasourcingservice.services.DataSourcingQueryManager
import org.dataland.datasourcingservice.services.NonSourceabilityEventListener
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.UUID

class NonSourceabilityEventConsumerTest {
    private val objectMapper = mock<ObjectMapper>()
    private val dataSourcingQueryManager = mock<DataSourcingQueryManager>()
    private val dataSourcingManager = mock<DataSourcingManager>()
    private val listener = NonSourceabilityEventListener(objectMapper, dataSourcingQueryManager, dataSourcingManager)

    @Test
    fun `discard malformed nonSourceabilityId before performing data-sourcing lookup`() {
        listener.applyEvent(
            NonSourceabilityLifecycleEvent(
                nonSourceabilityId = "bad-id",
                companyId = UUID.randomUUID().toString(),
                dataType = "sfdr",
                reportingPeriod = "2025",
                eventType = NonSourceabilityEventType.CREATED,
            ),
        )

        verify(dataSourcingQueryManager, never()).searchDataSourcings(any(), any(), any(), any(), any(), any())
        verify(dataSourcingManager, never()).patchDataSourcingEntityById(any(), any())
    }

    @Test
    fun `discard unknown nonSourceabilityId when no data-sourcing entry can be resolved`() {
        whenever(
            dataSourcingQueryManager.searchDataSourcings(
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                any(),
                any(),
            ),
        ).thenReturn(emptyList())

        listener.applyEvent(
            NonSourceabilityLifecycleEvent(
                nonSourceabilityId = UUID.randomUUID().toString(),
                companyId = UUID.randomUUID().toString(),
                dataType = "sfdr",
                reportingPeriod = "2025",
                eventType = NonSourceabilityEventType.CREATED,
            ),
        )

        verify(dataSourcingQueryManager).searchDataSourcings(
            anyOrNull(),
            anyOrNull(),
            anyOrNull(),
            anyOrNull(),
            any(),
            any(),
        )
        verify(dataSourcingManager, never()).patchDataSourcingEntityById(any(), any())
    }

    @Test
    fun `created event transitions data-sourcing state to non-sourceable verification`() {
        val dataSourcingId = UUID.randomUUID().toString()
        whenever(
            dataSourcingQueryManager.searchDataSourcings(
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                any(),
                any(),
            ),
        ).thenReturn(
            listOf(
                StoredDataSourcing(
                    dataSourcingId = dataSourcingId,
                    companyId = UUID.randomUUID().toString(),
                    reportingPeriod = "2025",
                    dataType = "sfdr",
                    state = DataSourcingState.Initialized,
                ),
            ),
        )

        listener.applyEvent(
            NonSourceabilityLifecycleEvent(
                nonSourceabilityId = UUID.randomUUID().toString(),
                companyId = UUID.randomUUID().toString(),
                dataType = "sfdr",
                reportingPeriod = "2025",
                eventType = NonSourceabilityEventType.CREATED,
            ),
        )

        verify(dataSourcingManager).patchDataSourcingEntityById(
            UUID.fromString(dataSourcingId),
            DataSourcingPatch(state = DataSourcingState.NonSourceableVerification),
        )
    }

    @Test
    fun `auto accepted event transitions data-sourcing state to non-sourceable`() {
        val dataSourcingId = UUID.randomUUID().toString()
        whenever(
            dataSourcingQueryManager.searchDataSourcings(
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                anyOrNull(),
                any(),
                any(),
            ),
        ).thenReturn(
            listOf(
                StoredDataSourcing(
                    dataSourcingId = dataSourcingId,
                    companyId = UUID.randomUUID().toString(),
                    reportingPeriod = "2025",
                    dataType = "sfdr",
                    state = DataSourcingState.Initialized,
                ),
            ),
        )

        listener.applyEvent(
            NonSourceabilityLifecycleEvent(
                nonSourceabilityId = UUID.randomUUID().toString(),
                companyId = UUID.randomUUID().toString(),
                dataType = "sfdr",
                reportingPeriod = "2025",
                eventType = NonSourceabilityEventType.AUTO_ACCEPTED,
            ),
        )

        verify(dataSourcingManager).patchDataSourcingEntityById(
            UUID.fromString(dataSourcingId),
            DataSourcingPatch(state = DataSourcingState.NonSourceable),
        )
    }
}
