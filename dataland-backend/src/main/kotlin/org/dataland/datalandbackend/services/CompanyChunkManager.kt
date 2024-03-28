package org.dataland.datalandbackend.services

import org.dataland.datalandbackend.entities.BasicCompanyInformation
import org.dataland.datalandbackend.repositories.utils.StoredCompanySearchFilter
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.slf4j.LoggerFactory
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
) {
    private val logger = LoggerFactory.getLogger(javaClass)

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
        val getCompanies = companyQueryManager.searchCompaniesAndGetApiModel(filter)
        val chunkedCompanies = getCompanies.chunked(chunkSize)
        var requestedChunk = emptyList<BasicCompanyInformation>()
        if (chunkIndex >= 0 && chunkIndex < chunkedCompanies.size) {
            requestedChunk = chunkedCompanies[chunkIndex]
            println("Chunk $chunkIndex: $requestedChunk")
        } else {
            throw ResourceNotFoundApiException(
                "Invalid index",
                "The specified index of the chunk is invalid.",
            )
        }
        return requestedChunk
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
