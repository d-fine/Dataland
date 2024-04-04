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
        var companies = emptyList<BasicCompanyInformation>()
        if (areAllDropdownFiltersDeactivated(filter)) {
            if (filter.searchStringLength == 0) {
                // show all companies with datasets -> very simple query getALlCompaniesWithDatasets in history
                companies = companyRepository
                    .getAllCompaniesWithDataWithoutFilterOrSearchString(
                        chunkSize, chunkIndex * (chunkSize),
                    )
            } else {
                // use landing page search
                // modify object to get all data information
            }
        } else {
            if (filter.dataTypeFilterSize > 0) {
                companies = companyRepository.searchCompaniesWithDataset(filter, chunkSize, chunkIndex * chunkSize)
            } else {
                companies = companyRepository.searchCompanies(filter, chunkSize, chunkIndex * chunkSize)
            }
        }
        return companies
    }

    /**
     * Method to check if ever Filter (excluding searchString) is deactivated
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
        if (filter.dataTypeFilterSize +
            filter.countryCodeFilterSize +
            filter.sectorFilterSize +
            filter.searchStringLength == 0
        ) {
            return 1000
        }
        return companyRepository.getNumberOfCompanies(filter)
    }
}
