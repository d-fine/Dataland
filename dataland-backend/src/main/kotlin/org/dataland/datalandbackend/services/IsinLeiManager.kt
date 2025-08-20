package org.dataland.datalandbackend.services

import org.dataland.datalandbackend.entities.IsinLeiEntity
import org.dataland.datalandbackend.entities.StoredCompanyEntity
import org.dataland.datalandbackend.model.IsinLeiMappingData
import org.dataland.datalandbackend.model.enums.company.IdentifierType
import org.dataland.datalandbackend.repositories.IsinLeiRepository
import org.dataland.datalandbackend.repositories.StoredCompanyRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

/**
 * Implementation of a ISIN manager for Dataland
 */
@Service
class IsinLeiManager
    @Autowired
    constructor(
        private val storedCompanyRepository: StoredCompanyRepository,
        private val isinLeiTransactionalService: IsinLeiTransactionalService,
        private val isinLeiRepository: IsinLeiRepository,
    ) {
        private val logger = LoggerFactory.getLogger(javaClass)

        /**
         * Method to put an ISIN-LEI mapping into the database.
         * This method clears all previous mappings and adds new ones.
         * @param isinLeiMappingData the list of ISIN-LEI mapping data to be put
         */
        fun postIsinLeiMapping(isinLeiMappingData: List<IsinLeiMappingData>) {
            logger.info("Start dropping previous entries")
            isinLeiTransactionalService.clearAllMappings()
            logger.info("Dropped previous entries")
            logger.info("Preparing to add new ISIN-LEI mappings: ${isinLeiMappingData.size} entries")
            processIsinLeiMappingData(isinLeiMappingData)
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
                    companies?.firstOrNull {
                        it.identifiers
                            .filter { id -> id.identifierType == IdentifierType.Lei }
                            .map { id -> id.identifierValue }
                            .contains(mappingData.lei)
                    }
                if (company == null) {
                    logger.error("Attention: The LEI ${mappingData.lei} is unknown!!!!!!!!!")
                } else {
                    entities.add(
                        IsinLeiEntity(
                            isin = mappingData.isin,
                            company = company,
                            lei = mappingData.lei,
                        ),
                    )
                }
            }
            return entities
        }

        /**
         * Method to save ISIN-LEI mappings in chunks using a callable for asynchronous execution.
         * @param isinLeiMappingData the ISIN-LEI mapping data to save
         * @param chunkSize the size of each chunk to process in parallel
         */
        fun processIsinLeiMappingData(
            isinLeiMappingData: List<IsinLeiMappingData>,
            chunkSize: Int = 10000,
        ) {
            val chunks = isinLeiMappingData.chunked(chunkSize)
            val futures =
                chunks.map { chunk ->
                    val companies = storedCompanyRepository.findCompaniesbyListOfLeis(chunk.map { it.lei }.toSet().toList())
                    val entities = convertToIsinLeiEntity(chunk, companies)
                    isinLeiTransactionalService.saveAllJpaHibernate(entities)
                }
            futures.forEach { it.join() }
        }

        /**
         * Method to get all ISINs associated with a given LEI.
         * @param lei the LEI to search for
         * @return a list of ISINs associated with the given LEI (empty list if none are found)
         */
        fun getIsinsByLei(lei: String): List<String> = isinLeiRepository.findAllByLei(lei).map { it.isin }
    }
