package org.dataland.datalandqaservice.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandmessagequeueutils.cloudevents.CloudEventMessageHandler
import org.dataland.datalandmessagequeueutils.constants.ExchangeName
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.constants.RoutingKeyNames
import org.dataland.datalandmessagequeueutils.model.NonSourceabilityEventType
import org.dataland.datalandmessagequeueutils.model.NonSourceabilityLifecycleEvent
import org.dataland.datalandqaservice.DatalandQaService
import org.dataland.datalandqaservice.entities.NonSourceableQaReviewInformationEntity
import org.dataland.datalandqaservice.repositories.NonSourceableQaReviewRepository
import org.dataland.keycloakAdapter.auth.DatalandRealmRole
import org.dataland.keycloakAdapter.utils.AuthenticationMock
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Transactional
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@SpringBootTest(
    classes = [DatalandQaService::class],
    properties = ["spring.profiles.active=nodb"],
)
class NonSourceabilityQaControllerTest(
    @Autowired private val nonSourceabilityQaController: NonSourceabilityQaController,
    @Autowired private val nonSourceableQaReviewRepository: NonSourceableQaReviewRepository,
    @Autowired private val objectMapper: ObjectMapper,
) {
    @MockitoBean private lateinit var cloudEventMessageHandler: CloudEventMessageHandler

    @BeforeEach
    fun setup() {
        nonSourceableQaReviewRepository.deleteAll()
        mockSecurityContext(
            userId = "reviewer-1",
            roles = setOf(DatalandRealmRole.ROLE_USER, DatalandRealmRole.ROLE_REVIEWER),
        )
    }

    @Test
    fun `accepted decision persists and emits QA accepted lifecycle event`() {
        val nonSourceabilityId = UUID.randomUUID()
        nonSourceableQaReviewRepository.save(
            NonSourceableQaReviewInformationEntity(
                nonSourceabilityId = nonSourceabilityId,
                companyId = UUID.randomUUID().toString(),
                dataType = "sfdr",
                reportingPeriod = "2025",
                qaStatus = QaStatus.Pending,
                reason = "No source",
                uploaderUserId = "uploader-1",
                uploadTime = 100L,
            ),
        )

        val response =
            nonSourceabilityQaController
                .decideNonSourceability(
                    nonSourceabilityId = nonSourceabilityId.toString(),
                    qaStatus = QaStatus.Accepted,
                    qaComment = "Approved",
                ).body

        assertNotNull(response)
        assertEquals(QaStatus.Accepted, response!!.qaStatus)
        assertEquals("Approved", response.qaComment)
        assertEquals("reviewer-1", response.reviewerUserId)

        val persisted = nonSourceableQaReviewRepository.findById(nonSourceabilityId).get()
        assertEquals(QaStatus.Accepted, persisted.qaStatus)
        assertEquals("Approved", persisted.qaComment)
        assertEquals("reviewer-1", persisted.reviewerUserId)

        val bodyCaptor = argumentCaptor<String>()
        verify(cloudEventMessageHandler).buildCEMessageAndSendToQueue(
            bodyCaptor.capture(),
            eq(MessageType.NON_SOURCEABILITY_LIFECYCLE),
            any(),
            eq(ExchangeName.BACKEND_DATA_NONSOURCEABLE),
            eq(RoutingKeyNames.NON_SOURCEABILITY_LIFECYCLE),
        )

        val emittedEvent = objectMapper.readValue(bodyCaptor.firstValue, NonSourceabilityLifecycleEvent::class.java)
        assertEquals(nonSourceabilityId.toString(), emittedEvent.nonSourceabilityId)
        assertEquals(NonSourceabilityEventType.QA_ACCEPTED, emittedEvent.eventType)
        assertEquals(QaStatus.Accepted, emittedEvent.qaStatus)
        assertTrue(emittedEvent.currentlyActive == true)
    }

    @Test
    fun `rejected decision persists comment and emits QA rejected lifecycle event`() {
        val nonSourceabilityId = UUID.randomUUID()
        nonSourceableQaReviewRepository.save(
            NonSourceableQaReviewInformationEntity(
                nonSourceabilityId = nonSourceabilityId,
                companyId = UUID.randomUUID().toString(),
                dataType = "sfdr",
                reportingPeriod = "2025",
                qaStatus = QaStatus.Pending,
                reason = "insufficient evidence",
                uploaderUserId = "uploader-2",
                uploadTime = 200L,
            ),
        )

        val response =
            nonSourceabilityQaController
                .decideNonSourceability(
                    nonSourceabilityId = nonSourceabilityId.toString(),
                    qaStatus = QaStatus.Rejected,
                    qaComment = "Please provide additional source documents",
                ).body

        assertNotNull(response)
        assertEquals(QaStatus.Rejected, response!!.qaStatus)
        assertEquals("Please provide additional source documents", response.qaComment)
        assertEquals("reviewer-1", response.reviewerUserId)

        val persisted = nonSourceableQaReviewRepository.findById(nonSourceabilityId).get()
        assertEquals(QaStatus.Rejected, persisted.qaStatus)
        assertEquals("Please provide additional source documents", persisted.qaComment)
        assertEquals("reviewer-1", persisted.reviewerUserId)

        val bodyCaptor = argumentCaptor<String>()
        verify(cloudEventMessageHandler).buildCEMessageAndSendToQueue(
            bodyCaptor.capture(),
            eq(MessageType.NON_SOURCEABILITY_LIFECYCLE),
            any(),
            eq(ExchangeName.BACKEND_DATA_NONSOURCEABLE),
            eq(RoutingKeyNames.NON_SOURCEABILITY_LIFECYCLE),
        )

        val emittedEvent = objectMapper.readValue(bodyCaptor.lastValue, NonSourceabilityLifecycleEvent::class.java)
        assertEquals(nonSourceabilityId.toString(), emittedEvent.nonSourceabilityId)
        assertEquals(NonSourceabilityEventType.QA_REJECTED, emittedEvent.eventType)
        assertEquals(QaStatus.Rejected, emittedEvent.qaStatus)
        assertTrue(emittedEvent.currentlyActive == false)
    }

    @Test
    fun `non-sourceability list and queue endpoints filter by status`() {
        val pendingId = UUID.randomUUID()
        val acceptedId = UUID.randomUUID()
        val rejectedId = UUID.randomUUID()

        nonSourceableQaReviewRepository.save(
            NonSourceableQaReviewInformationEntity(
                nonSourceabilityId = pendingId,
                companyId = UUID.randomUUID().toString(),
                dataType = "sfdr",
                reportingPeriod = "2025",
                qaStatus = QaStatus.Pending,
                reason = "Pending",
                uploaderUserId = "uploader-1",
                uploadTime = 10L,
            ),
        )
        nonSourceableQaReviewRepository.save(
            NonSourceableQaReviewInformationEntity(
                nonSourceabilityId = acceptedId,
                companyId = UUID.randomUUID().toString(),
                dataType = "sfdr",
                reportingPeriod = "2024",
                qaStatus = QaStatus.Accepted,
                reason = "Accepted",
                uploaderUserId = "uploader-1",
                uploadTime = 20L,
            ),
        )
        nonSourceableQaReviewRepository.save(
            NonSourceableQaReviewInformationEntity(
                nonSourceabilityId = rejectedId,
                companyId = UUID.randomUUID().toString(),
                dataType = "sfdr",
                reportingPeriod = "2023",
                qaStatus = QaStatus.Rejected,
                reason = "Rejected",
                uploaderUserId = "uploader-1",
                uploadTime = 30L,
            ),
        )

        val pendingOnly =
            nonSourceabilityQaController
                .getNonSourceabilityReviews(
                    companyId = null,
                    dataType = null,
                    reportingPeriod = null,
                    qaStatus = QaStatus.Pending,
                    showOnlyActive = true,
                    chunkSize = 10,
                    chunkIndex = 0,
                ).body!!
        assertEquals(1, pendingOnly.size)
        assertEquals(pendingId.toString(), pendingOnly.first().nonSourceabilityId)

        val pendingQueue = nonSourceabilityQaController.getPendingNonSourceabilityQueue().body!!
        assertEquals(1, pendingQueue.size)
        assertEquals(pendingId.toString(), pendingQueue.first().nonSourceabilityId)
    }

    private fun mockSecurityContext(
        userId: String,
        roles: Set<DatalandRealmRole>,
    ) {
        val authentication = AuthenticationMock.mockJwtAuthentication("mocked-reviewer", userId, roles)
        val securityContext = mock<SecurityContext>()
        whenever(securityContext.authentication).thenReturn(authentication)
        SecurityContextHolder.setContext(securityContext)
    }
}
