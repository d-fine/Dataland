package org.dataland.datalandcommunitymanager.services

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.infrastructure.ApiClient
import org.dataland.datalandbackend.openApiClient.model.CompanyIdAndName
import org.dataland.datalandbackend.openApiClient.model.CompanyInformation
import org.dataland.datalandbackend.openApiClient.model.StoredCompany
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

/**
 * Class for getting companies that exist on Dataland
 */
@Service("CompanyGetter")
class CompanyGetter(
    @Value("\${dataland.backend.base-url}") private val backendBaseUrl: String,
) {
    private val companyDataControllerApi = CompanyDataControllerApi(backendBaseUrl)

    /** This method gets the companyId and name of all companies on Dataland that match the search string by their
     * name, one alternative name or one identifier
     * @param searchString the search string to use for this
     * @param bearerToken a bearer token required to perform the api call
     * @returns a list of companyIds and names
     */
    fun getCompanyIdsAndNamesForSearchString(searchString: String, bearerToken: String): List<CompanyIdAndName> {
        ApiClient.accessToken = bearerToken
        return companyDataControllerApi.getCompaniesBySearchString(searchString)
    }

    /** This method gets the company information behind the given company ID
     * @param companyId the company ID string to use for this
     * @param bearerToken a bearer token required to perform the api call
     * @returns the StoredCompany object for the give company ID
     */
    fun getCompanyById(companyId: String, bearerToken: String): StoredCompany {
        ApiClient.accessToken = bearerToken
        return companyDataControllerApi.getCompanyById(companyId)
    }

    /**
     * This method returns information about a specified company
     * @param companyId the ID of the company of interest
     * @returns information about the specified company
     */
    fun getCompanyInfo(companyId: String): CompanyInformation {
        return companyDataControllerApi.getCompanyInfo(companyId)
    }
}
