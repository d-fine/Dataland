package org.dataland.datalandbackend

import org.dataland.datalandbackend.interfaces.DataManagerInterface
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

const val RATIO_PRECISION = 4

@Component("PreviewStuff")
class PreviewStuff(
    @Autowired var dataManager: DataManagerInterface,
) {
    fun isCompanyPublic(requestedCompanyId: String): Boolean {
        val teaserCompanyName = System.getenv("TEASER_COMPANY_NAME") ?: "Adidas AG"
        val searchResult = dataManager.searchCompanies(teaserCompanyName, true)
        return if (searchResult.isEmpty()) {
            false
        } else if (searchResult.size > 1) {
            for (storedCompany in searchResult) {
                if (requestedCompanyId == storedCompany.companyId) {
                    return true
                }
            }
            return false
        } else return requestedCompanyId == searchResult.first().companyId
    }
}
