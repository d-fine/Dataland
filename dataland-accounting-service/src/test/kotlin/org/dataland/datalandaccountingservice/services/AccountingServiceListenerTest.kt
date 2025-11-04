package org.dataland.datalandaccountingservice.services

import org.dataland.datalandaccountingservice.entities.BilledRequestEntity
import org.dataland.datalandaccountingservice.model.BilledRequestEntityId
import org.dataland.datalandaccountingservice.repositories.BilledRequestRepository
import org.dataland.datalandbackendutils.utils.JsonUtils.defaultObjectMapper
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.exceptions.MessageQueueRejectException
import org.dataland.datalandmessagequeueutils.messages.RequestSetToProcessingMessage
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Optional
import java.util.UUID

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AccountingServiceListenerTest {
    private val mockBilledRequestRepository = mock<BilledRequestRepository>()

    private lateinit var accountingServiceListener: AccountingServiceListener

    private val billedCompanyId = UUID.randomUUID().toString()
    private val dataSourcingId = UUID.randomUUID().toString()
    private val requestedCompanyId = UUID.randomUUID().toString()
    private val requestedReportingPeriod = "2025"
    private val requestedFramework = "sfdr"

    private val requestSetToProcessingMessage =
        RequestSetToProcessingMessage(
            billedCompanyId = billedCompanyId,
            dataSourcingId = dataSourcingId,
            requestedCompanyId = requestedCompanyId,
            requestedReportingPeriod = requestedReportingPeriod,
            requestedFramework = requestedFramework,
        )

    private val requestProcessingMessagePayload = defaultObjectMapper.writeValueAsString(requestSetToProcessingMessage)

    private val correlationId = UUID.randomUUID().toString()

    private val billedRequestEntity =
        BilledRequestEntity(
            billedCompanyId = UUID.fromString(billedCompanyId),
            dataSourcingId = UUID.fromString(dataSourcingId),
            requestedCompanyId = UUID.fromString(requestedCompanyId),
            requestedReportingPeriod = requestedReportingPeriod,
            requestedFramework = requestedFramework,
        )

    @BeforeEach
    fun setup() {
        reset(mockBilledRequestRepository)

        accountingServiceListener = AccountingServiceListener(mockBilledRequestRepository)
    }

    @Test
    fun `check that the wrong message type leads to an appropriate exception`() {
        assertThrows<MessageQueueRejectException> {
            accountingServiceListener.createBilledRequestOnRequestPatchToStateProcessing(
                payload = requestProcessingMessagePayload,
                type = "some.wrong.message.type",
                correlationId = correlationId,
            )
        }
    }

    @Test
    fun `check that no billed request is saved when one already exists under the relevant ID`() {
        doReturn(Optional.of(billedRequestEntity)).whenever(mockBilledRequestRepository).findById(
            BilledRequestEntityId(
                billedCompanyId = UUID.fromString(billedCompanyId),
                dataSourcingId = UUID.fromString(dataSourcingId),
            ),
        )

        accountingServiceListener.createBilledRequestOnRequestPatchToStateProcessing(
            payload = requestProcessingMessagePayload,
            type = MessageType.REQUEST_SET_TO_PROCESSING,
            correlationId = correlationId,
        )

        verify(mockBilledRequestRepository, times(0)).save(any())
    }

    @Test
    fun `check that a billed request is saved when none exists under the relevant ID`() {
        doReturn(Optional.empty<BilledRequestEntity>()).whenever(mockBilledRequestRepository).findById(
            BilledRequestEntityId(
                billedCompanyId = UUID.fromString(billedCompanyId),
                dataSourcingId = UUID.fromString(dataSourcingId),
            ),
        )
        doAnswer { invocation -> invocation.arguments[0] }.whenever(mockBilledRequestRepository).save(any())

        accountingServiceListener.createBilledRequestOnRequestPatchToStateProcessing(
            payload = requestProcessingMessagePayload,
            type = MessageType.REQUEST_SET_TO_PROCESSING,
            correlationId = correlationId,
        )

        verify(mockBilledRequestRepository, times(1)).save(
            argThat {
                billedCompanyId == billedRequestEntity.billedCompanyId &&
                    dataSourcingId == billedRequestEntity.dataSourcingId
            },
        )
    }
}
