package org.dataland.datalandemailservice.email

import org.dataland.datalandmessagequeueutils.messages.email.AccessToDatasetGranted
import org.dataland.datalandmessagequeueutils.messages.email.AccessToDatasetRequested
import org.dataland.datalandmessagequeueutils.messages.email.CompanyOwnershipClaimApproved
import org.dataland.datalandmessagequeueutils.messages.email.DataRequestAnswered
import org.dataland.datalandmessagequeueutils.messages.email.DataRequestClosed
import org.dataland.datalandmessagequeueutils.messages.email.DatasetRequestedClaimOwnership
import org.dataland.datalandmessagequeueutils.messages.email.KeyValueTable
import org.dataland.datalandmessagequeueutils.messages.email.MultipleDatasetsUploadedEngagement
import org.dataland.datalandmessagequeueutils.messages.email.SingleDatasetUploadedEngagement
import org.dataland.datalandmessagequeueutils.messages.email.TypedEmailContent
import org.dataland.datalandmessagequeueutils.messages.email.Value
import java.util.*

object TypedEmailContentTestData {
    val companyId = UUID.randomUUID().toString()
    const val companyName = "Banana Inc."
    const val numberOfOpenDataRequestForCompany = 10

    const val requesterEmail = "requester@example.com"
    const val dataTypeA = "eutaxonomy-non-financials"
    const val dataTypeLabelA = "EU Taxonomy for non-financial companies"
    const val dataTypeLabelB = "VSME"
    const val reportingPeriodA = "2020"
    const val reportingPeriodB = "2023"
    const val reportinPeriodC = "2024"

    const val message = "Some message"
    const val firstName = "John"
    const val lastName = "Doe"
    val subscriptionUuid = UUID.randomUUID().toString()
    const val baseUrl = "test.dataland.com"

    const val creationDate = "October 5th"
    val dataRequestId = UUID.randomUUID().toString()
    const val numberOfDays = 23

    val datasetRequestedClaimOwnership =
        DatasetRequestedClaimOwnership(
            companyId, companyName, requesterEmail, dataTypeLabelA, listOf(reportingPeriodA, reportingPeriodB),
            message, firstName, lastName,
        ).also {
            it.subscriptionUuid = subscriptionUuid
            it.baseUrl = baseUrl
        }

    val datasetRequestedClaimOwnershipKeywords =
        listOf(
            companyId, companyName, requesterEmail, dataTypeLabelA, reportingPeriodA,
            reportingPeriodB, message, firstName, lastName, subscriptionUuid, baseUrl,
            "REGISTER AND CLAIM OWNERSHIP",
        )

    val dataRequestAnswered =
        DataRequestAnswered(
            companyName, dataTypeLabelA, reportingPeriodA, creationDate, dataRequestId, numberOfDays,
        ).also {
            it.baseUrl = baseUrl
        }

    val dataRequestAnsweredKeywords =
        listOf(
            companyName, dataTypeLabelA, reportingPeriodA, creationDate, dataRequestId, numberOfDays.toString(), baseUrl,
            "Your data request has been answered.",
        )

    val dataRequestClosed =
        DataRequestClosed(
            companyName, dataTypeLabelA, reportingPeriodA, creationDate, dataRequestId, numberOfDays,
        ).also {
            it.baseUrl = baseUrl
        }

    val dataRequestClosedKeywords =
        listOf(
            companyName, dataTypeLabelA, reportingPeriodA, creationDate, dataRequestId, numberOfDays.toString(), baseUrl,
            "Your answered data request has been automatically closed as no action was taken within the last",
        )

    val companyOwnershipClaimApproved =
        CompanyOwnershipClaimApproved(
            companyId, companyName, numberOfOpenDataRequestForCompany,
        ).also {
            it.baseUrl = baseUrl
        }

    val companyOwnershipClaimApprovedKeywords =
        listOf(
            companyId, companyName, numberOfOpenDataRequestForCompany.toString(), baseUrl,
            "You've successfully claimed company ownership for",
        )

    val accessToDatasetRequested =
        AccessToDatasetRequested(
            companyId, companyName, dataTypeLabelA, listOf(reportingPeriodA, reportingPeriodB),
            message, requesterEmail, firstName, lastName,
        ).also {
            it.baseUrl = baseUrl
        }

    val accessToDatasetRequestedKeywords =
        listOf(
            companyId, companyName, dataTypeLabelA, reportingPeriodA, reportingPeriodB,
            message, requesterEmail, firstName, lastName, baseUrl,
            "is requesting access to your data from",
        )

    val accessToDatasetGranted =
        AccessToDatasetGranted(
            companyId, companyName, dataTypeA, dataTypeLabelA, reportingPeriodA, creationDate,
        ).also {
            it.baseUrl = baseUrl
        }

    val accessToDatasetGrantedKeywords =
        listOf(
            companyId, companyName, dataTypeA, dataTypeLabelA, reportingPeriodA, creationDate, baseUrl,
            "You have now access to the following dataset on Dataland",
        )

    val singleDatasetUploadedEngagement =
        SingleDatasetUploadedEngagement(
            companyId, companyName, dataTypeLabelA, reportingPeriodA,
        ).also {
            it.baseUrl = baseUrl
            it.subscriptionUuid = subscriptionUuid
        }

    val singleDatasetUploadedEngagementKeywords =
        listOf(
            companyId, dataTypeLabelA, reportingPeriodA, baseUrl, subscriptionUuid,
            "We are Dataland, an open, neutral, and transparent data engagement platform.",
            "One of our members has specifically requested data about your company.",
            "A data provider within our network has created a dataset for your company, which is now accessible on Dataland:",
            "CLAIM COMPANY OWNERSHIP",
        )

    val multipleDatasetsUploadedEngagement =
        MultipleDatasetsUploadedEngagement(
            companyId, companyName,
            listOf(
                MultipleDatasetsUploadedEngagement.FrameworkData(dataTypeLabelA, listOf(reportingPeriodA, reportingPeriodB)),
                MultipleDatasetsUploadedEngagement.FrameworkData(dataTypeLabelB, listOf(reportinPeriodC)),
            ),
            numberOfDays.toLong(),
        ).also {
            it.baseUrl = baseUrl
            it.subscriptionUuid = subscriptionUuid
        }

    val multipleDatasetsUploadedEngagementKeywords =
        listOf(
            companyId, companyName, dataTypeLabelA, reportingPeriodA, reportingPeriodB, dataTypeLabelB, reportinPeriodC,
            numberOfDays.toString(), baseUrl, subscriptionUuid,
            "CLAIM COMPANY OWNERSHIP",
        )

    val keyValueTable =
        KeyValueTable(
            "subject", "Email-Title", "Email-Title",
            listOf(
                "Key0" to Value.Text("ValueA"),
                "Key1" to Value.RelativeLink("/example", "Link-TitleA"),
                "Key2" to Value.List(Value.Text("TextA"), Value.Text("TextB"), Value.RelativeLink("/test", "Link-TitleB")),
                "Key3" to Value.EmailAddressWithSubscriptionStatus("testA@example.com"),
                "Key4" to Value.EmailAddressWithSubscriptionStatus("testB@example.com"),
                "key5" to
                    Value.List(
                        Value.Text("ValueB"), Value.Text("ValueC"), Value.Text("ValueD"),
                        separator = "$", start = "@", end = "=",
                    ),
            ),
        ).also {
            it.baseUrl = baseUrl
            (it.table[3].second as Value.EmailAddressWithSubscriptionStatus).subscribed = true
            (it.table[4].second as Value.EmailAddressWithSubscriptionStatus).subscribed = false
        }

    val keyValueTableKeywords =
        listOf(
            "Email-Title",
            "testA@example.com (subscribed)",
            "testB@example.com (unsubscribed)",
            "@ValueB\$ValueC\$ValueD=",
            "TextA, TextB",
            "https://$baseUrl/test",
            "https://$baseUrl/example",
            "Link-TitleA",
            "Link-TitleB",
        )

    val contentToKeywordsMap: List<Pair<TypedEmailContent, List<String>>> =
        listOf(
            datasetRequestedClaimOwnership to datasetRequestedClaimOwnershipKeywords,
            dataRequestAnswered to dataRequestAnsweredKeywords,
            dataRequestClosed to dataRequestClosedKeywords,
            companyOwnershipClaimApproved to companyOwnershipClaimApprovedKeywords,
            accessToDatasetRequested to accessToDatasetRequestedKeywords,
            accessToDatasetGranted to accessToDatasetGrantedKeywords,
            singleDatasetUploadedEngagement to singleDatasetUploadedEngagementKeywords,
            multipleDatasetsUploadedEngagement to multipleDatasetsUploadedEngagementKeywords,
            keyValueTable to keyValueTableKeywords,
        )
}
