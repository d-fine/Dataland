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
        chunkSize: Int?,
        chunkIndex: Int,
        filter: StoredCompanySearchFilter,
    ): List<BasicCompanyInformation> {
        if (filter.dataTypeFilterSize +
            filter.countryCodeFilterSize +
            filter.sectorFilterSize +
            filter.searchStringLength == 0
        ) {
            return companyRepository.getAllCompaniesWithDataset(chunkSize ?: 1, chunkIndex * (chunkSize ?: 1))
        }
        return companyRepository.searchCompanies(filter, chunkSize ?: 1, chunkIndex * (chunkSize ?: 1))
    }

    /**
     * Method to get the number of companies satisfying the filter
     * @param filter The filter to use during searching
     */
    @Transactional
    fun returnNumberOfCompanies(
        filter: StoredCompanySearchFilter,
    ): Int {
        if(filter.dataTypeFilterSize +
            filter.countryCodeFilterSize +
            filter.sectorFilterSize +
            filter.searchStringLength == 0){
            return 1000
        }
        return companyRepository.getNumberOfCompanies(filter)
    }
}
