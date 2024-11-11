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
import java.util.UUID

object TypedEmailContentTestData {
    val companyId = UUID.randomUUID().toString()
    const val COMPANY_NAME = "Banana Inc."
    const val NUMBER_OF_OPEN_DATA_REQUEST_FOR_COMPANY = 10

    const val REQUESTER_EMAIL = "requester@example.com"
    const val DATA_TYPE_A = "eutaxonomy-non-financials"
    const val DATA_TYPE_LABEL_A = "EU Taxonomy for non-financial companies"
    const val DATA_TYPE_LABEL_B = "VSME"
    const val REPORTING_PERIOD_A = "2020"
    const val REPORTING_PERIOD_B = "2023"
    const val REPORTIN_PERIOD_C = "2024"

    const val MESSAGE = "Some message"
    const val FIRST_NAME = "John"
    const val LAST_NAME = "Doe"
    val subscriptionUuid = UUID.randomUUID().toString()
    const val BASE_URL = "test.dataland.com"

    const val CREATION_DATE = "October 5th"
    val dataRequestId = UUID.randomUUID().toString()
    const val NUMBER_OF_DAYS = 23

    val datasetRequestedClaimOwnership =
        DatasetRequestedClaimOwnership(
            companyId, COMPANY_NAME, REQUESTER_EMAIL, DATA_TYPE_LABEL_A, listOf(REPORTING_PERIOD_A, REPORTING_PERIOD_B),
            MESSAGE, FIRST_NAME, LAST_NAME,
        ).also {
            it.subscriptionUuid = subscriptionUuid
            it.baseUrl = BASE_URL
        }

    val datasetRequestedClaimOwnershipKeywords =
        listOf(
            companyId, COMPANY_NAME, REQUESTER_EMAIL, DATA_TYPE_LABEL_A, REPORTING_PERIOD_A,
            REPORTING_PERIOD_B, MESSAGE, FIRST_NAME, LAST_NAME, subscriptionUuid, BASE_URL,
            "REGISTER AND CLAIM OWNERSHIP",
        )

    val dataRequestAnswered =
        DataRequestAnswered(
            COMPANY_NAME, DATA_TYPE_LABEL_A, REPORTING_PERIOD_A, CREATION_DATE, dataRequestId, NUMBER_OF_DAYS,
        ).also {
            it.baseUrl = BASE_URL
        }

    val dataRequestAnsweredKeywords =
        listOf(
            COMPANY_NAME, DATA_TYPE_LABEL_A, REPORTING_PERIOD_A, CREATION_DATE, dataRequestId, NUMBER_OF_DAYS.toString(), BASE_URL,
            "Your data request has been answered.",
        )

    val dataRequestClosed =
        DataRequestClosed(
            COMPANY_NAME, DATA_TYPE_LABEL_A, REPORTING_PERIOD_A, CREATION_DATE, dataRequestId, NUMBER_OF_DAYS,
        ).also {
            it.baseUrl = BASE_URL
        }

    val dataRequestClosedKeywords =
        listOf(
            COMPANY_NAME, DATA_TYPE_LABEL_A, REPORTING_PERIOD_A, CREATION_DATE, dataRequestId, NUMBER_OF_DAYS.toString(), BASE_URL,
            "Your answered data request has been automatically closed as no action was taken within the last",
        )

    val companyOwnershipClaimApproved =
        CompanyOwnershipClaimApproved(
            companyId, COMPANY_NAME, NUMBER_OF_OPEN_DATA_REQUEST_FOR_COMPANY,
        ).also {
            it.baseUrl = BASE_URL
        }

    val companyOwnershipClaimApprovedKeywords =
        listOf(
            companyId, COMPANY_NAME, NUMBER_OF_OPEN_DATA_REQUEST_FOR_COMPANY.toString(), BASE_URL,
            "You've successfully claimed company ownership for",
        )

    val accessToDatasetRequested =
        AccessToDatasetRequested(
            companyId, COMPANY_NAME, DATA_TYPE_LABEL_A, listOf(REPORTING_PERIOD_A, REPORTING_PERIOD_B),
            MESSAGE, REQUESTER_EMAIL, FIRST_NAME, LAST_NAME,
        ).also {
            it.baseUrl = BASE_URL
        }

    val accessToDatasetRequestedKeywords =
        listOf(
            companyId, COMPANY_NAME, DATA_TYPE_LABEL_A, REPORTING_PERIOD_A, REPORTING_PERIOD_B,
            MESSAGE, REQUESTER_EMAIL, FIRST_NAME, LAST_NAME, BASE_URL,
            "is requesting access to your data from",
        )

    val accessToDatasetGranted =
        AccessToDatasetGranted(
            companyId, COMPANY_NAME, DATA_TYPE_A, DATA_TYPE_LABEL_A, REPORTING_PERIOD_A, CREATION_DATE,
        ).also {
            it.baseUrl = BASE_URL
        }

    val accessToDatasetGrantedKeywords =
        listOf(
            companyId, COMPANY_NAME, DATA_TYPE_A, DATA_TYPE_LABEL_A, REPORTING_PERIOD_A, CREATION_DATE, BASE_URL,
            "You have now access to the following dataset on Dataland",
        )

    val singleDatasetUploadedEngagement =
        SingleDatasetUploadedEngagement(
            companyId, COMPANY_NAME, DATA_TYPE_LABEL_A, REPORTING_PERIOD_A,
        ).also {
            it.baseUrl = BASE_URL
            it.subscriptionUuid = subscriptionUuid
        }

    val singleDatasetUploadedEngagementKeywords =
        listOf(
            companyId, DATA_TYPE_LABEL_A, REPORTING_PERIOD_A, BASE_URL, subscriptionUuid,
            "We are Dataland, an open, neutral, and transparent data engagement platform.",
            "One of our members has specifically requested data about your company.",
            "A data provider within our network has created a dataset for your company, which is now accessible on Dataland:",
            "CLAIM COMPANY OWNERSHIP",
        )

    val multipleDatasetsUploadedEngagement =
        MultipleDatasetsUploadedEngagement(
            companyId, COMPANY_NAME,
            listOf(
                MultipleDatasetsUploadedEngagement.FrameworkData(DATA_TYPE_LABEL_A, listOf(REPORTING_PERIOD_A, REPORTING_PERIOD_B)),
                MultipleDatasetsUploadedEngagement.FrameworkData(DATA_TYPE_LABEL_B, listOf(REPORTIN_PERIOD_C)),
            ),
            NUMBER_OF_DAYS.toLong(),
        ).also {
            it.baseUrl = BASE_URL
            it.subscriptionUuid = subscriptionUuid
        }

    val multipleDatasetsUploadedEngagementKeywords =
        listOf(
            companyId, COMPANY_NAME, DATA_TYPE_LABEL_A, REPORTING_PERIOD_A, REPORTING_PERIOD_B, DATA_TYPE_LABEL_B, REPORTIN_PERIOD_C,
            NUMBER_OF_DAYS.toString(), BASE_URL, subscriptionUuid,
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
            it.baseUrl = BASE_URL
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
            "https://$BASE_URL/test",
            "https://$BASE_URL/example",
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
