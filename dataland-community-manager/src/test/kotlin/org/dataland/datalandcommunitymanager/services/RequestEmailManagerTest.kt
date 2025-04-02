package org.dataland.datalandcommunitymanager.services

import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandcommunitymanager.entities.DataRequestEntity
import org.dataland.datalandcommunitymanager.entities.MessageEntity
import org.dataland.datalandcommunitymanager.model.dataRequest.AccessStatus
import org.dataland.datalandcommunitymanager.model.dataRequest.DataRequestPatch
import org.dataland.datalandcommunitymanager.model.dataRequest.RequestStatus
import org.dataland.datalandcommunitymanager.services.messaging.AccessRequestEmailBuilder
import org.dataland.datalandcommunitymanager.services.messaging.DataRequestResponseEmailBuilder
import org.dataland.datalandcommunitymanager.services.messaging.SingleDataRequestEmailMessageSender
import org.dataland.datalandcommunitymanager.utils.TestUtils
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.reset
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.verifyNoMoreInteractions
import java.util.UUID

class RequestEmailManagerTest {
    private lateinit var dataRequestResponseEmailMessageSender: DataRequestResponseEmailBuilder
    private lateinit var singleDataRequestEmailMessageSender: SingleDataRequestEmailMessageSender
    private lateinit var accessRequestEmailBuilder: AccessRequestEmailBuilder

    private lateinit var requestEmailManager: RequestEmailManager

    @BeforeEach
    fun setupRequestEmailManager() {
        dataRequestResponseEmailMessageSender = mock(DataRequestResponseEmailBuilder::class.java)
        singleDataRequestEmailMessageSender = mock(SingleDataRequestEmailMessageSender::class.java)
        accessRequestEmailBuilder = mock(AccessRequestEmailBuilder::class.java)

        requestEmailManager =
            RequestEmailManager(
                dataRequestResponseEmailMessageSender,
                singleDataRequestEmailMessageSender,
                accessRequestEmailBuilder,
            )

        TestUtils.mockSecurityContext(
            username = "admin",
            userId = "adminId",
            roles = setOf(DatalandRealmRole.ROLE_ADMIN, DatalandRealmRole.ROLE_PREMIUM_USER),
        )
    }

    @Test
    fun `validate that a response email is only sent when a request status is patched to answered or nonsourceable`() {
        val dataRequestEntity = mock(DataRequestEntity::class.java)
        for (requestStatus in RequestStatus.entries) {
            requestEmailManager.sendEmailsWhenRequestStatusChanged(dataRequestEntity, requestStatus, false, null)

            if (requestStatus == RequestStatus.Answered) {
                verify(dataRequestResponseEmailMessageSender, times(1))
                    .buildDataRequestAnsweredEmailAndSendCEMessage(any(), any())
            } else if (requestStatus == RequestStatus.NonSourceable) {
                verify(dataRequestResponseEmailMessageSender, times(1))
                    .buildDataRequestNonSourceableEmailAndSendCEMessage(any(), any())
            } else {
                verify(dataRequestResponseEmailMessageSender, times(0))
                    .buildDataRequestAnsweredEmailAndSendCEMessage(any(), any())
                verify(dataRequestResponseEmailMessageSender, times(0))
                    .buildDataRequestNonSourceableEmailAndSendCEMessage(any(), any())
            }
            reset(dataRequestResponseEmailMessageSender)
        }
        verifyNoInteractions(accessRequestEmailBuilder)
    }

    @Test
    fun `validate that an access granted email is only sent on granted`() {
        val dataRequestEntity = DataRequestEntity("", "", false, "", "", 0L)
        for (accessStatus in AccessStatus.entries) {
            val dataRequestPatch = DataRequestPatch(accessStatus = accessStatus)
            requestEmailManager.sendNotificationsSpecificToAccessRequests(dataRequestEntity, dataRequestPatch, UUID.randomUUID().toString())

            if (accessStatus == AccessStatus.Granted) {
                verify(accessRequestEmailBuilder, times(1))
                    .notifyRequesterAboutGrantedRequest(any(), any())
            } else {
                verify(accessRequestEmailBuilder, times(0))
                    .notifyRequesterAboutGrantedRequest(any(), any())
            }
            verifyNoMoreInteractions(accessRequestEmailBuilder)
            reset(accessRequestEmailBuilder)
        }
        verifyNoInteractions(dataRequestResponseEmailMessageSender)
    }

    @Test
    fun `validate that an access requested email is sent`() {
        val dataRequestEntity = DataRequestEntity("", DataTypeEnum.vsme.name, true, "", "", 0L)
        dataRequestEntity.messageHistory =
            listOf(
                MessageEntity(
                    "", MessageEntity.COMPANY_OWNER_KEYWORD,
                    "Message", 0L, dataRequestEntity,
                ),
            )
        requestEmailManager.sendNotificationsSpecificToAccessRequests(
            dataRequestEntity,
            DataRequestPatch(
                requestStatus = RequestStatus.Answered,
                accessStatus = AccessStatus.Pending,
            ),
            UUID.randomUUID().toString(),
        )
        requestEmailManager.sendEmailsWhenRequestStatusChanged(
            dataRequestEntity,
            RequestStatus.Answered,
            false,
            UUID.randomUUID().toString(),
        )

        verify(accessRequestEmailBuilder, times(1))
            .notifyCompanyOwnerAboutNewRequest(any(), any())
        verify(dataRequestResponseEmailMessageSender, times(1))
            .buildDataRequestAnsweredEmailAndSendCEMessage(any(), any())

        verifyNoMoreInteractions(accessRequestEmailBuilder)
        verifyNoMoreInteractions(dataRequestResponseEmailMessageSender)
    }
}
