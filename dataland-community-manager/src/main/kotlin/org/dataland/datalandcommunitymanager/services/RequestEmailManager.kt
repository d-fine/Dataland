package org.dataland.datalandcommunitymanager.services

import org.dataland.datalandbackend.openApiClient.model.DataTypeEnum
import org.dataland.datalandcommunitymanager.entities.DataRequestEntity
import org.dataland.datalandcommunitymanager.model.dataRequest.AccessStatus
import org.dataland.datalandcommunitymanager.model.dataRequest.DataRequestPatch
import org.dataland.datalandcommunitymanager.model.dataRequest.RequestStatus
import org.dataland.datalandcommunitymanager.services.messaging.AccessRequestEmailBuilder
import org.dataland.datalandcommunitymanager.services.messaging.DataRequestResponseEmailBuilder
import org.dataland.datalandcommunitymanager.services.messaging.SingleDataRequestEmailMessageBuilder
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
    @Autowired private val dataRequestResponseEmailMessageSender: DataRequestResponseEmailBuilder,
    @Autowired private val singleDataRequestEmailMessageBuilder: SingleDataRequestEmailMessageBuilder,
    @Autowired private val accessRequestEmailBuilder: AccessRequestEmailBuilder,
) {
    /**
     * Method to send email when the request status changes
     * @param dataRequestEntity the request entity
     * @param newRequestStatus the new request status of the request
     * @param correlationId the correlationId of the operation
     */
    fun sendEmailsWhenRequestStatusChanged(
        dataRequestEntity: DataRequestEntity,
        newRequestStatus: RequestStatus?,
        requestStatusChangeReason: String?,
        earlierQaApprovedVersionOfDatasetExists: Boolean,
        correlationId: String = UUID.randomUUID().toString(),
    ) {
        if (newRequestStatus == RequestStatus.Answered) {
            if (earlierQaApprovedVersionOfDatasetExists) {
                dataRequestResponseEmailMessageSender.buildDataUpdatedEmailAndSendCEMessage(dataRequestEntity, correlationId)
            } else {
                dataRequestResponseEmailMessageSender.buildDataRequestAnsweredEmailAndSendCEMessage(dataRequestEntity, correlationId)
            }
        }
        if (newRequestStatus == RequestStatus.NonSourceable) {
            dataRequestResponseEmailMessageSender.buildDataRequestNonSourceableEmailAndSendCEMessage(
                dataRequestEntity,
                requestStatusChangeReason,
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
        singleDataRequestEmailMessageBuilder.buildSingleDataRequestExternalMessageAndSendCEMessage(
            messageInformation =
                SingleDataRequestEmailMessageBuilder.MessageInformation(
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
     * Function to send an e-mail notification to a user with an answered, closed or resolved data request that there
     * has been a QA approval for a dataset with regard to the same company, reporting period and framework.
     */
    fun sendDataUpdatedEmail(
        dataRequestEntity: DataRequestEntity,
        correlationId: String,
    ) {
        dataRequestResponseEmailMessageSender.buildDataUpdatedEmailAndSendCEMessage(
            dataRequestEntity,
            correlationId,
        )
    }

    /**
     * Function to send relevant e-mail notifications on a patch event for an access request.
     */
    fun sendNotificationsSpecificToAccessRequests(
        dataRequestEntity: DataRequestEntity,
        dataRequestPatch: DataRequestPatch,
        correlationId: String,
    ) {
        if (dataRequestPatch.accessStatus == AccessStatus.Granted) {
            accessRequestEmailBuilder.notifyRequesterAboutGrantedRequest(
                AccessRequestEmailBuilder.GrantedEmailInformation(dataRequestEntity),
                correlationId,
            )
        }
        if (dataRequestPatch.requestStatus == RequestStatus.Answered &&
            dataRequestPatch.accessStatus == AccessStatus.Pending
        ) {
            accessRequestEmailBuilder.notifyCompanyOwnerAboutNewRequest(
                AccessRequestEmailBuilder.RequestEmailInformation(dataRequestEntity),
                correlationId,
            )
        }
    }
}
