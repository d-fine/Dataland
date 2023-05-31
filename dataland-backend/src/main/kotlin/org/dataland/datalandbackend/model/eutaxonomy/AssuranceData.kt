package org.dataland.datalandbackend.model.eutaxonomy

import org.dataland.datalandbackend.model.CompanyReportReference
import org.dataland.datalandbackend.model.enums.eutaxonomy.AssuranceOptions

/**
 * --- API model ---
 * Level of assurance for the reported data and information about the assurance provider
 */
data class AssuranceData(
    val assurance: AssuranceOptions? = null,

    val provider: String? = null,

    val dataSource: CompanyReportReference? = null,
)
