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
            return
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
                requesterEmail = userAuthentication.username,
                companyIdentifierType = companyIdentifierType,
                companyIdentifierValue = companyIdentifierValue,
                dataType = singleDataRequest.frameworkName,
                reportingPeriods = singleDataRequest.listOfReportingPeriods,
            ),
        )
    }
}
