package org.dataland.datalandcommunitymanager.services

import org.dataland.datalandbackend.model.enums.p2p.DataRequestCompanyIdentifierType
import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandemail.email.Email
import org.dataland.datalandemail.email.PropertyStyleEmailBuilder
import org.dataland.keycloakAdapter.auth.DatalandJwtAuthentication
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

/**
 * A class that manages generating emails regarding bulk data requests
 */
@Component
class SingleDataRequestInternalEmailBuilder(
    @Value("\${dataland.proxy.primary.url}") private val proxyPrimaryUrl: String,
    @Value("\${dataland.notification.sender.address}") senderEmail: String,
    @Value("\${dataland.notification.sender.name}") senderName: String,
    @Value("\${dataland.notification.data-request.internal.receivers}") semicolonSeparatedReceiverEmails: String,
    @Value("\${dataland.notification.data-request.internal.cc}") semicolonSeparatedCcEmails: String,
    @Autowired val companyApi: CompanyDataControllerApi,
) : PropertyStyleEmailBuilder(
    senderEmail = senderEmail,
    senderName = senderName,
    semicolonSeparatedReceiverEmails = semicolonSeparatedReceiverEmails,
    semicolonSeparatedCcEmails = semicolonSeparatedCcEmails,
) {
    /**
     * Function that generates the email to be sent
     */
    fun buildSingleDataRequestInternalEmail(
        userAuthentication: DatalandJwtAuthentication,
        companyIdentifierType: DataRequestCompanyIdentifierType,
        companyIdentifierValue: String,
        dataType: DataTypeEnum,
        reportingPeriods: Set<String>,
    ): Email {
        val properties = mutableMapOf(
            "Environment" to proxyPrimaryUrl,
            "User" to buildUserInfo(userAuthentication),
            "Data Type" to dataType.name,
            "Reporting Periods" to reportingPeriods.joinToString(", "),
            "Company Identifier (${companyIdentifierType.name})" to companyIdentifierValue,
        )
        if (companyIdentifierType == DataRequestCompanyIdentifierType.DatalandCompanyId) {
            properties["Company Name"] = companyApi.getCompanyInfo(companyIdentifierValue).companyName
        }
        return buildPropertyStyleEmail(
            subject = "Dataland Single Data Request",
            textTitle = "A single data request has been submitted",
            htmlTitle = "Single Data Request",
            properties = properties,
        )
    }
}
