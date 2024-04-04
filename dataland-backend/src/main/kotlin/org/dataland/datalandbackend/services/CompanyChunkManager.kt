package org.dataland.datalandbackend.services

import org.dataland.datalandbackend.entities.BasicCompanyInformation
import org.dataland.datalandbackend.repositories.StoredCompanyRepository
import org.dataland.datalandbackend.repositories.utils.StoredCompanySearchFilter
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
/**
 * Service to chunk large data sets
 * @param companyQueryManager provides the data
 */
@Service("CompanyChunkManager")
class CompanyChunkManager(
    @Autowired private val companyQueryManager: CompanyQueryManager,
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
        return companyRepository.searchCompanies(filter, chunkSize ?: 1, chunkIndex * (chunkSize ?: 1))

        val companies = companyQueryManager.searchCompaniesAndGetApiModel(filter) // todo
        val companiesPerChunk = if (chunkSize != null && chunkSize > 0) chunkSize else companies.size // todo

        val chunkedCompanies = companies.chunked(companiesPerChunk)
        if (chunkIndex >= 0 && chunkIndex < chunkedCompanies.size) {
            val requestedChunk = chunkedCompanies[chunkIndex]
            return requestedChunk
        } else {
            throw ResourceNotFoundApiException(
                "Invalid index",
                "The specified index of the chunk is invalid.",
            )
        }
    }

    /**
     * Method to get the number of companies satisfying the filter
     * @param filter The filter to use during searching
     */
    @Transactional
    fun returnNumberOfCompanies(
        filter: StoredCompanySearchFilter,
    ): Int {
        val getCompanies = companyQueryManager.searchCompaniesAndGetApiModel(filter)
        return getCompanies.size
    }
}
