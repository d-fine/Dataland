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
     * List all revisions of an entity of the provided class type by its id.
     * When withTimestamps is true, queries revision metadata and returns pairs of (entity, timestamp).
     * When false, queries entities only and returns pairs with a dummy timestamp of 0L.
     * @param id the id of the entity
     * @param classType the class type of the entity
     * @param withTimestamps whether to query and return revision timestamps
     * @return a list of pairs containing the entity and its revision timestamp (epoch milliseconds), or 0L if not queried
     */
    private fun <T> listDataRevisionsById(
        id: UUID?,
        classType: Class<T>,
        withTimestamps: Boolean,
    ): List<Pair<T, Long>> {
        val auditReader = AuditReaderFactory.get(entityManager)

        val query =
            auditReader
                .createQuery()
                .forRevisionsOfEntity(classType, !withTimestamps, false)
                .add(AuditEntity.id().eq(id))

        if (withTimestamps) {
            return query.resultList.filterIsInstance<Array<Any>>().map {
                val entity = classType.cast(it[0])
                val revisionEntity = it[1] as DefaultRevisionEntity
                Pair(entity, revisionEntity.timestamp)
            }
        }
        return (query.resultList as List<T>).map { Pair(it, 0L) }
    }

    /**
     * List all previous versions of a DataSourcingEntity identified by its id with their revision timestamps.
     * @param id the id of the DataSourcingEntity
     * @return a list of pairs containing DataSourcingEntity revisions and their timestamps (epoch milliseconds)
     */
    fun listDataSourcingRevisionsById(id: UUID?): List<Pair<DataSourcingEntity, Long>> =
        listDataRevisionsById(id, DataSourcingEntity::class.java, withTimestamps = true)

    /**
     * List all previous versions of a RequestEntity identified by its id.
     * @param id the id of the RequestEntity
     * @return a list of RequestEntity revisions
     */
    fun listDataRequestRevisionsById(id: UUID?): List<RequestEntity> =
        listDataRevisionsById(id, RequestEntity::class.java, withTimestamps = false).map { it.first }
}
