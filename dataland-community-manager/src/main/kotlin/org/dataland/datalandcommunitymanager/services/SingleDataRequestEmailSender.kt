package org.dataland.datalandcommunitymanager.services

import org.dataland.datalandbackend.model.enums.p2p.DataRequestCompanyIdentifierType
import org.dataland.datalandcommunitymanager.model.dataRequest.SingleDataRequest
import org.dataland.datalandemail.email.EmailSender
import org.dataland.keycloakAdapter.auth.DatalandJwtAuthentication
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

/**
 * Implementation of a request manager service for all operations concerning the processing of single data requests
 */
@Service
class SingleDataRequestEmailSender(
    @Autowired private val emailSender: EmailSender,
    @Autowired private val singleDataRequestEmailBuilder: SingleDataRequestEmailBuilder,
    @Autowired private val singleDataRequestInternalEmailBuilder: SingleDataRequestInternalEmailBuilder,
) {
    /**
     * Sends emails to the proper recepients,
     * i.e. to the provided contacts and the data owners of the specified companies
     * or the dataland staff if the company is not known to Dataland
     * or no contact is specified and no data owner is known
     * @param userAuthentication the authentication of the user who called this method
     * @param singleDataRequest the fundamental data request
     * @param companyIdentifierType the type of the identifer provided by the user or the Dataland company ID
     * @param companyIdentifierValue an identifier for the company of type [companyIdentifierType]
     */
    fun sendSingleDataRequestEmails(
        userAuthentication: DatalandJwtAuthentication,
        singleDataRequest: SingleDataRequest,
        companyIdentifierType: DataRequestCompanyIdentifierType,
        companyIdentifierValue: String,
    ) {
        if (singleDataRequest.listOfReportingPeriods.isEmpty()) return
        if (companyIdentifierType != DataRequestCompanyIdentifierType.DatalandCompanyId) {
            sendInternalEmail(
                userAuthentication = userAuthentication,
                singleDataRequest = singleDataRequest,
                companyIdentifierType = companyIdentifierType,
                companyIdentifierValue = companyIdentifierValue,
            )
            return
        }
        singleDataRequest.contactList?.forEach { contactEmail ->
            emailSender.sendEmail(
                singleDataRequestEmailBuilder.buildSingleDataRequestEmail(
                    requesterEmail = userAuthentication.username,
                    receiverEmail = contactEmail,
                    companyId = companyIdentifierValue,
                    dataType = singleDataRequest.frameworkName,
                    reportingPeriods = singleDataRequest.listOfReportingPeriods,
                    message = singleDataRequest.message,
                ),
            )
        }
        if ((singleDataRequest.contactList?.count() ?: 0) == 0) {
            sendInternalEmail(
                userAuthentication = userAuthentication,
                singleDataRequest = singleDataRequest,
                companyIdentifierType = companyIdentifierType,
                companyIdentifierValue = companyIdentifierValue,
            )
        }
    }

    private fun sendInternalEmail(
        userAuthentication: DatalandJwtAuthentication,
        singleDataRequest: SingleDataRequest,
        companyIdentifierType: DataRequestCompanyIdentifierType,
        companyIdentifierValue: String,
    ) {
        emailSender.sendEmail(
            singleDataRequestInternalEmailBuilder.buildSingleDataRequestInternalEmail(
                userAuthentication = userAuthentication,
                companyIdentifierType = companyIdentifierType,
                companyIdentifierValue = companyIdentifierValue,
                dataType = singleDataRequest.frameworkName,
                reportingPeriods = singleDataRequest.listOfReportingPeriods,
            ),
        )
    }
}
