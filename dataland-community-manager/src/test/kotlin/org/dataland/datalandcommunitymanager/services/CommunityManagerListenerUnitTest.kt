package org.dataland.datalandcommunitymanager.services

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackend.openApiClient.model.SourceabilityInfo
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandmessagequeueutils.constants.ActionType
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.exceptions.MessageQueueRejectException
import org.dataland.datalandmessagequeueutils.messages.PrivateDataUploadMessage
import org.dataland.datalandmessagequeueutils.messages.QaStatusChangeMessage
import org.dataland.datalandmessagequeueutils.messages.SourceabilityMessage
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.EnumSource
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.times
import org.mockito.kotlin.verify

/**
 * Tests if the listener processes the incoming non-sourceable data information correctly.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class CommunityManagerListenerUnitTest {
    private lateinit var communityManagerListener: CommunityManagerListener
    private val jacksonObjectMapper = jacksonObjectMapper().findAndRegisterModules()
    private val mockDataRequestUpdateManager = mock<DataRequestUpdateManager>()
    private val mockInvestorRelationshipsManager = mock<InvestorRelationshipsManager>()
    private val validDataId = "valid-data-id"
    private val invalidDataId = ""
    private val correlationId = "test correlation id"

    private val typeQAStatusChange = MessageType.QA_STATUS_UPDATED
    private val typePrivateUpload = MessageType.PRIVATE_DATA_RECEIVED
    private val typeNonSourceable = MessageType.DATA_NONSOURCEABLE

    @BeforeEach
    fun setUp() {
        reset(
            mockDataRequestUpdateManager,
            mockInvestorRelationshipsManager,
        )
        communityManagerListener =
            CommunityManagerListener(
                jacksonObjectMapper,
                mockDataRequestUpdateManager,
                mockInvestorRelationshipsManager,
            )
    }

    @ParameterizedTest
    @EnumSource(QaStatus::class)
    fun `valid QA status change message should be dealt with appropriately`(qaStatus: QaStatus) {
        val qaStatusChangeMessage: QaStatusChangeMessage
        when (qaStatus) {
            QaStatus.Pending -> return
            else ->
                qaStatusChangeMessage =
                    QaStatusChangeMessage(
                        dataId = validDataId,
                        updatedQaStatus = qaStatus,
                        currentlyActiveDataId = validDataId,
                    )
        }
        communityManagerListener.changeRequestStatusAfterQaDecision(
            jacksonObjectMapper.writeValueAsString(qaStatusChangeMessage),
            typeQAStatusChange, correlationId,
        )

        when (qaStatus) {
            QaStatus.Accepted -> {
                verify(mockDataRequestUpdateManager).processUserRequests(
                    validDataId, correlationId,
                )
                verify(mockInvestorRelationshipsManager).saveNotificationEventForInvestorRelationshipsEmails(validDataId)
            }
            QaStatus.Rejected -> {
                verify(mockDataRequestUpdateManager, times(0)).processUserRequests(
                    any<String>(), any<String>(),
                )
                verify(mockInvestorRelationshipsManager, times(0)).saveNotificationEventForInvestorRelationshipsEmails(any<String>())
            }
            else -> {}
        }
    }

    @Test
    fun `invalid QA status change message should throw exception`() {
        val invalidQaStatusChangeMessage =
            QaStatusChangeMessage(
                dataId = invalidDataId,
                updatedQaStatus = QaStatus.Accepted,
                currentlyActiveDataId = invalidDataId,
            )
        assertThrows<MessageQueueRejectException> {
            communityManagerListener.changeRequestStatusAfterQaDecision(
                jacksonObjectMapper.writeValueAsString(invalidQaStatusChangeMessage),
                typeQAStatusChange, correlationId,
            )
        }
    }

    @Test
    fun `invalid private data received message should throw exception`() {
        val invalidPrivateDataUploadMessage =
            PrivateDataUploadMessage(
                dataId = invalidDataId,
                companyId = "",
                reportingPeriod = "",
                actionType = ActionType.STORE_PRIVATE_DATA_AND_DOCUMENTS,
                documentHashes = mapOf<String, String>(),
            )
        assertThrows<MessageQueueRejectException> {
            communityManagerListener.changeRequestStatusAfterPrivateDataUpload(
                jacksonObjectMapper.writeValueAsString(invalidPrivateDataUploadMessage),
                typePrivateUpload, correlationId,
            )
        }
    }

    @Test
    fun `valid private data received message should be processed successfully`() {
        val validPrivateDataUploadMessage =
            PrivateDataUploadMessage(
                dataId = validDataId,
                companyId = "",
                reportingPeriod = "",
                actionType = ActionType.STORE_PRIVATE_DATA_AND_DOCUMENTS,
                documentHashes = mapOf<String, String>(),
            )
        communityManagerListener.changeRequestStatusAfterPrivateDataUpload(
            jacksonObjectMapper.writeValueAsString(validPrivateDataUploadMessage),
            typePrivateUpload, correlationId,
        )
        verify(mockDataRequestUpdateManager).processUserRequests(
            validDataId, correlationId,
        )
    }

    @Test
    fun `valid nonsourceable message should be processed successfully`() {
        val sourceabilityMessageValid =
            SourceabilityMessage(
                "exampleCompany",
                "sfdr",
                "2023",
                true,
                "test",
            )
        val sourceabilityInfoValid =
            SourceabilityInfo(
                "exampleCompany",
                DataTypeEnum.sfdr,
                "2023",
                true,
                "test",
            )
        communityManagerListener.processDataReportedNonSourceableMessage(
            jacksonObjectMapper.writeValueAsString(sourceabilityMessageValid), typeNonSourceable, correlationId,
        )
        verify(mockDataRequestUpdateManager).patchAllRequestsToStatusNonSourceable(
            sourceabilityInfoValid,
            correlationId,
        )
    }

    @ParameterizedTest
    @CsvSource(
        value = [
            "\"\",\"2023\"",
            "\"exampleCompany\",\"\"",
        ],
    )
    fun `should throw exception for incomplete data in nonsourceable message`(
        companyId: String,
        reportingPeriod: String,
    ) {
        val sourceabilityMessageIncomplete =
            SourceabilityMessage(
                companyId,
                "sdfr",
                reportingPeriod,
                true,
                "test",
            )
        assertThrows<MessageQueueRejectException> {
            communityManagerListener.processDataReportedNonSourceableMessage(
                jacksonObjectMapper.writeValueAsString(sourceabilityMessageIncomplete),
                typeNonSourceable,
                correlationId,
            )
        }
    }

    @Test
    fun `should throw exception when isNonSourceable is false in nonsourceable message`() {
        assertThrows<MessageQueueRejectException> {
            val sourceability =
                SourceabilityMessage(
                    "exampleCompany",
                    "sfdr",
                    "2023",
                    false,
                    "test",
                )
            communityManagerListener
                .processDataReportedNonSourceableMessage(
                    jacksonObjectMapper.writeValueAsString(sourceability),
                    typeNonSourceable,
                    correlationId,
                )
        }
    }
}
