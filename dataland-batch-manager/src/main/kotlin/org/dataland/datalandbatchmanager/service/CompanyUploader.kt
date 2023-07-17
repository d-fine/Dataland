package org.dataland.datalandbatchmanager.service

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.infrastructure.ClientException
import org.dataland.datalandbackend.openApiClient.infrastructure.ServerException
import org.dataland.datalandbackend.openApiClient.model.CompanyInformation
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.net.SocketTimeoutException

/**
 * Class for handling the upload of the company information retrieved from GLEIF to the Dataland backend
 */
@Service
class CompanyUploader(
    @Autowired private val companyDataControllerApi: CompanyDataControllerApi,
) {
    companion object {
        const val MAX_RETRIES = 3
        const val UNAUTHORIZED_CODE = 401
        const val FORBIDDEN_CODE = 403
    }

    private val logger = LoggerFactory.getLogger(javaClass)

    /**
     * Uploads a single Company to the dataland backend performing at most MAX_RETRIES retries.
     * This function absorbs errors and logs them.
     */
    fun uploadSingleCompany(
        companyInformation: CompanyInformation,
    ) {
        var counter = 0
        while (counter < MAX_RETRIES) {
            try {
                logger.info(
                    "Uploading company data for ${companyInformation.companyName} " +
                        "(LEI: ${companyInformation.identifiers["Lei"]!!.first()})",
                )
                companyDataControllerApi.postCompany(companyInformation)
                break
            } catch (exception: ClientException) {
                logger.error("Unable to upload company data. Response was: ${exception.message}.")
                counter = MAX_RETRIES
            } catch (exception: SocketTimeoutException) {
                logger.error("Unexpected timeout occurred. Response was: ${exception.message}.")
                counter++
            } catch (exception: ServerException) {
                logger.error("Unexpected server exception. Response was: ${exception.message}.")
                counter++
            }
        }
    }

    fun uploadOrPatchSingleCompany(
            companyInformation: CompanyInformation,
    ) {
        var counter = 0
        while (counter < MAX_RETRIES) {
            try {
                logger.info(
                        "Uploading company data for ${companyInformation.companyName} " +
                                "(LEI: ${companyInformation.identifiers["Lei"]!!.first()})",
                )
                companyDataControllerApi.postCompany(companyInformation)
                break
          //  } catch (exception: ClientException) {
          //      logger.error("Unable to upload company data. Response was: ${exception.message}.") --- do we need a message?
          //      companyDataControllerApi.patchCompanyByLei(companyInformation) --- function needs to be implemented
            } catch (exception: ClientException) {
                logger.error("Unable to upload company data. Response was: ${exception.message}.")
                counter = MAX_RETRIES
            } catch (exception: SocketTimeoutException) {
                logger.error("Unexpected timeout occurred. Response was: ${exception.message}.")
                counter++
            } catch (exception: ServerException) {
                logger.error("Unexpected server exception. Response was: ${exception.message}.")
                counter++
            }
        }
    }

    fun patchCompanyByLei(companyInformation: CompanyInformation): ResponseEntity<StoredCompany> {
        //val companyEntity = companyQueryManager.getCompanyByLei(companyInformation.identifiers[IdentifierType.lei.value].toSet())
        //or
        // val companyEntity = companyQueryManager.getCompanyByLei(companyInformation.identifiers["Lei"]!!.first())
        logger.info("Patching Company ${companyEntity.companyName} with Lei ${companyInformation.identifiers["Lei"]!!.first()}")
        companyInformation.companyName?.let { companyEntity.companyName = it }
        //companyInformation.companyLegalForm?.let { companyEntity.companyLegalForm = it } --not part of gleif company info
        companyInformation.headquarters?.let { companyEntity.headquarters = it }
        companyInformation.headquartersPostalCode?.let { companyEntity.headquartersPostalCode = it }
        //companyInformation.sector?.let { companyEntity.sector = it } --not part of gleif company info
        companyInformation.countryCode?.let { companyEntity.countryCode = it }
        //companyInformation.website?.let { companyEntity.website = it } --not part of gleif company info
        companyInformation.isTeaserCompany?.let { companyEntity.isTeaserCompany = it }

        //if (companyInformation.companyAlternativeNames != null) {
           // companyEntity.companyAlternativeNames = companyInformation.companyAlternativeNames
        //} --not part of gleif company info
        //this can be adapted to support just Lei - but is it even possible for Lei to change for gleif companies?
        if (companyInformation.identifiers != null) {
            for (keypair in companyInformation.identifiers) {
                replaceCompanyIdentifiers(
                        companyEntity = companyEntity,
                        identifierType = keypair.key,
                        newIdentifiers = keypair.value,
                )
            }
        }

        return companyRepository.save(companyEntity)
    }
}

// the error we get when posting a company with an already existing lei:
/*{
  "errors": [
    {
      "httpStatus": 400,
      "errorType": "invalid-input",
      "summary": "Company identifier already used",
      "message": "Could not insert company as one company identifier is already used to identify another company"
    }
  ]
}/*