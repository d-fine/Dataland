import com.fasterxml.jackson.databind.ObjectMapper
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
import org.mockito.Mockito.`when`

/**
 * Tests if the listener processes the incoming non-sourceable data information correctly.
 */
class NonSourceableDataListenerTest {
    private lateinit var nonSourceableDataListener: NonSourceableDataListener
    private lateinit var mockObjectMapper: ObjectMapper
    private lateinit var mockNonSourceableDataManager: NonSourceableDataManager

    private val nonSourceableJsonStringValid =
        """{"companyId": "exampleCompany", "dataType": "sfdr",
        | "reportingPeriod": "2023", "isNonSourceable": true, "reason": "test"}
        """.trimMargin()

    private val nonSourceableJsonStringNoCompanyId =
        """{"companyId": "", "dataType": "sfdr",
        | "reportingPeriod": "2023", "isNonSourceable": true}
        """.trimMargin()

    private val nonSourceableJsonStringNoReportingPeriod =
        """{"companyId": "exampleCompany", "dataType": "sfdr",
        | "reportingPeriod": "", "isNonSourceable": true}
        """.trimMargin()

    private val nonSourceableJsonStringValidButSourceable =
        """{"companyId": "exampleCompany", "dataType": "sfdr",
        | "reportingPeriod": "2023", "isNonSourceable": false, "reason": "test"}
        """.trimMargin()

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
        mockObjectMapper = mock(ObjectMapper::class.java)
        mockNonSourceableDataManager = mock(NonSourceableDataManager::class.java)
        nonSourceableDataListener = NonSourceableDataListener(mockObjectMapper, mockNonSourceableDataManager)

        `when`(mockObjectMapper.readValue(nonSourceableJsonStringValid, NonSourceableInfo::class.java)).thenReturn(nonSourceableInfoValid)
        `when`(mockObjectMapper.readValue(nonSourceableJsonStringNoCompanyId, NonSourceableInfo::class.java))
            .thenReturn(nonSourceableInfoNoCompanyId)
        `when`(mockObjectMapper.readValue(nonSourceableJsonStringValidButSourceable, NonSourceableInfo::class.java))
            .thenReturn(nonSourceableInfoValidButSourceable)
        `when`(mockObjectMapper.readValue(nonSourceableJsonStringNoReportingPeriod, NonSourceableInfo::class.java))
            .thenReturn(nonSourceableInfoNoReportingPeriod)
    }

    @Test
    fun `should process non sourceable message successfully`() {
        nonSourceableDataListener.processDataReportedNotSourceableMessage(nonSourceableJsonStringValid, type, correlationId)
        verify(mockNonSourceableDataManager).patchAllRequestsForThisDatasetToStatusNonSourceable(nonSourceableInfoValid, correlationId)
    }

    @Test
    fun `should throw exception for incomplete data`() {
        assertThrows<MessageQueueRejectException> {
            nonSourceableDataListener.processDataReportedNotSourceableMessage(nonSourceableJsonStringNoCompanyId, type, correlationId)
        }
        assertThrows<MessageQueueRejectException> {
            nonSourceableDataListener.processDataReportedNotSourceableMessage(nonSourceableJsonStringNoReportingPeriod, type, correlationId)
        }
    }

    @Test
    fun `should throw exception when isNonSourceable is false`() {
        assertThrows<MessageQueueRejectException> {
            nonSourceableDataListener
                .processDataReportedNotSourceableMessage(nonSourceableJsonStringValidButSourceable, type, correlationId)
        }
    }
}
