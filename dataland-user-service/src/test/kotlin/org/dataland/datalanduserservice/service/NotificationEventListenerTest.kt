package org.dataland.datalanduserservice.service

import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackendutils.model.BasicDataDimensions
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandbackendutils.utils.JsonUtils
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.messages.QaStatusChangeMessage
import org.dataland.datalandmessagequeueutils.messages.SourceabilityMessage
import org.dataland.datalanduserservice.entity.NotificationEventEntity
import org.dataland.datalanduserservice.model.enums.NotificationEventType
import org.dataland.datalanduserservice.repository.NotificationEventRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import java.util.UUID

class NotificationEventListenerTest {
    private val mockNotificationEventRepository = mock<NotificationEventRepository>()
    private val notificationEventListener = NotificationEventListener(mockNotificationEventRepository)

    companion object {
        val dataId = UUID.randomUUID()
        val companyId = UUID.randomUUID()
        val dataType = DataTypeEnum.sfdr
        const val REPORTINGPERIOD = "2025"
        val qaStatus = QaStatus.Accepted
    }

    @Test
    fun `test nonSourceable messages`() {
        val payload =
            SourceabilityMessage(
                BasicDataDimensions(
                    companyId.toString(),
                    dataType.value,
                    REPORTINGPERIOD,
                ),
                true,
                "",
            )

        notificationEventListener.processMessageForDataReportedAsNonSourceable(
            JsonUtils.defaultObjectMapper.writeValueAsString(payload),
            MessageType.DATASOURCING_NONSOURCEABLE,
            UUID.randomUUID().toString(),
        )

        val notificationCaptor = argumentCaptor<NotificationEventEntity>()
        verify(mockNotificationEventRepository, times(1)).save(
            notificationCaptor.capture(),
        )
        val notificationEventEntity = notificationCaptor.firstValue
        assertEquals(companyId, notificationEventEntity.companyId)
        assertEquals(dataType, notificationEventEntity.framework)
        assertEquals(REPORTINGPERIOD, notificationEventEntity.reportingPeriod)
        assertEquals(NotificationEventType.NonSourceableEvent, notificationEventEntity.notificationEventType)
    }

    @Test
    fun `test data availability messages`() {
        val payload =
            QaStatusChangeMessage(
                dataId.toString(),
                qaStatus,
                null,
                BasicDataDimensions(
                    companyId.toString(),
                    dataType.value,
                    REPORTINGPERIOD,
                ),
                isUpdate = true,
            )

        notificationEventListener.processMessageForAvailableDataAndUpdates(
            JsonUtils.defaultObjectMapper.writeValueAsString(payload),
            MessageType.QA_STATUS_UPDATED,
            UUID.randomUUID().toString(),
        )

        val notificationCaptor = argumentCaptor<NotificationEventEntity>()
        verify(mockNotificationEventRepository, times(1)).save(
            notificationCaptor.capture(),
        )
        val notificationEventEntity = notificationCaptor.firstValue
        assertEquals(companyId, notificationEventEntity.companyId)
        assertEquals(dataType, notificationEventEntity.framework)
        assertEquals(REPORTINGPERIOD, notificationEventEntity.reportingPeriod)
        assertEquals(NotificationEventType.UpdatedEvent, notificationEventEntity.notificationEventType)
    }
}
