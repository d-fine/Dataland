package org.dataland.datalandbackend.model

import org.dataland.datalandbackend.model.enums.data.QualityOptions

/**
 * --- API model ---
 * Fields of a generic basic data point with the minimal necessary information
 */
interface DataPointBaseInformationInterface: CompanyReportReferenceInterface {
    val quality: QualityOptions

    val comment: String?
}


