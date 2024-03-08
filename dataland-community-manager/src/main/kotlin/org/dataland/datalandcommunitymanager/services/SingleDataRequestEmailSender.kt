package org.dataland.datalandcommunitymanager.services

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
     * Sends emails to the proper recipients,
     * i.e. to the provided contacts and the data owners of the specified companies
     * or the dataland staff if the company is not known to Dataland
     * or no contact is specified and no data owner is known
     * @param userAuthentication the authentication of the user who called this method
     * @param singleDataRequest the fundamental data request
     * @param datalandCompanyId is the companyId of the company in Dataland
     */
    fun sendSingleDataRequestEmails(
        userAuthentication: DatalandJwtAuthentication,
        singleDataRequest: SingleDataRequest,
        datalandCompanyId: String,
    ) {
        if (
            singleDataRequest.contacts.isNullOrEmpty()
        ) {
            sendInternalEmail(
                userAuthentication = userAuthentication,
                singleDataRequest = singleDataRequest,
                datalandCompanyId = datalandCompanyId,
            )
            return
        }
        sendEmailToSpecifiedContacts(userAuthentication, singleDataRequest, datalandCompanyId)
    }

    private fun sendEmailToSpecifiedContacts(
        userAuthentication: DatalandJwtAuthentication,
        singleDataRequest: SingleDataRequest,
        datalandCompanyId: String,
    ) {
        singleDataRequest.contacts?.forEach { contactEmail ->
            emailSender.sendEmail(
                singleDataRequestEmailBuilder.buildSingleDataRequestEmail(
                    requesterEmail = userAuthentication.username,
                    receiverEmail = contactEmail,
                    companyId = datalandCompanyId,
                    dataType = singleDataRequest.dataType,
                    reportingPeriods = singleDataRequest.reportingPeriods,
                    rawMessage = singleDataRequest.message,
                ),
            )
        }
    }

    private fun sendInternalEmail(
        userAuthentication: DatalandJwtAuthentication,
        datalandCompanyId: String,
        singleDataRequest: SingleDataRequest,
    ) {
        emailSender.sendEmail(
            singleDataRequestInternalEmailBuilder.buildSingleDataRequestInternalEmail(
                userAuthentication = userAuthentication,
                datalandCompanyId,
                dataType = singleDataRequest.dataType,
                reportingPeriods = singleDataRequest.reportingPeriods,
            ),
        )
    }
}
