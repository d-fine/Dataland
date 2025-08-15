package org.dataland.datalandbackend.services

import org.dataland.datalandbackend.entities.IsinLeiEntity
import org.dataland.datalandbackend.repositories.IsinLeiRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.concurrent.CompletableFuture
import javax.sql.DataSource

/** Service for managing ISIN-LEI mappings in a transactional manner.
 * This service provides methods to clear all mappings and save mappings in batches.
 */
@Service
class IsinLeiTransactionalService(
    @Autowired private val dataSource: DataSource,
    @Autowired private val isinLeiRepository: IsinLeiRepository,
) {
    private val tableName = "isin_lei_mapping"

    /**
     * Method to remove all ISIN-LEI mappings.
     */
    @Transactional
    fun clearAllMappings() {
        val sql = """TRUNCATE TABLE $tableName"""
        dataSource.connection.use { connection ->
            connection.createStatement().use { statement ->
                statement.executeUpdate(sql)
            }
        }
    }

    /**
     * Method to save ISIN-LEI mappings in batches using JPA/Hibernate.
     * @param entities the ISIN-LEI mappings to save
     */
    @Async
    @Transactional
    fun saveAllJpaHibernate(entities: List<IsinLeiEntity>): CompletableFuture<Unit> {
        isinLeiRepository.saveAll(entities)
        return CompletableFuture.completedFuture(null)
    }
}
