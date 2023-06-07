package org.dataland.datalandbatchmanager.gleif

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.CompanyInformation

const val BASE_PATH_TO_DATALAND_BACKEND = "http://backend/api"

class Upload {
    fun uploadCompanies(companyInformation: List<CompanyInformation>) {
        val companyDataControllerApi = CompanyDataControllerApi(BASE_PATH_TO_DATALAND_BACKEND)
        companyInformation.forEach { companyDataControllerApi.postCompany(it) }
    }
}
