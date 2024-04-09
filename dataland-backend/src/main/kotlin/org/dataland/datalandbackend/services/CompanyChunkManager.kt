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
                // todo maybe do this in the frontend? --> set all filter as selected?
                companyRepository
                    .getAllCompaniesWithDataset(
                        chunkSize, offset,
                    )
            } else {
                companyRepository.searchCompaniesWithoutSearchString(filter, chunkSize, offset)
            }
        } else {
            //todo decide if we want to have seperat querys
            return if (filter.dataTypeFilterSize > 0) {
                companyRepository.searchCompanies(filter,chunkSize,offset)
            //companyRepository.searchCompaniesWithDatasets(filter, chunkSize, offset)
            } else {
                companyRepository.searchCompanies(filter,chunkSize,offset)
                //companyRepository.searchCompaniesWithoutDatasets(filter, chunkSize, offset)
            }

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
        return 1000
        // todo check because of number of Companies (idea: fix in frontend)
        if (filter.searchStringLength == 0) {
            return companyRepository
                .getNumberOfCompaniesWithoutSearchString(
                    filter,
                )
        } else {
            return companyRepository.getNumberOfCompanies(filter)
        }
    }
}
