package org.dataland.datalandbackend.services

import com.zaxxer.hikari.HikariDataSource
import org.dataland.datalandbackend.entities.IsinLeiEntity
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.sql.PreparedStatement
import java.util.concurrent.Callable
import java.util.concurrent.Executors

/**
 * Implementation of a ISIN manager for Dataland
 */
@Service
class IsinLeiManager {
    @Autowired
    private lateinit var hikariDataSource: HikariDataSource

    /**
     * Method to save ISIN-LEI mappings in batches.
     * @param entities the ISIN-LEI mappings to save
     */
    fun saveAllJdbcBatch(
        entities: List<IsinLeiEntity>,
        batchSize: Int = 100,
    ) {
        val tableName = "isin_lei_mapping"
        val sql = """INSERT INTO $tableName (isin, lei) VALUES (?, ?)"""

        hikariDataSource.connection.use { connection ->
            connection.prepareStatement(sql).use { statement ->
                executeBatchInsert(statement, entities, batchSize)
            }
        }
    }

    /**
     * Executes a batch insert for ISIN-LEI mappings.
     * @param statement the prepared statement to execute
     * @param entities the list of ISIN-LEI entities to insert
     * @param batchSize the size of each batch to insert
     */
    private fun executeBatchInsert(
        statement: PreparedStatement,
        entities: List<IsinLeiEntity>,
        batchSize: Int,
    ) {
        var counter = 0
        for (entity in entities) {
            statement.clearParameters()
            statement.setString(1, entity.isin)
            statement.setString(2, entity.lei)
            statement.addBatch()

            if ((counter + 1) % batchSize == 0 || (counter + 1) == entities.size) {
                statement.executeBatch()
                statement.clearBatch()
            }
            counter++
        }
    }

    /**
     * Method to save ISIN-LEI mappings in chunks using a callable for asynchronous execution.
     * @param entities the ISIN-LEI mappings to save
     * @param chunkSize the size of each chunk to process in parallel
     */
    @Async
    fun saveAllJdbcBatchCallable(
        entities: List<IsinLeiEntity>,
        chunkSize: Int = 10000,
    ) {
        val executorService = Executors.newFixedThreadPool(hikariDataSource.maximumPoolSize)
        val chunks = entities.chunked(chunkSize)
        val tasks =
            chunks.map { chunk ->
                Callable<Void> {
                    saveAllJdbcBatch(chunk)
                    null
                }
            }

        executorService.invokeAll(tasks)
    }

    /**
     * Method to remove all ISIN-LEI mappings.
     */
    @Transactional
    fun clearAllMappings() {
        val sql = """TRUNCATE TABLE isin_lei_mapping"""
        hikariDataSource.connection.use { connection ->
            connection.createStatement().use { statement ->
                statement.executeUpdate(sql)
            }
        }
    }
}
