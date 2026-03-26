package org.dataland.datalandqaservice.org.dataland.datalandqaservice.utils

import org.dataland.dataSourcingService.openApiClient.model.DataSourcingPriorityByDataDimensions
import org.dataland.datalandbackendutils.model.BasicDataDimensions
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.QaReviewResponse

object QaReviewUtils {
    /**
     * Maps QA review responses to their corresponding data sourcing priorities.
     *
     * Builds a map from dimensions to priorities and applies it to each QA review response.
     * If no priorities are provided, all priorities in the responses are set to null.
     *
     * @param qaReviewResponses the QA review responses to update
     * @param priorities list of data sourcing priorities, or null if unavailable
     * @return updated list of QA review responses with assigned priorities
     */
    fun assignPriorities(
        qaReviewResponses: List<QaReviewResponse>,
        prioritiesOfAssociatedDataSourcing: List<DataSourcingPriorityByDataDimensions>?,
    ): List<QaReviewResponse> {
        val priorityMap =
            prioritiesOfAssociatedDataSourcing
                ?.associateBy(
                    { BasicDataDimensions(it.companyId, it.dataType, it.reportingPeriod) },
                    { it.priority },
                ).orEmpty()

        return qaReviewResponses.map { response ->
            val key = BasicDataDimensions(response.companyId, response.framework, response.reportingPeriod)
            response.copy(priorityOfAssociatedDataSourcing = priorityMap[key])
        }
    }
}
