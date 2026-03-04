package org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.reports

import org.dataland.datalandqaservice.model.reports.AcceptedDataPointSource

/**
 * Request body for patching the accepted source of a data point in a dataset review.
 */
data class AcceptedSourcePatch(
    val acceptedSource: AcceptedDataPointSource? = null,
    val companyIdOfAcceptedQaReport: String? = null,
    val customValue: String? = null,
)
