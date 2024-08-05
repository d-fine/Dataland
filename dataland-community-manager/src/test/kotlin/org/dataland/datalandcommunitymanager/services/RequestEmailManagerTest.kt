package org.dataland.datalandcommunitymanager.services

import org.dataland.datalandcommunitymanager.entities.DataRequestEntity
import org.dataland.datalandcommunitymanager.entities.MessageEntity
import org.dataland.datalandcommunitymanager.model.dataRequest.AccessStatus
import org.dataland.datalandcommunitymanager.model.dataRequest.RequestStatus
import org.dataland.datalandcommunitymanager.services.messaging.AccessRequestEmailSender
import org.dataland.datalandcommunitymanager.services.messaging.DataRequestResponseEmailSender
import org.dataland.datalandcommunitymanager.services.messaging.SingleDataRequestEmailMessageSender
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.reset
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.verifyNoMoreInteractions

class RequestEmailManagerTest {

    private lateinit var dataRequestResponseEmailMessageSender: DataRequestResponseEmailSender
    private lateinit var singleDataRequestEmailMessageSender: SingleDataRequestEmailMessageSender
    private lateinit var accessRequestEmailSender: AccessRequestEmailSender

    private lateinit var requestEmailManager: RequestEmailManager

    @BeforeEach
    fun setupRequestEmailManager() {
        dataRequestResponseEmailMessageSender = mock(DataRequestResponseEmailSender::class.java)
        singleDataRequestEmailMessageSender = mock(SingleDataRequestEmailMessageSender::class.java)
        accessRequestEmailSender = mock(AccessRequestEmailSender::class.java)

        requestEmailManager = RequestEmailManager(
            dataRequestResponseEmailMessageSender,
            singleDataRequestEmailMessageSender,
            accessRequestEmailSender,
        )
    }

    @Test
    fun `validate that a response email is only sent when a request status is patched to any but answered or closed`() {
        val dataRequestEntity = mock(DataRequestEntity::class.java)
        for (requestStatus in RequestStatus.entries) {
            requestEmailManager.sendEmailsWhenStatusChanged(dataRequestEntity, requestStatus, null, null)

            if (requestStatus == RequestStatus.Answered || requestStatus == RequestStatus.Closed) {
                verify(dataRequestResponseEmailMessageSender, times(1))
                    .sendDataRequestResponseEmail(any(), any(), any())
            } else {
                verify(dataRequestResponseEmailMessageSender, times(0))
                    .sendDataRequestResponseEmail(any(), any(), any())
            }
            reset(dataRequestResponseEmailMessageSender)
        }
        verifyNoInteractions(accessRequestEmailSender)
    }

    @Test
    fun `validate that a access granted email is only sent on granted`() {
        val dataRequestEntity = DataRequestEntity("", "", "", "", 0L)
        for (accessStatus in AccessStatus.entries) {
            requestEmailManager.sendEmailsWhenStatusChanged(dataRequestEntity, null, accessStatus, null)

            if (accessStatus == AccessStatus.Granted) {
                verify(accessRequestEmailSender, times(1))
                    .notifyRequesterAboutGrantedRequest(any(), any())
            } else {
                verify(accessRequestEmailSender, times(0))
                    .notifyRequesterAboutGrantedRequest(any(), any())
            }
            verifyNoMoreInteractions(accessRequestEmailSender)
            reset(accessRequestEmailSender)
        }
        verifyNoInteractions(dataRequestResponseEmailMessageSender)
    }

    @Test
    fun `validate that a access requested email is send`() {
        val dataRequestEntity = DataRequestEntity("", "", "", "", 0L)
        dataRequestEntity.messageHistory =
            listOf(
                MessageEntity(
                    "", MessageEntity.COMPANY_OWNER_KEYWORD,
                    "Message", 0L, dataRequestEntity,
                ),
            )
        requestEmailManager.sendEmailsWhenStatusChanged(
            dataRequestEntity, RequestStatus.Answered, AccessStatus.Pending, null,
        )

        verify(accessRequestEmailSender, times(1))
            .notifyCompanyOwnerAboutNewRequest(any(), any())
        verify(dataRequestResponseEmailMessageSender, times(1))
            .sendDataRequestResponseEmail(any(), any(), any())

        verifyNoMoreInteractions(accessRequestEmailSender)
        verifyNoMoreInteractions(dataRequestResponseEmailMessageSender)
    }
}
