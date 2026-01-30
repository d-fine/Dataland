package org.dataland.datasourcingservice.repositories

import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import jakarta.transaction.Transactional
import org.dataland.datasourcingservice.entities.DataSourcingEntity
import org.dataland.datasourcingservice.entities.RequestEntity
import org.hibernate.envers.AuditReaderFactory
import org.hibernate.envers.DefaultRevisionEntity
import org.hibernate.envers.query.AuditEntity
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
     * List all revisions of an entity of the provided class type by its id with their revision timestamps.
     * E.g. providing the id of a data sourcing entity and the class type DataSourcingEntity::class.java
     * will return all previous versions of that data sourcing entity with their timestamps.
     * @param id the id of the entity
     * @param classType the class type of the entity
     * @return a list of pairs containing the entity and its revision timestamp (epoch milliseconds)
     */
    private fun <T> listDataRevisionsById(
        id: UUID?,
        classType: Class<T>,
    ): List<Pair<T, Long>> {
        val auditReader = AuditReaderFactory.get(entityManager)

        val resultList =
            auditReader
                .createQuery()
                .forRevisionsOfEntity(classType, false, false)
                .add(AuditEntity.id().eq(id))
                .resultList as List<Array<Any>>

        return resultList.map {
            val entity = it[0] as T
            val revisionEntity = it[1] as DefaultRevisionEntity
            Pair(entity, revisionEntity.timestamp)
        }
    }

    /**
     * List all previous versions of a DataSourcingEntity identified by its id with their revision timestamps.
     * @param id the id of the DataSourcingEntity
     * @return a list of pairs containing DataSourcingEntity revisions and their timestamps (epoch milliseconds)
     */
    fun listDataSourcingRevisionsById(id: UUID?): List<Pair<DataSourcingEntity, Long>> =
        listDataRevisionsById(id, DataSourcingEntity::class.java)

    /**
     * List all previous versions of a RequestEntity identified by its id with their revision timestamps.
     * @param id the id of the RequestEntity
     * @return a list of pairs containing RequestEntity revisions and their timestamps (epoch milliseconds)
     */
    fun listDataRequestRevisionsById(id: UUID?): List<Pair<RequestEntity, Long>> = listDataRevisionsById(id, RequestEntity::class.java)
}
