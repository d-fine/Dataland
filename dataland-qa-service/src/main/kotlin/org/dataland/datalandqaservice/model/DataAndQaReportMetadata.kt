package org.dataland.datalandqaservice.org.dataland.datalandqaservice.model

import org.dataland.datalandbackend.openApiClient.model.DataMetaInformation
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.reports.QaReportMetaInformation

/**
 * --- API model ---
 * Combined Metadata of dataset and corresponding QA report
 * @param dataMetadata Metadata of dataset
 * @param qaReportMetadata Metadata of QA report for dataset
 */
data class DataAndQaReportMetadata(
    val dataMetadata: DataMetaInformation,
    val qaReportMetadata: QaReportMetaInformation,
)
