package org.dataland.datalandbackend.services

import org.dataland.datalandbackend.entities.IsinLeiEntity
import org.dataland.datalandbackend.entities.StoredCompanyEntity
import org.dataland.datalandbackend.model.IsinLeiMappingData
import org.dataland.datalandbackend.model.enums.company.IdentifierType
import org.dataland.datalandbackend.repositories.StoredCompanyRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service

/**
 * Implementation of a ISIN manager for Dataland
 */
@Service
class IsinLeiManager(
    @Autowired private val storedCompanyRepository: StoredCompanyRepository,
    @Autowired private val isinLeiTransactionalService: IsinLeiTransactionalService,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Method to put an ISIN-LEI mapping into the database.
     * This method clears all previous mappings and adds new ones.
     * @param isinLeiMappingData the list of ISIN-LEI mapping data to be put
     */
    fun putIsinLeiMapping(isinLeiMappingData: List<IsinLeiMappingData>) {
        logger.info("Start dropping previous entries")
        isinLeiTransactionalService.clearAllMappings()
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
            try {
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
            } catch (e: NoSuchElementException) {
                logger.error("Company with LEI ${mappingData.lei} could not be found: ${e.message}")
            }
        }
        return entities
    }

    /**
     * Method to save ISIN-LEI mappings in chunks using a callable for asynchronous execution.
     * @param data the ISIN-LEI mapping data to save
     * @param chunkSize the size of each chunk to process in parallel
     */
    @Async
    fun saveAllJdbcBatchCallable(
        data: List<IsinLeiMappingData>,
        chunkSize: Int = 10000,
    ) {
        val chunks = data.chunked(chunkSize)
        chunks.map { chunk ->
            val companies = storedCompanyRepository.findCompaniesbyListOfLeis(chunk.map { it.lei }.toSet().toList())
            val entities = convertToIsinLeiEntity(chunk, companies)
            isinLeiTransactionalService.saveAllJdbcBatch(entities)
        }
    }

    /**
     * Asynchronous method to save ISIN-LEI mappings in batches.
     * @param data the ISIN-LEI mapping data to save
     * @param batchSize the size of each batch to process
     */
    @Async
    fun saveAllJdbcBatchAsync(
        data: List<IsinLeiEntity>,
        batchSize: Int = 50,
    ) {
        isinLeiTransactionalService.saveAllJdbcBatch(data, batchSize)
    }
}
