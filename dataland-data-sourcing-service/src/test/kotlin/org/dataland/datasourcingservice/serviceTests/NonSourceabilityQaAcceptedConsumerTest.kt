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
import org.dataland.datasourcingservice.services.NonSourceabilityQaDecisionListener
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.UUID

class NonSourceabilityQaAcceptedConsumerTest {
    private val objectMapper = mock<ObjectMapper>()
    private val dataSourcingQueryManager = mock<DataSourcingQueryManager>()
    private val dataSourcingManager = mock<DataSourcingManager>()
    private val lifecycleListener = NonSourceabilityEventListener(objectMapper, dataSourcingQueryManager, dataSourcingManager)
    private val qaDecisionListener = NonSourceabilityQaDecisionListener(lifecycleListener)

    @Test
    fun `qa accepted lifecycle event transitions data-sourcing object to non-sourceable`() {
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
                    state = DataSourcingState.NonSourceableVerification,
                ),
            ),
        )

        qaDecisionListener.applyEvent(
            NonSourceabilityLifecycleEvent(
                nonSourceabilityId = UUID.randomUUID().toString(),
                companyId = UUID.randomUUID().toString(),
                dataType = "sfdr",
                reportingPeriod = "2025",
                eventType = NonSourceabilityEventType.QA_ACCEPTED,
            ),
        )

        verify(dataSourcingManager).patchDataSourcingEntityById(
            UUID.fromString(dataSourcingId),
            DataSourcingPatch(state = DataSourcingState.NonSourceable),
        )
    }
}
