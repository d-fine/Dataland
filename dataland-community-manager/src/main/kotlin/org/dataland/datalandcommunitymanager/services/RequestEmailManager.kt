package org.dataland.datalandcommunitymanager.services

import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandcommunitymanager.entities.DataRequestEntity
import org.dataland.datalandcommunitymanager.model.dataRequest.AccessStatus
import org.dataland.datalandcommunitymanager.model.dataRequest.RequestStatus
import org.dataland.datalandcommunitymanager.services.messaging.AccessRequestEmailSender
import org.dataland.datalandcommunitymanager.services.messaging.DataRequestResponseEmailSender
import org.dataland.datalandcommunitymanager.services.messaging.SingleDataRequestEmailMessageSender
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.dataland.keycloakAdapter.auth.DatalandJwtAuthentication
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.UUID

/**
 * Manages all alterations of data requests
 */
@Service
class RequestEmailManager(
    @Autowired private val dataRequestResponseEmailMessageSender: DataRequestResponseEmailSender,
    @Autowired private val singleDataRequestEmailMessageSender: SingleDataRequestEmailMessageSender,
    @Autowired private val accessRequestEmailSender: AccessRequestEmailSender,
) {
    /**
     * Method to send email when the request status or access status changes
     * @param dataRequestEntity the request entity
     * @param requestStatus the new request status of the request
     * @param accessStatus the new access status of the request
     * @param correlationId the correlationId of the operation
     */
    fun sendEmailsWhenStatusChanged(
        dataRequestEntity: DataRequestEntity,
        requestStatus: RequestStatus?,
        accessStatus: AccessStatus?,
        correlationId: String?,
    ) {
        val correlationId = correlationId ?: UUID.randomUUID().toString()
        if (requestStatus == RequestStatus.Answered) {
            dataRequestResponseEmailMessageSender.sendDataRequestAnsweredEmail(dataRequestEntity, correlationId)
        }
        if (requestStatus == RequestStatus.Closed) {
            dataRequestResponseEmailMessageSender.sendDataRequestClosedEmail(dataRequestEntity, correlationId)
        }
        if (requestStatus == RequestStatus.NonSourceable) {
            dataRequestResponseEmailMessageSender.sendDataRequestNonSourceableEmail(dataRequestEntity, correlationId)
        }
        if (accessStatus == AccessStatus.Granted) {
            accessRequestEmailSender.notifyRequesterAboutGrantedRequest(
                AccessRequestEmailSender.GrantedEmailInformation(dataRequestEntity),
                correlationId,
            )
        }
        if (requestStatus == RequestStatus.Answered && accessStatus == AccessStatus.Pending) {
            accessRequestEmailSender.notifyCompanyOwnerAboutNewRequest(
                AccessRequestEmailSender.RequestEmailInformation(dataRequestEntity),
                correlationId,
            )
        }
    }

    /**
     * Method to send email if the message history is updated
     * @param dataRequestEntity the request entity
     * @param contacts set of email addresses
     * @param message string content of the email
     */
    fun sendSingleDataRequestEmail(
        dataRequestEntity: DataRequestEntity,
        contacts: Set<String>,
        message: String?,
    ) {
        val correlationId = UUID.randomUUID().toString()
        singleDataRequestEmailMessageSender.sendSingleDataRequestExternalMessage(
            messageInformation =
                SingleDataRequestEmailMessageSender.MessageInformation(
                    dataType = DataTypeEnum.decode(dataRequestEntity.dataType)!!,
                    reportingPeriods = setOf(dataRequestEntity.reportingPeriod),
                    datalandCompanyId = dataRequestEntity.datalandCompanyId,
                    userAuthentication = DatalandAuthentication.fromContext() as DatalandJwtAuthentication,
                ),
            receiverSet = contacts,
            contactMessage = message,
            correlationId = correlationId,
        )
    }

    /**
     * Function to send an e-mail notification to a user with a closed data request that there
     * has been a QA approval for a dataset with regard to the same company, reporting period and
     * framework.
     */
    fun sendEmailToUserWithAnsweredOrClosedOrResolvedRequest(
        dataRequestEntity: DataRequestEntity,
        correlationId: String,
    ) {
        dataRequestResponseEmailMessageSender.sendEmailToUserWithAnsweredOrClosedOrResolvedRequest(
            dataRequestEntity,
            correlationId,
        )
    }
}
