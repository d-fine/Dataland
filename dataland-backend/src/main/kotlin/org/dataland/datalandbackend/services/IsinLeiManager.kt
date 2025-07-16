package org.dataland.datalandbackend.services

import org.dataland.datalandbackend.entities.IsinLeiEntity
import org.dataland.datalandbackend.model.IsinLeiMappingData
import org.dataland.datalandbackend.repositories.CompanyIdentifierRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.sql.PreparedStatement
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import javax.sql.DataSource

/**
 * Implementation of a ISIN manager for Dataland
 */
@Service
class IsinLeiManager(
    @Autowired private val companyIdentifierRepository: CompanyIdentifierRepository,
    @Autowired private val dataSource: DataSource,
    @Value("\${spring.datasource.hikari.maximum-pool-size}")
    private val dataSourceMaximumPoolSize: Int,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Method to put an ISIN-LEI mapping into the database.
     * This method clears all previous mappings and adds new ones.
     * @param isinLeiMappingData the list of ISIN-LEI mapping data to be put
     */
    @Async
    @Transactional
    fun putIsinLeiMapping(isinLeiMappingData: List<IsinLeiMappingData>) {
        logger.info("Start dropping previous entries")
        clearAllMappings()
        logger.info("Dropped previous entries")
        logger.info("Preparing to add new ISIN-LEI mappings: ${isinLeiMappingData.size} entries")
        val entities = isinLeiMappingData.map { convertToIsinLeiEntity(it) }
        saveAllJdbcBatchCallable(entities)
        logger.info("Added new ISIN-LEI mappings: ${isinLeiMappingData.size} entries")
    }

    /**
     * Converts a list of [IsinLeiMappingData] to a list of [IsinLeiEntity].
     * @param isinLeiMappingData the list of ISIN-LEI mapping data
     * @return the list of ISIN-LEI entities
     */
    private fun convertToIsinLeiEntity(isinLeiMappingData: IsinLeiMappingData): IsinLeiEntity {
        val companyId =
            companyIdentifierRepository
                .findByIdentifierValueAndIdentifierType(
                    identifierValue = isinLeiMappingData.lei,
                )?.company
                ?.companyId
                ?: throw IllegalArgumentException("Company with LEI ${isinLeiMappingData.lei} not found")

        return IsinLeiEntity(
            companyId = companyId,
            isin = isinLeiMappingData.isin,
            lei = isinLeiMappingData.lei,
        )
    }

    /**
     * Method to save ISIN-LEI mappings in batches.
     * @param entities the ISIN-LEI mappings to save
     */
    fun saveAllJdbcBatch(
        entities: List<IsinLeiEntity>,
        batchSize: Int = 100,
    ) {
        val tableName = "isin_lei_mapping"
        val sql = "INSERT INTO $tableName (company_id, isin, lei) VALUES (?, ?, ?)"

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
            statement.setString(1, entity.companyId)
            statement.setString(2, entity.isin)
            @Suppress("MagicNumber")
            statement.setString(3, entity.lei)
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
    private fun saveAllJdbcBatchCallable(
        entities: List<IsinLeiEntity>,
        chunkSize: Int = 10000,
    ) {
        val executorService = Executors.newFixedThreadPool(dataSourceMaximumPoolSize)
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
    private fun clearAllMappings() {
        val sql = """TRUNCATE TABLE isin_lei_mapping"""
        dataSource.connection.use { connection ->
            connection.createStatement().use { statement ->
                statement.executeUpdate(sql)
            }
        }
    }
}
