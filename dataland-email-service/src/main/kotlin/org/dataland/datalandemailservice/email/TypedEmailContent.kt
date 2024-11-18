package org.dataland.datalandemailservice.email

import org.dataland.datalandemailservice.services.EmailSubscriptionTracker
import org.dataland.datalandmessagequeueutils.messages.email.InitializeBaseUrlLater
import org.dataland.datalandmessagequeueutils.messages.email.InitializeSubscriptionUuidLater
import org.dataland.datalandmessagequeueutils.messages.email.InternalEmailContentTable
import org.dataland.datalandmessagequeueutils.messages.email.TypedEmailContent
import org.dataland.datalandmessagequeueutils.messages.email.Value
import java.util.UUID

/**
 * This function initializes the remaining variables required to create the content of the emails.
 * This includes late init variables that are declared through an interface and also variables that are
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
        this.baseUrl = "https://$proxyPrimaryUrl"
    }
    if (this is InitializeSubscriptionUuidLater) {
        require(receivers.size == 1)
        this.subscriptionUuid = receivers.values.first().toString()
    }
    if (this is InternalEmailContentTable) {
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
            this.subscribed =
                emailSubscriptionTracker.shouldReceiveEmail(
                    EmailContact.create(this.emailAddress),
                )
    }
}

/**
 * This function converts the TypedEmailContent received by the rabbit mq to the EmailContent required to send the email.
 * This function associates with every subtype of TypedEmailContent a subject, a template for the text content and a
 * template for the html content. Then it processes the templates with the instance of TypedEmailContent and
 * creates the EmailContent.
 */
fun TypedEmailContent.build(): EmailContent =
    EmailContent.fromTemplates(
        this.subject, this, this.textTemplate, this.htmlTemplate,
    )
