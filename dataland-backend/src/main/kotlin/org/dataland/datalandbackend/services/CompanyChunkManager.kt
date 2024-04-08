package org.dataland.datalandbackend.services

import org.dataland.datalandbackend.entities.BasicCompanyInformation
import org.dataland.datalandbackend.repositories.StoredCompanyRepository
import org.dataland.datalandbackend.repositories.utils.StoredCompanySearchFilter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
/**
 * Service to chunk large data sets
 * @param companyRepository the data
 */
@Service("CompanyChunkManager")
class CompanyChunkManager(
    @Autowired private val companyRepository: StoredCompanyRepository,
) {

    /**
     * Method to split the return type of method searchCompaniesAndGetApiModel into a list of lists each not exceeeding
     * the given size
     * @param chunkSize the package size of the records
     * @param chunkIndex the index of the chunk which is requested
     * @param filter The filter to use during searching
     * @return list of lists each containing BasicCompanyInformation objects
     */
    @Transactional
    fun returnCompaniesInChunks(
        filter: StoredCompanySearchFilter,
        chunkIndex: Int,
        chunkSize: Int?,
    ): List<BasicCompanyInformation> {
        val offset = chunkIndex * (chunkSize ?: 0)
        val companies = if (areAllDropdownFiltersDeactivated(filter)) {
            companyRepository
                .getAllCompaniesWithDataset(
                    filter,
                    chunkSize, offset,
                )
        } else {
            companyRepository.searchCompanies(filter, chunkSize, offset)
        }
        return companies
    }

    /**
     * Method to check if ever filter is deactivated
     * @param filter The filter to use during searching
     */
    private fun areAllDropdownFiltersDeactivated(filter: StoredCompanySearchFilter): Boolean {
        return (
            filter.searchStringLength == 0
            )
    }

    /**
     * Method to get the number of companies satisfying the filter
     * @param filter The filter to use during searching
     */
    @Transactional
    fun returnNumberOfCompanies(
        filter: StoredCompanySearchFilter,
    ): Int {
        return companyRepository.getNumberOfCompanies(filter)
    }
}
