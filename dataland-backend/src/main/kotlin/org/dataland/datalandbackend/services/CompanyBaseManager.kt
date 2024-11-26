package org.dataland.datalandbackend.services

import org.dataland.datalandbackend.repositories.StoredCompanyBaseRepository
import org.dataland.datalandbackend.repositories.utils.StoredCompanySearchFilter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * Implementation of a company base manager for Dataland
 * @param companyBaseRepository JPA repository for company base data
 */
@Service
class CompanyBaseManager(
    @Autowired private val companyBaseRepository: StoredCompanyBaseRepository,
) {
    /**
     * Method to get the number of companies satisfying the filter
     * @param filter The filter to use during counting
     */
    @Transactional
    fun countNumberOfCompanies(filter: StoredCompanySearchFilter): Int =
        if (filter.searchStringLength == 0) {
            companyBaseRepository
                .getNumberOfCompaniesWithoutSearchString(
                    filter,
                )
        } else {
            companyBaseRepository.getNumberOfCompanies(filter)
        }

    /**
     * Returns a list of available country codes across all stored companies
     */
    fun getDistinctCountryCodes(): Set<String> = companyBaseRepository.fetchDistinctCountryCodes()

    /**
     * Returns a list of available sectors across all stored companies
     */
    fun getDistinctSectors(): Set<String> = companyBaseRepository.fetchDistinctSectors()
}
