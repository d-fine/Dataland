package org.dataland.datalandcommunitymanager.services

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.dataland.datalandbackendutils.model.BasicDataDimensions
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.exceptions.MessageQueueRejectException
import org.dataland.datalandmessagequeueutils.messages.QaStatusChangeMessage
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import java.util.UUID

/**
 * Tests if the listener processes the incoming QA status change information correctly.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CommunityManagerListenerUnitTest {
    private lateinit var communityManagerListener: CommunityManagerListener
    private val jacksonObjectMapper = jacksonObjectMapper().findAndRegisterModules()
    private val mockInvestorRelationsManager = mock<InvestorRelationsManager>()
    private val validDataId = UUID.randomUUID().toString()
    private val invalidDataId = ""
    private val correlationId = UUID.randomUUID().toString()

    private val typeQAStatusChange = MessageType.QA_STATUS_UPDATED

    @BeforeEach
    fun setUp() {
        reset(
            mockInvestorRelationsManager,
        )
        communityManagerListener =
            CommunityManagerListener(
                mockInvestorRelationsManager,
            )
    }

    @ParameterizedTest
    @EnumSource(QaStatus::class)
    fun `valid QA status change message should be dealt with appropriately`(qaStatus: QaStatus) {
        if (qaStatus == QaStatus.Pending) return
        val qaStatusChangeMessage =
            QaStatusChangeMessage(
                dataId = validDataId,
                updatedQaStatus = qaStatus,
                currentlyActiveDataId = validDataId,
                BasicDataDimensions(UUID.randomUUID().toString(), "sfdr", "2025"),
                false,
            )
        communityManagerListener.saveInvestorRelationsNotificationAfterQaDecision(
            jacksonObjectMapper.writeValueAsString(qaStatusChangeMessage),
            typeQAStatusChange, correlationId,
        )

        when (qaStatus) {
            QaStatus.Accepted -> {
                verify(mockInvestorRelationsManager).saveNotificationEventForInvestorRelationsEmails(validDataId)
            }

            QaStatus.Rejected -> {
                verify(
                    mockInvestorRelationsManager,
                    times(0),
                ).saveNotificationEventForInvestorRelationsEmails(any<String>())
            }

            else -> {
                Unit
            }
        }
    }

    @Test
    fun `invalid QA status change message should throw exception`() {
        val invalidQaStatusChangeMessage =
            QaStatusChangeMessage(
                dataId = invalidDataId,
                updatedQaStatus = QaStatus.Accepted,
                currentlyActiveDataId = invalidDataId,
                BasicDataDimensions(UUID.randomUUID().toString(), "sfdr", "2025"), false,
            )
        assertThrows<MessageQueueRejectException> {
            communityManagerListener.saveInvestorRelationsNotificationAfterQaDecision(
                jacksonObjectMapper.writeValueAsString(invalidQaStatusChangeMessage),
                typeQAStatusChange, correlationId,
            )
        }
    }
}
