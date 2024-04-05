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
        chunkSize: Int,
        chunkIndex: Int,
        filter: StoredCompanySearchFilter,
    ): List<BasicCompanyInformation> {
        val companies: List<BasicCompanyInformation>
        if (areAllDropdownFiltersDeactivated(filter)) {
            companies = if (filter.searchStringLength == 0) {
                companyRepository
                    .getAllCompaniesWithDataset(
                        chunkSize, chunkIndex * (chunkSize),
                    )
            } else {
                companyRepository.searchCompaniesByNameOrIdentifierAsBasicCompanyInformation(
                    filter.searchString, chunkSize, chunkSize * chunkIndex,
                )
            }
        } else {
            companies = if (filter.dataTypeFilterSize > 0) {
                companyRepository.searchCompaniesWithDataset(filter, chunkSize, chunkIndex * chunkSize)
            } else {
                companyRepository.searchCompanies(filter, chunkSize, chunkIndex * chunkSize)
            }
        }
        return companies
    }

    /**
     * Method to check if ever dropdownFilter is deactivated
     * @param filter The filter to use during searching
     */
    private fun areAllDropdownFiltersDeactivated(filter: StoredCompanySearchFilter): Boolean {
        return (
            filter.dataTypeFilterSize +
                filter.countryCodeFilterSize +
                filter.sectorFilterSize == 0
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
        // todo
        return 1000
        return companyRepository.getNumberOfCompanies(filter)
    }
}
