package org.dataland.e2etests.utils

import org.dataland.datalandbackend.openApiClient.model.CompanyInformation
import org.dataland.datalandbackend.openApiClient.model.StoredCompany

/**
 * This function "normalises" the marketCap BigInt of a CompanyInformation object
 * so that it can be compared to other CopanyInformation objects by using .equals()
 */
fun CompanyInformation.copyNormalised(): CompanyInformation {
    return this.copy(
        marketCap = this.marketCap.stripTrailingZeros(),
        numberOfShares = this.numberOfShares?.stripTrailingZeros(),
        sharePrice = this.sharePrice?.stripTrailingZeros(),
        numberOfEmployees = this.numberOfEmployees?.stripTrailingZeros()
    )
}

/**
 * This function "normalises" the marketCap BigInt of a StoredCompany object
 * so that it can be compared to other StoredCompany objects by using .equals()
 */
fun StoredCompany.copyNormalised(): StoredCompany {
    return this.copy(
        companyInformation = this.companyInformation.copyNormalised()
    )
}
