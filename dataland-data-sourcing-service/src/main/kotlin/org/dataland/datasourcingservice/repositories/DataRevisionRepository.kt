package org.dataland.datasourcingservice.repositories

import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import jakarta.transaction.Transactional
import org.dataland.datasourcingservice.entities.DataSourcingEntity
import org.dataland.datasourcingservice.entities.RequestEntity
import org.hibernate.envers.AuditReaderFactory
import org.springframework.stereotype.Repository
import java.util.UUID

/**
 * A Repository for managing historized data
 */

@Repository
@Transactional
class DataRevisionRepository(
    @PersistenceContext
    val entityManager: EntityManager,
) {
    /**
     * List all revisions of an entity by its id.
     * @param id the id of the entity
     * @param classType the class type of the entity
     * @return a list of arrays containing the entity and its revision information
     */
    private fun listDataRevisionsById(
        id: UUID?,
        classType: Class<*>,
    ): List<Any> {
        val auditReader = AuditReaderFactory.get(entityManager)

        val revisions = auditReader.getRevisions(classType, id)
        val entityList = revisions.map { revision -> auditReader.find(classType, id, revision) }

        return entityList
    }

    @Suppress("UNCHECKED_CAST")
    fun listDataSourcingRevisionsById(id: UUID?): List<DataSourcingEntity> =
        listDataRevisionsById(id, DataSourcingEntity::class.java) as List<DataSourcingEntity>

    @Suppress("UNCHECKED_CAST")
    fun listDataRequestRevisionsById(id: UUID?): List<RequestEntity> =
        listDataRevisionsById(id, RequestEntity::class.java) as List<RequestEntity>
}
