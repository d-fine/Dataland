package org.dataland.datalandcommunitymanager.services.messaging

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.dataland.datalandcommunitymanager.entities.NotificationEventEntity
import org.dataland.datalandcommunitymanager.events.NotificationEventType
import org.dataland.datalandcommunitymanager.utils.CompanyInfoService
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.datalandmessagequeueutils.messages.email.DataRequestSummaryEmailContent
import org.dataland.datalandmessagequeueutils.messages.email.EmailMessage
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.reset
import org.mockito.kotlin.spy
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.UUID

class DataRequestSummaryEmailBuilderTest {
    private val mockCloudEventMessageHandler = mock<CloudEventMessageHandler>()
    private val mockCompanyInfoService = mock<CompanyInfoService>()
    private val spyObjectMapper = spy<ObjectMapper>()
    private lateinit var dataRequestSummaryEmailBuilder: DataRequestSummaryEmailBuilder

    private val dummyCompanyName = "Dummy Company"
    private val sampleCompanyId = UUID.randomUUID()
    private val userId = UUID.randomUUID()

    @BeforeEach
    fun setUp() {
        reset(
            mockCloudEventMessageHandler,
            mockCompanyInfoService,
            spyObjectMapper,
        )

        doReturn(dummyCompanyName).whenever(mockCompanyInfoService).getValidCompanyNameOrId(any())

        dataRequestSummaryEmailBuilder =
            DataRequestSummaryEmailBuilder(
                mockCloudEventMessageHandler,
                mockCompanyInfoService,
                spyObjectMapper,
            )
    }

    @ParameterizedTest
    @CsvSource(
        delimiter = ';',
        value = [
            "sfdr;SFDR",
            "eutaxonomy-financials;EU Taxonomy for financial companies",
            "euTaxonomy-non-financials;EU Taxonomy for non-financial companies",
            "nuclear-and-gas;EU Taxonomy Nuclear and Gas",
            "p2p;WWF Pathways to Paris",
            "lksg;LkSG",
            "additional-company-information;Additional Company Information",
            "vsme;VSME",
            "esg-datenkatalog;ESG Datenkatalog",
            "heimathafen;Heimathafen",
        ],
    )
    fun `check that data request summary emails contain the human readable names of frameworks`(
        dataTypeName: String,
        dataTypeHumanReadableName: String,
    ) {
        val dataTypeDecoded = DataTypeEnum.decode(dataTypeName)
        if (dataTypeDecoded == null) {
            throw InvalidInputApiException(
                summary = "Data type could not be decoded.",
                message = "The string $dataTypeName does not correspond to a known data type on Dataland.",
            )
        }

        val unprocessedEvent =
            NotificationEventEntity(
                notificationEventType = NotificationEventType.AvailableEvent,
                userId = userId,
                isProcessed = false,
                companyId = sampleCompanyId,
                framework = dataTypeDecoded,
                reportingPeriod = "2024",
            )

        dataRequestSummaryEmailBuilder.buildDataRequestSummaryEmailAndSendCEMessage(
            listOf(unprocessedEvent),
            userId,
        )

        verify(spyObjectMapper).writeValueAsString(
            argThat { message ->
                (message as? EmailMessage)?.typedEmailContent?.let { content ->
                    content is DataRequestSummaryEmailContent &&
                        content.newData.first().dataTypeLabel == dataTypeHumanReadableName
                } ?: false
            },
        )
    }
}
