package org.dataland.datalandbatchmanager.gleif

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.CompanyInformation
import org.slf4j.LoggerFactory
import java.net.SocketTimeoutException

const val BASE_PATH_TO_DATALAND_BACKEND = "http://backend/api"

class Upload {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun uploadCompanies(companyInformation: List<CompanyInformation>) {
        companyInformation.forEach { uploadSingleCompany(it) }
    }

    private fun uploadSingleCompany(companyInformation: CompanyInformation) {
        var counter = 0
        val maxTries = 3
        while (counter < maxTries) {
            try {
                logger.info("Uploading company data for ${companyInformation.companyName} (LEI: ${companyInformation.identifiers[0].identifierValue})")
                CompanyDataControllerApi("https://local-dev.dataland.com/api").postCompany(companyInformation)
                break
            } catch (ex: Exception) {
                logger.error("Unable to upload company data. Response was: ${ex.message}.")
                println(ex)
                when (ex) {
                    is SocketTimeoutException -> counter++
                    else -> break
                }
            }
        }
    }
}
