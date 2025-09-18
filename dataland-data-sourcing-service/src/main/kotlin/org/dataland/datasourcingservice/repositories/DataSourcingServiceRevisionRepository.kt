package org.dataland.datasourcingservice.repositories

import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.hibernate.envers.AuditReaderFactory
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

/**
 * A JPA Repository for managing RequestEntity instances.
 */

@Repository
@Transactional
class DataSourcingServiceRevisionRepository(
    @PersistenceContext
    val entityManager: EntityManager,
) {
    fun listDataSourcingRevisionsById(
        id: UUID?,
        classType: Class<*>,
    ): List<Array<*>> {
        val auditReader = AuditReaderFactory.get(entityManager)

        val revisions = auditReader.getRevisions(classType, id)
        val entityList = revisions.map { revision -> auditReader.find(classType, id, revision) }

        @Suppress("UNCHECKED_CAST")
        return entityList as List<Array<*>>
    }
}
