package org.dataland.datalandemailservice.email

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
import java.util.*

fun TypedEmailContent.setLateInitVars(receivers: Map<EmailContact, UUID>, proxyPrimaryUrl: String) {
    if (this is InitializeBaseUrlLater) {
        this.baseUrl = proxyPrimaryUrl
    }
    if (this is InitializeSubscriptionUuidLater) {
        require(receivers.size == 1)
        this.subscriptionUuid = receivers.values.first().toString()
    }
    if (this is KeyValueTable) {
        this.table.forEach { it.second.setLateInitVars() }
    }
}

fun Value.setLateInitVars() {
    when (this) {
        is Value.List -> this.values.forEach { it.setLateInitVars() }
        is Value.RelativeLink -> { /*nothing to do*/ }
        is Value.Text -> { /*nothing to do*/ }
        is Value.EmailAddressWithSubscriptionStatus -> this.subscribed = true // TODO fix this
    }
}

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
