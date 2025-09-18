@file:Suppress("ktlint:standard:no-wildcard-imports")

package org.dataland.datasourcingservice

import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import jakarta.transaction.Transactional
import org.assertj.core.api.Assertions.assertThat
import org.dataland.datasourcingservice.entities.DataSourcingEntity
import org.dataland.datasourcingservice.model.enums.DataSourcingState
import org.dataland.datasourcingservice.repositories.DataSourcingRepository
import org.hibernate.envers.AuditReaderFactory
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.util.*

@SpringBootTest
@Testcontainers
class DataSourcingEntityAuditTests {
    companion object {
        @Container
        @JvmStatic
        val postgres = TestPostgresContainer.postgres

        @DynamicPropertySource
        @JvmStatic
        fun configureProperties(registry: DynamicPropertyRegistry) {
            TestPostgresContainer.configureProperties(registry)
        }
    }

    @PersistenceContext
    private lateinit var entityManager: EntityManager

    @Autowired
    private lateinit var dataSourcingRepository: DataSourcingRepository

    @Transactional
    @Test
    fun `test audit historization of DataSourcingEntity with updated states`() {
        val dataSourcingEntityId = UUID.randomUUID()
        val companyId = UUID.randomUUID()
        val initialDate = Date()

        // Step 1: Create and persist the entity in the Initialized state
        val entity =
            DataSourcingEntity(
                id = dataSourcingEntityId,
                companyId = companyId,
                reportingPeriod = "Q1-2023",
                dataType = "Financial",
                state = DataSourcingState.Initialized, // Updated with the new state
                documentIds = setOf(UUID.randomUUID(), UUID.randomUUID()),
                expectedPublicationDatesOfDocuments = null,
                dateDocumentSourcingAttempt = initialDate,
                documentCollector = UUID.randomUUID(),
                dataExtractor = UUID.randomUUID(),
                adminComment = "Initialized data sourcing",
                associatedRequests = null,
            )
        dataSourcingRepository.saveAndFlush(entity)
        // Step 2: Update the entity to DocumentSourcing state
        val updatedState1 = DataSourcingState.DocumentSourcing
        val updatedAdminComment1 = "Document sourcing in progress"
        entity.apply {
            state = updatedState1
            adminComment = updatedAdminComment1
        }
        dataSourcingRepository.saveAndFlush(entity)

        // Step 3: Update the entity to DataVerification state
        val updatedState2 = DataSourcingState.DataVerification
        val updatedAdminComment2 = "Data verification stage"
        entity.apply {
            state = updatedState2
            adminComment = updatedAdminComment2
        }
        dataSourcingRepository.saveAndFlush(entity)
        // Step 5: Verify historization using AuditReader
        val auditReader = AuditReaderFactory.get(entityManager)

        // Query revisions for the entity ID
        val revisions = auditReader.getRevisions(DataSourcingEntity::class.java, dataSourcingEntityId)
        assertThat(revisions).hasSize(3) // Create, Update 1, Update 2

        // Helper function to validate entity state at a specific revision
        fun validateRevision(
            revNumber: Number,
            expectedState: DataSourcingState,
            expectedAdminComment: String,
        ) {
            val entityAtRevision = auditReader.find(DataSourcingEntity::class.java, dataSourcingEntityId, revNumber)
            assertThat(entityAtRevision).isNotNull
            assertThat(entityAtRevision?.state).isEqualTo(expectedState)
            assertThat(entityAtRevision?.adminComment).isEqualTo(expectedAdminComment)
        }

        validateRevision(revisions[0], DataSourcingState.Initialized, "Initialized data sourcing")
        validateRevision(revisions[1], DataSourcingState.DocumentSourcing, updatedAdminComment1)
        validateRevision(revisions[2], DataSourcingState.DataVerification, updatedAdminComment2)
    }
}
