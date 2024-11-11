package org.dataland.datalandemailservice.email

import org.dataland.datalandemailservice.services.EmailSubscriptionTracker
import org.dataland.datalandmessagequeueutils.messages.email.AccessToDatasetGranted
import org.dataland.datalandmessagequeueutils.messages.email.AccessToDatasetRequested
import org.dataland.datalandmessagequeueutils.messages.email.CompanyOwnershipClaimApproved
import org.dataland.datalandmessagequeueutils.messages.email.DataRequestAnswered
import org.dataland.datalandmessagequeueutils.messages.email.DataRequestClosed
import org.dataland.datalandmessagequeueutils.messages.email.DatasetRequestedClaimOwnership
import org.dataland.datalandmessagequeueutils.messages.email.InitializeBaseUrlLater
import org.dataland.datalandmessagequeueutils.messages.email.InitializeSubscriptionUuidLater
import org.dataland.datalandmessagequeueutils.messages.email.KeyValueTable
import org.dataland.datalandmessagequeueutils.messages.email.MultipleDatasetsUploadedEngagement
import org.dataland.datalandmessagequeueutils.messages.email.SingleDatasetUploadedEngagement
import org.dataland.datalandmessagequeueutils.messages.email.TypedEmailContent
import org.dataland.datalandmessagequeueutils.messages.email.Value
import java.util.UUID

/**
 * This function initializes the remaining variables required to create the content of the emails.
 * This includes late init variables that are declared through an interface and also variables that
 * only defined by a class.
 * @param receivers The receiver of the email. This variable is used to set the subscriptionUuid of the email content.
 *  Note that we can only set this variable if there is a single receiver. Otherwise, the subscriptionUuid is not unique.
 * @param proxyPrimaryUrl Used to initialize the baseUrl that is used for links to Dataland in the templates.
 * @param emailSubscriptionTracker The emailSubscriptionTracker is used to obtain the subscription status of emailAddresses
 *  that are not the receiver. This information is used in KeyValueTable emails to inject the subscription status of
 *  email addresses.
 */
fun TypedEmailContent.setLateInitVars(
    receivers: Map<EmailContact, UUID>,
    proxyPrimaryUrl: String,
    emailSubscriptionTracker: EmailSubscriptionTracker,
) {
    if (this is InitializeBaseUrlLater) {
        this.baseUrl = proxyPrimaryUrl
    }
    if (this is InitializeSubscriptionUuidLater) {
        require(receivers.size == 1)
        this.subscriptionUuid = receivers.values.first().toString()
    }
    if (this is KeyValueTable) {
        this.table.forEach { it.second.setLateInitVars(emailSubscriptionTracker) }
    }
}

/**
 * Initializes the subscription status of email addresses defined in the KeyValueTable.
 * @param emailSubscriptionTracker The emailSubscriptionTracker service used to obtain the subscription status of
 * email addresses.
 */
fun Value.setLateInitVars(emailSubscriptionTracker: EmailSubscriptionTracker) {
    when (this) {
        is Value.List -> this.values.forEach { it.setLateInitVars(emailSubscriptionTracker) }
        is Value.RelativeLink -> { /*nothing to do*/ }
        is Value.Text -> { /*nothing to do*/ }
        is Value.EmailAddressWithSubscriptionStatus ->
            this.subscribed = emailSubscriptionTracker.shouldReceiveEmail(this.emailAddress)
    }
}

/**
 * This function converts the TypedEmailContent received by the rabbit mq to the EmailContent required to send the email.
 * This function associates with every subtype of TypedEmailContent a subject, a template for the text content and a
 * template for the html content. Then it processes the templates with the instance of TypedEmailContent and
 * creates the EmailContent.
 */
@Suppress("LongMethod")
fun TypedEmailContent.build(): EmailContent =
    when (this) {
        is DatasetRequestedClaimOwnership ->
            EmailContent.fromTemplates(
                "A message from Dataland: Your ESG data are high on demand!",
                this,
                "/text/dataset_requested_claim_ownership.ftl",
                "/html/dataset_requested_claim_ownership.ftl",
            )
        is DataRequestAnswered ->
            EmailContent.fromTemplates(
                "Your data request has been answered!",
                this,
                "/text/data_request_answered.ftl",
                "/html/data_request_answered.ftl",
            )
        is DataRequestClosed ->
            EmailContent.fromTemplates(
                "Your data request has been closed!",
                this,
                "/text/data_request_closed.ftl",
                "/html/data_request_closed.ftl",
            )
        is CompanyOwnershipClaimApproved ->
            EmailContent.fromTemplates(
                "Your company ownership claim for ${this.companyName}" + " is confirmed!",
                this,
                "/text/company_ownership_claim_approved.ftl",
                "/html/company_ownership_claim_approved.ftl",
            )
        is AccessToDatasetRequested ->
            EmailContent.fromTemplates(
                "Access to your data has been requested on Dataland!",
                this,
                "/text/access_to_dataset_requested.ftl",
                "/html/access_to_dataset_requested.ftl",
            )
        is SingleDatasetUploadedEngagement ->
            EmailContent.fromTemplates(
                "New data for ${this.companyName} on Dataland",
                this,
                "/text/single_dataset_uploaded_engagement.ftl",
                "/html/single_dataset_uploaded_engagement.ftl",
            )
        is MultipleDatasetsUploadedEngagement ->
            EmailContent.fromTemplates(
                "New data for ${this.companyName} on Dataland",
                this,
                "/text/multiple_datasets_uploaded_engagement.ftl",
                "/html/multiple_datasets_uploaded_engagement.ftl",
            )
        is AccessToDatasetGranted ->
            EmailContent.fromTemplates(
                "Your Dataland Access Request has been granted!",
                this,
                "/text/access_to_dataset_granted.ftl",
                "/html/access_to_dataset_granted.ftl",
            )
        is KeyValueTable ->
            EmailContent.fromTemplates(
                this.subject,
                this,
                "/text/key_value_table.ftl",
                "/html/key_value_table.ftl",
            )
    }
