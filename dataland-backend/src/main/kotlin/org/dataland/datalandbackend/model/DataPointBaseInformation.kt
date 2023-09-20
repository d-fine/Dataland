package org.dataland.datalandbackend.model

import org.dataland.datalandbackend.model.enums.data.QualityOptions

/**
 * --- API model ---
 * Fields of a generic basic data point with the minimal necessary information
 */
interface DataPointBaseInformationInterface {
    val quality: QualityOptions

    val dataSource: CompanyReportReference?

    val comment: String?
}

/**
 * --- API model ---
 * Fields of a generic basic data point with the minimal necessary information
 */
open class DataPointBaseInformation(
    override val quality: QualityOptions,

    override val dataSource: CompanyReportReference? = null,

    override val comment: String? = null,
): DataPointBaseInformationInterface
