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
     * List all revisions of an entity of the provided class type by its id. E.g. providing the id of a data sourcing
     * entity and the class type DataSourcingEntity::class.java will return all previous versions of that data sourcing entity,
     * @param id the id of the entity
     * @param classType the class type of the entity
     * @return a list of arrays containing the entity and its revision information
     */
    private fun <T> listDataRevisionsById(
        id: UUID?,
        classType: Class<T>,
    ): List<T> {
        val auditReader = AuditReaderFactory.get(entityManager)

        val revisions = auditReader.getRevisions(classType, id)
        val entityList = revisions.map { revision -> auditReader.find(classType, id, revision) }

        return entityList
    }

    /**
     * List all previous versions of a DataSourcingEntity identified by its id.
     * @param id the id of the DataSourcingEntity
     * @return a list of DataSourcingEntity containing all revisions
     */
    fun listDataSourcingRevisionsById(id: UUID?): List<DataSourcingEntity> = listDataRevisionsById(id, DataSourcingEntity::class.java)

    /**
     * List all previous versions of a RequestEntity identified by its id.
     * @param id the id of the RequestEntity
     * @return a list of RequestEntity containing all revisions
     */
    fun listDataRequestRevisionsById(id: UUID?): List<RequestEntity> = listDataRevisionsById(id, RequestEntity::class.java)
}
