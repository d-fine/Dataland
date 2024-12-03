import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackend.openApiClient.model.NonSourceableInfo
import org.dataland.datalandcommunitymanager.services.NonSourceableDataListener
import org.dataland.datalandcommunitymanager.services.NonSourceableDataManager
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
class NonSourceableDataListenerTest {
    private lateinit var nonSourceableDataListener: NonSourceableDataListener
    private lateinit var mockNonSourceableDataManager: NonSourceableDataManager

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
        mockNonSourceableDataManager = mock(NonSourceableDataManager::class.java)
        nonSourceableDataListener = NonSourceableDataListener(jacksonObjectMapper, mockNonSourceableDataManager)
    }

    @Test
    fun `should process non sourceable message successfully`() {
        nonSourceableDataListener.processDataReportedNotSourceableMessage(
            jacksonObjectMapper.writeValueAsString(this.nonSourceableInfoValid), type, correlationId,
        )
        verify(mockNonSourceableDataManager).patchAllRequestsForThisDatasetToStatusNonSourceable(nonSourceableInfoValid, correlationId)
    }

    @Test
    fun `should throw exception for incomplete data`() {
        assertThrows<MessageQueueRejectException> {
            nonSourceableDataListener.processDataReportedNotSourceableMessage(
                jacksonObjectMapper.writeValueAsString(this.nonSourceableInfoNoCompanyId), type, correlationId,
            )
        }
        assertThrows<MessageQueueRejectException> {
            nonSourceableDataListener.processDataReportedNotSourceableMessage(
                jacksonObjectMapper.writeValueAsString(this.nonSourceableInfoNoReportingPeriod), type, correlationId,
            )
        }
    }

    @Test
    fun `should throw exception when isNonSourceable is false`() {
        assertThrows<MessageQueueRejectException> {
            nonSourceableDataListener
                .processDataReportedNotSourceableMessage(
                    jacksonObjectMapper.writeValueAsString(nonSourceableInfoValidButSourceable), type, correlationId,
                )
        }
    }
}
