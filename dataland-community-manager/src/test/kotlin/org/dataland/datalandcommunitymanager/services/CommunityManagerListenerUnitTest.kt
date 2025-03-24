import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackend.openApiClient.model.NonSourceableInfo
import org.dataland.datalandcommunitymanager.services.CommunityManagerListener
import org.dataland.datalandcommunitymanager.services.DataRequestUpdateManager
import org.dataland.datalandcommunitymanager.services.InvestorRelationshipsManager
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.exceptions.MessageQueueRejectException
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

/**
 * Tests if the listener processes the incoming non-sourceable data information correctly.
 */
class CommunityManagerListenerUnitTest {
    private lateinit var communityManagerListener: CommunityManagerListener
    private lateinit var mockDataRequestUpdateManager: DataRequestUpdateManager
    private lateinit var mockInvestorRelationshipsManager: InvestorRelationshipsManager

    private val jacksonObjectMapper = jacksonObjectMapper()
    private val correlationId = "test correlation id"
    private val type = MessageType.DATA_NONSOURCEABLE

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
        mockDataRequestUpdateManager = mock(DataRequestUpdateManager::class.java)
        mockInvestorRelationshipsManager = mock(InvestorRelationshipsManager::class.java)
        communityManagerListener =
            CommunityManagerListener(
                jacksonObjectMapper,
                mockDataRequestUpdateManager,
                mockInvestorRelationshipsManager,
            )
    }

    @Test
    fun `should process non sourceable message successfully`() {
        communityManagerListener.processDataReportedNotSourceableMessage(
            jacksonObjectMapper.writeValueAsString(this.nonSourceableInfoValid), type, correlationId,
        )
        verify(mockDataRequestUpdateManager).patchAllRequestsForThisDatasetToStatusNonSourceable(nonSourceableInfoValid, correlationId)
    }

    @Test
    fun `should throw exception for incomplete data`() {
        assertThrows<MessageQueueRejectException> {
            communityManagerListener.processDataReportedNotSourceableMessage(
                jacksonObjectMapper.writeValueAsString(this.nonSourceableInfoNoCompanyId), type, correlationId,
            )
        }
        assertThrows<MessageQueueRejectException> {
            communityManagerListener.processDataReportedNotSourceableMessage(
                jacksonObjectMapper.writeValueAsString(this.nonSourceableInfoNoReportingPeriod), type, correlationId,
            )
        }
    }

    @Test
    fun `should throw exception when isNonSourceable is false`() {
        assertThrows<MessageQueueRejectException> {
            communityManagerListener
                .processDataReportedNotSourceableMessage(
                    jacksonObjectMapper.writeValueAsString(nonSourceableInfoValidButSourceable), type, correlationId,
                )
        }
    }
}
