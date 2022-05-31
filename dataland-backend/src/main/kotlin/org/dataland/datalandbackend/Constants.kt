package org.dataland.datalandbackend

import org.dataland.datalandbackend.interfaces.DataManagerInterface
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

const val RATIO_PRECISION = 4

/**
 * This class contains functionalities to provide specific "teaser" data to Dataland users without a required
 * authorization.
 */
@Component("TeaserConfiguration")
class TeaserConfiguration(
    @Autowired var dataManager: DataManagerInterface,
) {

    /**
     * This method checks if a company Id belongs to a company which should be accessible without authorization. Such
     * companies are called "teaser companies".
     * @param requestedCompanyId contains the company Id for which the check should occur
     * @return is a boolean which is TRUE if the company Id belongs to a teaser company, else it is FALSE
     */
    fun isCompanyPublic(requestedCompanyId: String): Boolean {
        val teaserCompanyName = System.getenv("TEASER_COMPANY_NAME") ?: "Adidas AG"
        val searchResult = dataManager.searchCompanies(teaserCompanyName, true)
        return searchResult.any { it.companyId == requestedCompanyId }
        }

    /**
     * This method checks if a data Id is assigned to a company Id which belongs to a teaser company.
     * @param requestedDataId contains the data Id for which a user has requested data
     * @return is a boolean which is TRUE if the data Id is assigned to the company Id of a teaser company,
     * else it is FALSE
     */
    fun isDataSetPublic(requestedDataId: String): Boolean {
        val associatedCompanyId = dataManager.getDataMetaInfo(requestedDataId).companyId
        return isCompanyPublic(associatedCompanyId)
    }
}
