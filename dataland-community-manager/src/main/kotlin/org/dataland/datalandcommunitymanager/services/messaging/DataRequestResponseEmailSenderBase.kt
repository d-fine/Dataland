package org.dataland.datalandcommunitymanager.services.messaging

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandcommunitymanager.entities.DataRequestEntity
import org.dataland.datalandcommunitymanager.services.KeycloakUserControllerApiService
import java.text.SimpleDateFormat
import java.util.*

/**
 * A class that provided utility for generating emails messages for data request responses
 */
open class DataRequestResponseEmailSenderBase(
    private val keycloakUserControllerApiService: KeycloakUserControllerApiService,
    private val companyDataControllerApi: CompanyDataControllerApi,
) {

    /**
     * Method to retrieve companyName by companyId
     * @param companyId dataland companyId
     * @returns companyName as string
     */
    private fun getCompanyNameById(companyId: String): String {
        return companyDataControllerApi.getCompanyInfo(companyId).companyName.ifEmpty { companyId }
    }

    /**
     * Method to convert unit time in ms to human-readable date
     * @param creationTimestamp unix time in ms
     * @returns human-readable date as string
     */
    private fun convertUnitTimeInMsToDate(creationTimestamp: Long): String {
        val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm")
        dateFormat.timeZone = TimeZone.getTimeZone("Europe/Berlin")
        return dateFormat.format(creationTimestamp)
    }

    /**
     * Method to retrieve userEmail by userId
     * @param userId dataland userId
     * @returns userEmail as string
     */
    protected fun getUserEmailById(userId: String): String {
        return keycloakUserControllerApiService.getEmailAddress(userId)
    }

    /**
     * Method to retrieve human-readable dataType
     * @param dataType dataland dataType
     * @returns human-readable dataType as string
     */
    private fun getDataTypeDescription(dataType: String): String {
        return when (dataType) {
            "eutaxonomy-financials" -> "EU Taxonomy for financial companies"
            "eutaxonomy-non-financials" -> "EU Taxonomy for non-financial companies"
            "lksg" -> "LkSG"
            "sfdr" -> "SFDR"
            "sme" -> "SME"
            "p2p" -> "WWF Pathways to Paris"
            "esg-questionnaire" -> "ESG Questionnaire"
            "heimathafen" -> "Heimathafen"
            else -> dataType
        }
    }

    protected fun getProperties(dataRequestEntity: DataRequestEntity, staleDaysThreshold: String): Map<String, String> {
        return mapOf(
            "companyId" to dataRequestEntity.datalandCompanyId,
            "companyName" to getCompanyNameById(dataRequestEntity.datalandCompanyId),
            "dataType" to dataRequestEntity.dataType,
            "reportingPeriod" to dataRequestEntity.reportingPeriod,
            "creationDate" to convertUnitTimeInMsToDate(dataRequestEntity.creationTimestamp),
            "dataTypeDescription" to getDataTypeDescription(dataRequestEntity.dataType),
            "dataRequestId" to dataRequestEntity.dataRequestId,
            "closedInDays" to staleDaysThreshold,
        )
    }
}
