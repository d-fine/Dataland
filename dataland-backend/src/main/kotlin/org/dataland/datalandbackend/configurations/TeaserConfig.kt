package org.dataland.datalandbackend.configurations

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.REALDATA
import org.dataland.datalandbackend.REAL_TEASER_COMPANY_PERM_ID
import org.dataland.datalandbackend.interfaces.DataManagerInterface
import org.dataland.datalandbackend.utils.CompanyInformationWithEuTaxonomyDataModel
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.io.File

/**
 * This class contains functionalities to provide specific "teaser" data to Dataland users without a required
 * authorization.
 */
@Component("TeaserConfig")
class TeaserConfig(
    @Autowired var dataManager: DataManagerInterface,
    @Autowired var objectMapper: ObjectMapper
) {

    /**
     * This method gets the n-th teaser company from the fixtures depending on the index that is set in the
     * environment variable TEASER_COMPANY_INDEX_IN_FIXTURES.
     * @return is the CompanyInformation object based on the n-th teaser company in the fixtures
     */
    fun getFakeTeaserCompanyName(): String {
        val jsonFile = File("./build/resources/CompanyInformationWithEuTaxonomyData.json")
        val testCompanyInformationWithEuTaxonomyData =
            objectMapper.readValue(
                jsonFile,
                object : TypeReference<List<CompanyInformationWithEuTaxonomyDataModel>>() {}
            )
        return testCompanyInformationWithEuTaxonomyData.first().companyInformation.companyName
    } // TODO maybe just import function from TestDataProvider in test

    /**
     * This method checks if a company Id belongs to a company which should be accessible without authorization. Such
     * companies are called "teaser companies".
     * @param requestedCompanyId contains the company Id for which the check should occur
     * @return is a boolean which is TRUE if the company Id belongs to a teaser company, else it is FALSE
     */
    fun isCompanyPublic(requestedCompanyId: String): Boolean {
        val teaserCompanyName = when (REALDATA) {
            true -> dataManager.searchCompanies(REAL_TEASER_COMPANY_PERM_ID, false)
                .first().companyInformation.companyName
            else -> getFakeTeaserCompanyName()
        }
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
