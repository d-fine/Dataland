package org.dataland.datalandbackend.services

import org.dataland.datalandbackend.entities.IsinLeiEntity
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.sql.BatchUpdateException
import java.sql.PreparedStatement
import javax.sql.DataSource
import kotlin.use

@Service
class IsinLeiTransactionalService(
    @Autowired private val dataSource: DataSource,
) {
    private val tableName = "isin_lei_mapping"
    private val logger = LoggerFactory.getLogger(javaClass)

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
     * Method to save ISIN-LEI mappings in batches.
     * @param entities the ISIN-LEI mappings to save
     */
    @Transactional
    fun saveAllJdbcBatch(
        entities: List<IsinLeiEntity>,
        batchSize: Int = 50,
    ) {
        val sql = """INSERT INTO $tableName (company_id, isin, lei) VALUES (?, ?, ?)"""
        dataSource.connection.use { connection ->
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
            statement.setString(1, entity.company?.companyId)
            statement.setString(2, entity.isin)
            @Suppress("MagicNumber")
            statement.setString(3, entity.lei)
            statement.addBatch()

            if ((counter + 1) % batchSize == 0 || (counter + 1) == entities.size) {
                try {
                    statement.executeBatch()
                    statement.clearBatch()
                } catch (e: BatchUpdateException) {
                    logger.error("Error executing batch insert: ${e.message}", e)
                }
                logger.info("Inserted ${counter + 1} / ${entities.size} records so far")
            }
            counter++
        }
    }
}
