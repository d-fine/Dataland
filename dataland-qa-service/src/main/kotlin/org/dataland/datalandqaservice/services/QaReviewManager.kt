package org.dataland.datalandqaservice.org.dataland.datalandqaservice.services

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.ReviewQueueResponse
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.repositories.ReviewQueueRepository
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.utils.QaSearchFilter
import org.slf4j.LoggerFactory
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
     * @param dataType the datatype of the dataset
     * @param reportingPeriod the reportingperiod of the dataset
     * @param companyName the company name connected to the dataset
     * @param chunkIndex the chunkIndex of the request
     * @param chunkSize the chunkSize of the request
     */
    fun getInfoOnUnreviewedDatasets(
        dataType: Set<DataTypeEnum>?,
        reportingPeriod: Set<String>?,
        companyName: String?,
        chunkSize: Int,
        chunkIndex: Int,
    ): List<ReviewQueueResponse> {
        var companyIds = emptySet<String>()
        if (!companyName.isNullOrBlank()) {
            companyIds =
                companyDataControllerApi.getCompaniesBySearchString(companyName).map { it.companyId }.toSet()
        }
        val searchFilter = QaSearchFilter(
            dataType = dataType,
            reportingPeriod = reportingPeriod,
            companyId = companyIds,
            companyName = companyName,
        )
        // TODO change sql query from =companyName to IN ListOfCompanyIds
        val offset = (chunkIndex) * (chunkSize)
        // TODO loop over result list and retrieve name for every entry
        return reviewQueueRepository.getSortedPendingMetadataSet(
            searchFilter, resultOffset = offset,
            resultLimit = chunkSize,

        )
    }
}
