package org.dataland.datalandqaservice.org.dataland.datalandqaservice.services

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.ReviewQueueResponse
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.repositories.ReviewQueueRepository
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.utils.QaSearchFilter
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

/**
 * A service class for managing QA report meta-information
 */
@Service
class QaReviewManager(
    @Autowired val reviewQueueRepository: ReviewQueueRepository,
    @Autowired val companyDataControllerApi: CompanyDataControllerApi,
) {
    /**
     * The method returns a list of unreviewed datasets with corresponding information for the specified input params
     * @param dataTypes the datatype of the dataset
     * @param reportingPeriods the reportingperiod of the dataset
     * @param companyName the company name connected to the dataset
     * @param chunkIndex the chunkIndex of the request
     * @param chunkSize the chunkSize of the request
     */
    fun getInfoOnUnreviewedDatasets(
        dataTypes: Set<DataTypeEnum>?,
        reportingPeriods: Set<String>?,
        companyName: String?,
        chunkSize: Int,
        chunkIndex: Int,
    ): List<ReviewQueueResponse> {
        var companyIds = emptySet<String>()
        if (!companyName.isNullOrBlank()) {
            companyIds =
                companyDataControllerApi.getCompaniesBySearchString(companyName).map { it.companyId }.toSet()
        }
        val searchFilter =
            QaSearchFilter(
                dataTypes = dataTypes,
                reportingPeriods = reportingPeriods,
                companyIds = companyIds,
                companyName = companyName,
            )
        val offset = (chunkIndex) * (chunkSize)
        return reviewQueueRepository.getSortedPendingMetadataSet(
            searchFilter, resultOffset = offset,
            resultLimit = chunkSize,
        )
    }

    /**
     * This method returns the number of unreviewed datasets for a specific set of filters
     * @param dataType the set of datatypes for which should be filtered
     * @param reportingPeriod the set of reportingPeriods for which should be filtered
     * @param companyName the companyName for which should be filtered
     */
    fun getNumberOfUnreviewedDatasets(
        dataTypes: Set<DataTypeEnum>?,
        reportingPeriods: Set<String>?,
        companyName: String?,
    ): Int {
        var companyIds = emptySet<String>()
        if (!companyName.isNullOrBlank()) {
            companyIds =
                companyDataControllerApi.getCompaniesBySearchString(companyName).map { it.companyId }.toSet()
        }
        val filter =
            QaSearchFilter(
                dataTypes = dataTypes, companyName = companyName, reportingPeriods = reportingPeriods,
                companyIds = companyIds,
            )
        return reviewQueueRepository.getNumberOfRequests(filter)
    }
}
