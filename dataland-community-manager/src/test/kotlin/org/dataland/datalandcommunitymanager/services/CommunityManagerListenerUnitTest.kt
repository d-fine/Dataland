import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackend.openApiClient.model.NonSourceableInfo
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandcommunitymanager.services.CommunityManagerListener
import org.dataland.datalandcommunitymanager.services.DataRequestUpdateManager
import org.dataland.datalandcommunitymanager.services.InvestorRelationshipsManager
import org.dataland.datalandmessagequeueutils.constants.ActionType
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.exceptions.MessageQueueRejectException
import org.dataland.datalandmessagequeueutils.messages.PrivateDataUploadMessage
import org.dataland.datalandmessagequeueutils.messages.QaStatusChangeMessage
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.mock
import org.mockito.Mockito.reset
import org.mockito.Mockito.times
import org.mockito.Mockito.verify

/**
 * Tests if the listener processes the incoming non-sourceable data information correctly.
 */
class CommunityManagerListenerUnitTest {
    private lateinit var communityManagerListener: CommunityManagerListener
    private val jacksonObjectMapper = jacksonObjectMapper()
    private val mockDataRequestUpdateManager = mock(DataRequestUpdateManager::class.java)
    private val mockInvestorRelationshipsManager = mock(InvestorRelationshipsManager::class.java)
    private val validDataId = "valid-data-id"
    private val invalidDataId = ""
    private val correlationId = "test correlation id"

    // Variables for testing the pipeline for QA acceptance messages.
    private val typeQAStatusChange = MessageType.QA_STATUS_UPDATED

    private val qaStatusChangeMessageWithAcceptance =
        QaStatusChangeMessage(
            dataId = validDataId,
            updatedQaStatus = QaStatus.Accepted,
            currentlyActiveDataId = validDataId,
        )

    private val qaStatusChangeMessageWithRejection =
        QaStatusChangeMessage(
            dataId = validDataId,
            updatedQaStatus = QaStatus.Rejected,
            currentlyActiveDataId = validDataId,
        )

    private val invalidQaStatusChangeMessage =
        QaStatusChangeMessage(
            dataId = invalidDataId,
            updatedQaStatus = QaStatus.Accepted,
            currentlyActiveDataId = invalidDataId,
        )

    // Variables for testing the pipeline for private data upload messages.
    private val typePrivateUpload = MessageType.PRIVATE_DATA_RECEIVED

    private val invalidPrivateDataUploadMessage =
        PrivateDataUploadMessage(
            dataId = invalidDataId,
            companyId = "",
            reportingPeriod = "",
            actionType = ActionType.STORE_PRIVATE_DATA_AND_DOCUMENTS,
            documentHashes = mapOf<String, String>(),
        )

    private val validPrivateDataUploadMessage =
        PrivateDataUploadMessage(
            dataId = validDataId,
            companyId = "",
            reportingPeriod = "",
            actionType = ActionType.STORE_PRIVATE_DATA_AND_DOCUMENTS,
            documentHashes = mapOf<String, String>(),
        )

    // Variables for testing the pipeline for nonsourceability messages.
    private val typeNonSourceable = MessageType.DATA_NONSOURCEABLE

    private val nonSourceableInfoValid =
        NonSourceableInfo(
            "exampleCompany",
            DataTypeEnum.sfdr,
            "2023",
            true,
            "test",
        )

    private val nonSourceableInfoNoCompanyId =
        NonSourceableInfo(
            "",
            DataTypeEnum.sfdr,
            "2023",
            true,
            "test",
        )

    private val nonSourceableInfoNoReportingPeriod =
        NonSourceableInfo(
            "exampleCompany",
            DataTypeEnum.sfdr,
            "",
            true,
            "test",
        )

    private val nonSourceableInfoValidButSourceable =
        NonSourceableInfo(
            "exampleCompany",
            DataTypeEnum.sfdr,
            "2023",
            false,
            "test",
        )

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

    @Test
    fun `valid QA status change message with acceptance should be processed successfully`() {
        communityManagerListener.changeRequestStatusAfterQADecision(
            jacksonObjectMapper.writeValueAsString(this.qaStatusChangeMessageWithAcceptance),
            typeQAStatusChange, correlationId,
        )
        verify(mockDataRequestUpdateManager).patchRequestStatusFromOpenOrNonSourceableToAnsweredByDataId(
            validDataId, correlationId,
        )
        verify(mockDataRequestUpdateManager).processAnsweredOrClosedOrResolvedRequests(
            validDataId, correlationId,
        )
        verify(mockInvestorRelationshipsManager).saveNotificationEventForIREmails(validDataId)
    }

    @Test
    fun `valid QA status change message with rejection should lead to no further processing`() {
        communityManagerListener.changeRequestStatusAfterQADecision(
            jacksonObjectMapper.writeValueAsString(this.qaStatusChangeMessageWithRejection),
            typeQAStatusChange, correlationId,
        )
        verify(mockDataRequestUpdateManager, times(0)).patchRequestStatusFromOpenOrNonSourceableToAnsweredByDataId(
            anyString(), anyString(),
        )
        verify(mockDataRequestUpdateManager, times(0)).processAnsweredOrClosedOrResolvedRequests(
            anyString(), anyString(),
        )
        verify(mockInvestorRelationshipsManager, times(0)).saveNotificationEventForIREmails(anyString())
    }

    @Test
    fun `invalid QA status change message should throw exception`() {
        assertThrows<MessageQueueRejectException> {
            communityManagerListener.changeRequestStatusAfterQADecision(
                jacksonObjectMapper.writeValueAsString(this.invalidQaStatusChangeMessage),
                typeQAStatusChange, correlationId,
            )
        }
    }

    @Test
    fun `invalid private data received message should throw exception`() {
        assertThrows<MessageQueueRejectException> {
            communityManagerListener.changeRequestStatusAfterPrivateDataUpload(
                jacksonObjectMapper.writeValueAsString(this.invalidPrivateDataUploadMessage),
                typePrivateUpload, correlationId,
            )
        }
    }

    @Test
    fun `valid private data received message should be processed successfully`() {
        communityManagerListener.changeRequestStatusAfterPrivateDataUpload(
            jacksonObjectMapper.writeValueAsString(this.validPrivateDataUploadMessage),
            typePrivateUpload, correlationId,
        )
        verify(mockDataRequestUpdateManager).patchRequestStatusFromOpenOrNonSourceableToAnsweredByDataId(
            validDataId, correlationId,
        )
    }

    @Test
    fun `valid nonsourceable message should be processed successfully`() {
        communityManagerListener.processDataReportedNotSourceableMessage(
            jacksonObjectMapper.writeValueAsString(this.nonSourceableInfoValid), typeNonSourceable, correlationId,
        )
        verify(mockDataRequestUpdateManager).patchAllRequestsForThisDatasetToStatusNonSourceable(
            nonSourceableInfoValid,
            correlationId,
        )
    }

    @Test
    fun `should throw exception for incomplete data in nonsourceable message`() {
        assertThrows<MessageQueueRejectException> {
            communityManagerListener.processDataReportedNotSourceableMessage(
                jacksonObjectMapper.writeValueAsString(this.nonSourceableInfoNoCompanyId),
                typeNonSourceable,
                correlationId,
            )
        }
        assertThrows<MessageQueueRejectException> {
            communityManagerListener.processDataReportedNotSourceableMessage(
                jacksonObjectMapper.writeValueAsString(this.nonSourceableInfoNoReportingPeriod),
                typeNonSourceable,
                correlationId,
            )
        }
    }

    @Test
    fun `should throw exception when isNonSourceable is false in nonsourceable message`() {
        assertThrows<MessageQueueRejectException> {
            communityManagerListener
                .processDataReportedNotSourceableMessage(
                    jacksonObjectMapper.writeValueAsString(nonSourceableInfoValidButSourceable),
                    typeNonSourceable,
                    correlationId,
                )
        }
    }
}
