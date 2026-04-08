package org.dataland.datalandbackend.services

import org.dataland.datalandbackend.DatalandBackend
import org.dataland.datalandbackend.entities.NonSourceabilityInformationEntity
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.repositories.NonSourceabilityDataRepository
import org.dataland.datalandbackend.utils.DefaultMocks
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandmessagequeueutils.exceptions.MessageQueueRejectException
import org.dataland.datalandmessagequeueutils.model.NonSourceabilityEventType
import org.dataland.datalandmessagequeueutils.model.NonSourceabilityLifecycleEvent
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import java.time.Instant

/**
 * Tests for backend QA decision consumer behavior (T044: fail-fast; T027: accepted/rejected state application).
 */
@SpringBootTest(classes = [DatalandBackend::class], properties = ["spring.profiles.active=nodb"])
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@DefaultMocks
class NonSourceabilityQaDecisionConsumerTest(
    @Autowired private val nonSourceabilityDataRepository: NonSourceabilityDataRepository,
) {
    private fun buildEvent(
        nonSourceabilityId: String,
        eventType: NonSourceabilityEventType,
    ): NonSourceabilityLifecycleEvent =
        NonSourceabilityLifecycleEvent(
            nonSourceabilityId = nonSourceabilityId,
            companyId = "test-company",
            dataType = "eutaxonomy-financials",
            reportingPeriod = "2023",
            eventType = eventType,
        )

    private fun persistPendingEntity(): NonSourceabilityInformationEntity {
        val entity =
            NonSourceabilityInformationEntity(
                companyId = "test-company",
                dataType = DataType("eutaxonomy-financials"),
                reportingPeriod = "2023",
                qaStatus = QaStatus.Pending,
                uploaderUserId = "uploader-1",
                uploadTime = Instant.now().toEpochMilli(),
                currentlyActive = false,
                reason = null,
                bypassQa = false,
            )
        return nonSourceabilityDataRepository.save(entity)
    }

    // ─── T044: fail-fast validation ────────────────────────────────────────

    @Test
    fun `QA decision listener discards event with malformed (non-UUID) nonSourceabilityId and throws reject exception`() {
        val listener = NonSourceabilityQaDecisionListener(nonSourceabilityDataRepository)
        val event = buildEvent("not-a-uuid", NonSourceabilityEventType.NON_SOURCEABILITY_QA_ACCEPTED)
        assertThrows(MessageQueueRejectException::class.java) {
            listener.processQaDecisionEvent(event, "corr-id-001")
        }
    }

    @Test
    fun `QA decision listener discards event with unresolvable nonSourceabilityId and throws reject exception`() {
        val listener = NonSourceabilityQaDecisionListener(nonSourceabilityDataRepository)
        val event =
            buildEvent(
                "00000000-0000-0000-0000-000000000000",
                NonSourceabilityEventType.NON_SOURCEABILITY_QA_ACCEPTED,
            )
        assertThrows(MessageQueueRejectException::class.java) {
            listener.processQaDecisionEvent(event, "corr-id-002")
        }
    }

    // ─── T027: accepted/rejected state application ─────────────────────────

    @Test
    fun `QA decision listener sets qaStatus Accepted and currentlyActive true on accepted event`() {
        val saved = persistPendingEntity()
        val id = saved.nonSourceabilityId.toString()
        val listener = NonSourceabilityQaDecisionListener(nonSourceabilityDataRepository)
        val event = buildEvent(id, NonSourceabilityEventType.NON_SOURCEABILITY_QA_ACCEPTED)

        listener.processQaDecisionEvent(event, "corr-accepted")

        val updated = nonSourceabilityDataRepository.findById(saved.nonSourceabilityId!!).orElseThrow()
        assertTrue(updated.currentlyActive)
        assertTrue(updated.qaStatus == QaStatus.Accepted)
    }

    @Test
    fun `QA decision listener sets qaStatus Rejected and currentlyActive false on rejected event`() {
        val saved = persistPendingEntity()
        val id = saved.nonSourceabilityId.toString()
        val listener = NonSourceabilityQaDecisionListener(nonSourceabilityDataRepository)
        val event = buildEvent(id, NonSourceabilityEventType.NON_SOURCEABILITY_QA_REJECTED)

        listener.processQaDecisionEvent(event, "corr-rejected")

        val updated = nonSourceabilityDataRepository.findById(saved.nonSourceabilityId!!).orElseThrow()
        assertFalse(updated.currentlyActive)
        assertTrue(updated.qaStatus == QaStatus.Rejected)
    }

    @Test
    fun `QA decision listener throws reject exception for unexpected event type`() {
        val saved = persistPendingEntity()
        val id = saved.nonSourceabilityId.toString()
        val listener = NonSourceabilityQaDecisionListener(nonSourceabilityDataRepository)
        val event = buildEvent(id, NonSourceabilityEventType.NON_SOURCEABILITY_CREATED)
        assertThrows(MessageQueueRejectException::class.java) {
            listener.processQaDecisionEvent(event, "corr-unexpected")
        }
    }
}
