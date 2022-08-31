package org.dataland.e2etests.utils

import org.dataland.datalandbackend.openApiClient.model.CompanyInformation
import org.dataland.datalandbackend.openApiClient.model.StoredCompany

fun CompanyInformation.copyNormalised(): CompanyInformation {
    return this.copy(
        marketCap = this.marketCap.stripTrailingZeros()
    )
}

fun StoredCompany.copyNormalised(): StoredCompany {
    return this.copy(
        companyInformation = this.companyInformation.copyNormalised()
    )
}
