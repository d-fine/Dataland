package org.dataland.datalandbatchmanager.gleif

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.infrastructure.ApiClient
import org.dataland.datalandbackend.openApiClient.model.CompanyInformation

const val BASE_PATH_TO_DATALAND_BACKEND = "http://backend/api"

class Upload {
    fun uploadCompanies(companyInformation: List<CompanyInformation>) {
        val companyDataControllerApi = CompanyDataControllerApi("https://local-dev.dataland.com/api")
        companyInformation.forEach { companyDataControllerApi.postCompany(it) }
    }
}
