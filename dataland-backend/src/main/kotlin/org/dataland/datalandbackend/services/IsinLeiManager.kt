package org.dataland.datalandbackend.services

import org.dataland.datalandbackend.entities.IsinLeiEntity
import org.dataland.datalandbackend.entities.StoredCompanyEntity
import org.dataland.datalandbackend.model.IsinLeiMappingData
import org.dataland.datalandbackend.model.enums.company.IdentifierType
import org.dataland.datalandbackend.repositories.StoredCompanyRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.sql.BatchUpdateException
import java.sql.PreparedStatement
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import javax.sql.DataSource

/**
 * Implementation of a ISIN manager for Dataland
 */
@Service
class IsinLeiManager(
    @Autowired private val storedCompanyRepository: StoredCompanyRepository,
    @Autowired private val dataSource: DataSource,
    @Value("\${spring.datasource.hikari.maximum-pool-size}")
    private val dataSourceMaximumPoolSize: Int,
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val tableName = "isin_lei_mapping"

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
        saveAllJdbcBatchCallable(isinLeiMappingData)
        logger.info("Added new ISIN-LEI mappings: ${isinLeiMappingData.size} entries")
    }

    /**
     * Converts a list of [IsinLeiMappingData] to a list of [IsinLeiEntity].
     * @param isinLeiMappingData the list of ISIN-LEI mapping data
     * @return the list of ISIN-LEI entities
     */
    private fun convertToIsinLeiEntity(
        isinLeiMappingData: List<IsinLeiMappingData>,
        companies: List<StoredCompanyEntity>?,
    ): List<IsinLeiEntity> {
        val entities = mutableListOf<IsinLeiEntity>()
        isinLeiMappingData.forEach { mappingData ->
            val company =
                companies?.first {
                    it.identifiers
                        .filter { id -> id.identifierType == IdentifierType.Lei }
                        .map { id -> id.identifierValue }
                        .contains(mappingData.lei)
                }
            entities.add(
                IsinLeiEntity(
                    isin = mappingData.isin,
                    company = company,
                    lei = mappingData.lei,
                ),
            )
        }
        return entities
    }

    /**
     * Method to save ISIN-LEI mappings in batches.
     * @param entities the ISIN-LEI mappings to save
     */
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

    /**
     * Method to save ISIN-LEI mappings in chunks using a callable for asynchronous execution.
     * @param data the ISIN-LEI mapping data to save
     * @param chunkSize the size of each chunk to process in parallel
     */
    private fun saveAllJdbcBatchCallable(
        data: List<IsinLeiMappingData>,
        chunkSize: Int = 10000,
    ) {
        val executorService = Executors.newFixedThreadPool(dataSourceMaximumPoolSize)
        val chunks = data.chunked(chunkSize)
        val tasks =
            chunks.map { chunk ->
                val companies = storedCompanyRepository.findCompaniesbyListOfLeis(data.map { it.lei }.toSet().toList())
                val entities = convertToIsinLeiEntity(data, companies)
                Callable<Void> {
                    saveAllJdbcBatch(entities)
                    null
                }
            }

        executorService.invokeAll(tasks)
    }

    /**
     * Method to remove all ISIN-LEI mappings.
     */
    protected fun clearAllMappings() {
        val sql = """TRUNCATE TABLE $tableName"""
        dataSource.connection.use { connection ->
            connection.createStatement().use { statement ->
                statement.executeUpdate(sql)
            }
        }
    }
}
