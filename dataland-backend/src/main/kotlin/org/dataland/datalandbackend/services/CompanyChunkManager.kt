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
        if (filter.searchStringLength == 0) {
            return if (areAllDropdownFiltersDeactivated(filter)) {
                companyRepository
                    .getAllCompaniesWithDataset(
                        chunkSize, offset,
                    )
            } else {
                companyRepository.searchCompaniesWithoutSearchString(filter, chunkSize, offset)
            }
        } else {
            return companyRepository.searchCompanies(filter, chunkSize, offset)
        }
    }

    /**
     * Method to check if ever dropdownFilter is deactivated
     * @param filter The filter to use during searching
     */
    private fun areAllDropdownFiltersDeactivated(filter: StoredCompanySearchFilter): Boolean {
        return (
            filter.dataTypeFilterSize + filter.sectorFilterSize + filter.countryCodeFilterSize == 0
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
        // todo check because of number of Companies (idea: fix in frontend)
        return if (filter.searchStringLength == 0) {
            companyRepository
                .getNumberOfCompaniesWithoutSearchString(
                    filter,
                )
        } else {
            companyRepository.getNumberOfCompanies(filter)
        }
    }
}
