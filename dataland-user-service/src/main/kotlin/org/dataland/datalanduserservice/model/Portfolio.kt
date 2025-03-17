package org.dataland.datalanduserservice.model

import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum

/**
 * Interface for the Portfolio API models
 */
interface Portfolio {
    val portfolioName: String
    val companyIds: Set<String>
    val frameworks: Set<DataTypeEnum>
}
