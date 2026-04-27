package org.dataland.datalandbackend.services

import org.dataland.datalandbackend.DatalandBackend
import org.dataland.datalandbackend.entities.NonSourceabilityInformationEntity
import org.dataland.datalandbackend.model.DataType
import org.dataland.datalandbackend.repositories.NonSourceabilityDataRepository
import org.dataland.datalandbackend.utils.DefaultMocks
import org.dataland.datalandbackendutils.model.QaStatus
import org.dataland.datalandmessagequeueutils.constants.MessageType
import org.dataland.datalandmessagequeueutils.exceptions.MessageQueueRejectException
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
 * Tests for backend QA decision consumer behavior: fail-fast validation and accepted/rejected state application.
 */
@SpringBootTest(classes = [DatalandBackend::class], properties = ["spring.profiles.active=nodb"])
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@DefaultMocks
class NonSourceabilityQaDecisionConsumerTest(
    @Autowired private val nonSourceabilityDataRepository: NonSourceabilityDataRepository,
) {
    private fun buildEvent(nonSourceabilityId: String): NonSourceabilityLifecycleEvent =
        NonSourceabilityLifecycleEvent(
            nonSourceabilityId = nonSourceabilityId,
            companyId = "test-company",
            dataType = "eutaxonomy-financials",
            reportingPeriod = "2023",
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

    // ─── fail-fast validation ────────────────────────────────────────────

    @Test
    fun `qa decision listener discards event with malformed nonUUID nonSourceabilityId and throws reject exception`() {
        val listener = NonSourceabilityQaDecisionListener(nonSourceabilityDataRepository)
        val event = buildEvent("not-a-uuid")
        assertThrows(MessageQueueRejectException::class.java) {
            listener.processQaDecisionEvent(event, MessageType.NON_SOURCEABILITY_QA_ACCEPTED)
        }
    }

    @Test
    fun `qa decision listener discards event with unresolvable nonSourceabilityId and throws reject exception`() {
        val listener = NonSourceabilityQaDecisionListener(nonSourceabilityDataRepository)
        val event = buildEvent("00000000-0000-0000-0000-000000000000")
        assertThrows(MessageQueueRejectException::class.java) {
            listener.processQaDecisionEvent(event, MessageType.NON_SOURCEABILITY_QA_ACCEPTED)
        }
    }

    // ─── accepted/rejected state application ─────────────────────────────

    @Test
    fun `qa decision listener sets qaStatus Accepted and currentlyActive true on accepted event`() {
        val saved = persistPendingEntity()
        val id = saved.nonSourceabilityId.toString()
        val listener = NonSourceabilityQaDecisionListener(nonSourceabilityDataRepository)
        val event = buildEvent(id)

        listener.processQaDecisionEvent(event, MessageType.NON_SOURCEABILITY_QA_ACCEPTED)

        val updated = nonSourceabilityDataRepository.findById(saved.nonSourceabilityId!!).orElseThrow()
        assertTrue(updated.currentlyActive)
        assertTrue(updated.qaStatus == QaStatus.Accepted)
    }

    @Test
    fun `qa decision listener sets qaStatus Rejected and currentlyActive false on rejected event`() {
        val saved = persistPendingEntity()
        val id = saved.nonSourceabilityId.toString()
        val listener = NonSourceabilityQaDecisionListener(nonSourceabilityDataRepository)
        val event = buildEvent(id)

        listener.processQaDecisionEvent(event, MessageType.NON_SOURCEABILITY_QA_REJECTED)

        val updated = nonSourceabilityDataRepository.findById(saved.nonSourceabilityId!!).orElseThrow()
        assertFalse(updated.currentlyActive)
        assertTrue(updated.qaStatus == QaStatus.Rejected)
    }

    @Test
    fun `qa decision listener throws reject exception for unexpected event type`() {
        val saved = persistPendingEntity()
        val id = saved.nonSourceabilityId.toString()
        val listener = NonSourceabilityQaDecisionListener(nonSourceabilityDataRepository)
        val event = buildEvent(id)
        assertThrows(MessageQueueRejectException::class.java) {
            listener.processQaDecisionEvent(event, MessageType.NON_SOURCEABILITY_CREATED)
        }
    }
}
