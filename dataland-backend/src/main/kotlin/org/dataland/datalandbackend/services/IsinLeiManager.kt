package org.dataland.datalandbackend.services

import org.dataland.datalandbackend.entities.IsinLeiEntity
import org.dataland.datalandbackend.repositories.IsinLeiRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Implementation of a ISIN manager for Dataland
 * @param isinLeiRepository JPA for ISIN data
 */
@Service
class IsinLeiManager(
    @Autowired private val isinLeiRepository: IsinLeiRepository,
) {
    /**
     * Method to put the information of a company.
     * @param isin the ISIN of the company
     * @param lei the LEI of the company
     * @return the updated ISIN-LEI mapping data
     */
    @Transactional
    fun putIsinLeiMapping(
        isin: String,
        lei: String,
    ): IsinLeiEntity {
        val isinLeiEntity =
            IsinLeiEntity(
                isin = isin,
                lei = lei,
            )
        return isinLeiRepository.save(isinLeiEntity)
    }

    /**
     * Method to remove all ISIN-LEI mappings.
     */
    @Transactional
    fun clearAllMappings() {
        isinLeiRepository.deleteAll()
    }
}
