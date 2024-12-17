package org.dataland.datalandqaservice.controller

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.transaction.Transactional
import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.api.DataPointControllerApi
import org.dataland.datalandbackend.openApiClient.model.CompanyInformation
import org.dataland.datalandbackend.openApiClient.model.DataPointMetaInformation
import org.dataland.datalandbackend.openApiClient.model.StoredCompany
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
import org.dataland.datalandmessagequeueutils.messages.QaStatusChangeMessage
import org.dataland.datalandqaservice.DatalandQaService
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.DataPointQaReviewInformation
import org.dataland.datalandqaservice.utils.UtilityFunctions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import java.util.UUID
import org.dataland.datalandbackend.openApiClient.model.QaStatus as OpenApiClientQaStatus
import org.dataland.datalandbackendutils.model.QaStatus as BackendUtilsQaStatus

@Transactional
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@SpringBootTest(
    classes = [
        DatalandQaService::class,
    ],
    properties = ["spring.profiles.active=nodb"],
)
class QaControllerTest(
    @Autowired private val qaController: QaController,
    @Autowired private val objectMapper: ObjectMapper,
) {
    @MockBean
    private lateinit var dataPointControllerApi: DataPointControllerApi

    @MockBean
    private lateinit var companyControllerApi: CompanyDataControllerApi

    @MockBean
    private lateinit var cloudEventMessageHandler: CloudEventMessageHandler

    val dataId = UUID.randomUUID().toString()
    val originalActiveDataId = UUID.randomUUID().toString()
    val dataPointIdentifier = "some-identifier"
    val reportingPeriod = "2022"
    val companyId = UUID.randomUUID().toString()
    val companyName = "some-company"
    val firstComment = "OriginalActive"

    private fun specifyMocks() {
        `when`(dataPointControllerApi.getDataPointMetaInfo(any())).thenReturn(
            DataPointMetaInformation(
                dataId = "dummy",
                dataPointIdentifier = dataPointIdentifier,
                companyId = companyId,
                reportingPeriod = reportingPeriod,
                qaStatus = OpenApiClientQaStatus.Accepted,
                currentlyActive = true,
                uploadTime = 0,
            ),
        )

        `when`(companyControllerApi.getCompanyById(companyId)).thenReturn(
            StoredCompany(
                companyId = companyId,
                companyInformation =
                    CompanyInformation(
                        companyName = companyName,
                        headquarters = "some-headquarters",
                        countryCode = "some-country",
                        identifiers = mapOf("LEI" to listOf("some-lei")),
                    ),
                dataRegisteredByDataland = emptyList(),
            ),
        )

        `when`(cloudEventMessageHandler.buildCEMessageAndSendToQueue(any(), any(), any(), any(), any()))
            .thenAnswer { println("Sending message to queue") }
    }

    private fun createMessageBody(
        dataId: String,
        updatedQaStatus: BackendUtilsQaStatus,
        currentlyActiveDataId: String,
    ): String =
        objectMapper.writeValueAsString(
            QaStatusChangeMessage(
                dataId = dataId,
                updatedQaStatus = updatedQaStatus,
                currentlyActiveDataId = currentlyActiveDataId,
            ),
        )

    private fun getReviewEntries(onlyLatest: Boolean): List<DataPointQaReviewInformation> =
        qaController
            .getDataPointQaReviewInformation(
                companyId = companyId,
                dataPointIdentifier = dataPointIdentifier,
                reportingPeriod = reportingPeriod,
                qaStatus = null,
                onlyLatest = onlyLatest,
                chunkSize = 10,
                chunkIndex = 0,
            ).body!!

    @Test
    fun `verify that the various endpoints return the correct order and content for data point QA review entries`() {
        specifyMocks()
        val expectedBodyForNewSetToActive = createMessageBody(dataId, BackendUtilsQaStatus.Accepted, dataId)
        val expectedBodyForOriginalSetToActive: String = createMessageBody(dataId, BackendUtilsQaStatus.Rejected, originalActiveDataId)
        UtilityFunctions.withReviewerAuthentication {
            qaController.changeDataPointQaStatus(originalActiveDataId, BackendUtilsQaStatus.Accepted, firstComment)
            qaController.changeDataPointQaStatus(dataId, BackendUtilsQaStatus.Pending, "Pending")
            qaController.changeDataPointQaStatus(dataId, BackendUtilsQaStatus.Accepted, "Accepted")
            qaController.changeDataPointQaStatus(dataId, BackendUtilsQaStatus.Rejected, "Rejected")

            verify(cloudEventMessageHandler, times(1)).buildCEMessageAndSendToQueue(
                eq(expectedBodyForNewSetToActive),
                eq(MessageType.QA_STATUS_CHANGED),
                any(),
                eq(ExchangeName.QA_SERVICE_DATA_QUALITY_EVENTS),
                eq(RoutingKeyNames.DATA_POINT_QA),
            )

            verify(cloudEventMessageHandler, times(1)).buildCEMessageAndSendToQueue(
                eq(expectedBodyForOriginalSetToActive),
                eq(MessageType.QA_STATUS_CHANGED),
                any(),
                eq(ExchangeName.QA_SERVICE_DATA_QUALITY_EVENTS),
                eq(RoutingKeyNames.DATA_POINT_QA),
            )

            val reviewEntries = qaController.getDataPointQaReviewInformationByDataId(dataId).body!!
            assertEquals(3, reviewEntries.size)
            assertEquals(BackendUtilsQaStatus.Rejected, reviewEntries.first().qaStatus)

            val latestReviewEntries = getReviewEntries(onlyLatest = true)
            assertEquals(2, latestReviewEntries.size)
            assertEquals(BackendUtilsQaStatus.Rejected, latestReviewEntries.first().qaStatus)

            val allReviewEntries = getReviewEntries(onlyLatest = false)
            assertEquals(4, allReviewEntries.size)
            assertEquals(firstComment, allReviewEntries.last().comment)
        }
    }
}
