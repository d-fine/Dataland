package org.dataland.datalandemailservice.email

import org.dataland.datalandmessagequeueutils.messages.email.AccessToDatasetGranted
import org.dataland.datalandmessagequeueutils.messages.email.AccessToDatasetRequested
import org.dataland.datalandmessagequeueutils.messages.email.CompanyOwnershipClaimApproved
import org.dataland.datalandmessagequeueutils.messages.email.DataRequestAnswered
import org.dataland.datalandmessagequeueutils.messages.email.DataRequestClosed
import org.dataland.datalandmessagequeueutils.messages.email.DataRequestNonSourceable
import org.dataland.datalandmessagequeueutils.messages.email.DatasetRequestedClaimOwnership
import org.dataland.datalandmessagequeueutils.messages.email.InternalEmailContentTable
import org.dataland.datalandmessagequeueutils.messages.email.MultipleDatasetsUploadedEngagement
import org.dataland.datalandmessagequeueutils.messages.email.SingleDatasetUploadedEngagement
import org.dataland.datalandmessagequeueutils.messages.email.Value
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import java.util.UUID
import java.util.stream.Stream

class TypedEmailContentTestData : ArgumentsProvider {
    companion object {
        const val REQUESTER_EMAIL = "requester@example.com"
        const val DATA_TYPE_A = "eutaxonomy-non-financials"
        const val DATA_TYPE_LABEL_A = "EU Taxonomy for non-financial companies"
        const val DATA_TYPE_LABEL_B = "VSME"
        const val REPORTING_PERIOD_A = "2020"
        const val REPORTING_PERIOD_B = "2023"
        const val REPORTING_PERIOD_C = "2024"
        const val COMPANY_NAME = "Banana Inc."
        const val NUMBER_OF_OPEN_DATA_REQUEST_FOR_COMPANY = 10
        const val MESSAGE = "Some message"
        const val FIRST_NAME = "John"
        const val LAST_NAME = "Doe"
        const val BASE_URL = "https://test.dataland.com"
        const val CREATION_DATE = "October 5th"
        const val NUMBER_OF_DAYS = 23
        const val EMAIL_TITLE = "Email-Title"
        const val NON_SOURCEABLE_COMMENT = "No bananas means no data available. Donkey Kong ate all the bananas..."
    }

    val companyId = UUID.randomUUID().toString()
    val subscriptionUuid = UUID.randomUUID().toString()
    val dataRequestId = UUID.randomUUID().toString()

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

    val dataRequestNonSourceableMail =
        DataRequestNonSourceable(
            COMPANY_NAME, DATA_TYPE_LABEL_A, REPORTING_PERIOD_A, CREATION_DATE, dataRequestId, NON_SOURCEABLE_COMMENT,
        ).also {
            it.baseUrl = BASE_URL
        }

    val dataRequestNonSourceableKeywords =
        listOf(
            COMPANY_NAME, DATA_TYPE_LABEL_A, REPORTING_PERIOD_A, dataRequestId, BASE_URL, NON_SOURCEABLE_COMMENT,
            "Unfortunately, there are no sources available for your requested dataset according to the data provider.",
            "We will continue to check the status of your request regularly",
            "inform you in case the dataset will be uploaded in the future.",
            "If you are certain the requested data should exist, you may reopen your request ",
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
                MultipleDatasetsUploadedEngagement.FrameworkData(DATA_TYPE_LABEL_B, listOf(REPORTING_PERIOD_C)),
            ),
            NUMBER_OF_DAYS.toLong(),
        ).also {
            it.baseUrl = BASE_URL
            it.subscriptionUuid = subscriptionUuid
        }

    val multipleDatasetsUploadedEngagementKeywords =
        listOf(
            companyId, COMPANY_NAME, DATA_TYPE_LABEL_A, REPORTING_PERIOD_A, REPORTING_PERIOD_B, DATA_TYPE_LABEL_B, REPORTING_PERIOD_C,
            NUMBER_OF_DAYS.toString(), BASE_URL, subscriptionUuid,
            "CLAIM COMPANY OWNERSHIP",
        )

    val internalEmailContentTable =
        InternalEmailContentTable(
            "subject", EMAIL_TITLE, EMAIL_TITLE,
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
            EMAIL_TITLE,
            "testA@example.com (subscribed)",
            "testB@example.com (unsubscribed)",
            "@ValueB\$ValueC\$ValueD=",
            "TextA, TextB",
            "$BASE_URL/test",
            "$BASE_URL/example",
            "Link-TitleA",
            "Link-TitleB",
        )

    override fun provideArguments(p0: ExtensionContext?): Stream<out Arguments> =
        Stream.of(
            Arguments.of(datasetRequestedClaimOwnership, datasetRequestedClaimOwnershipKeywords),
            Arguments.of(dataRequestAnswered, dataRequestAnsweredKeywords),
            Arguments.of(dataRequestClosed, dataRequestClosedKeywords),
            Arguments.of(dataRequestNonSourceableMail, dataRequestNonSourceableKeywords),
            Arguments.of(companyOwnershipClaimApproved, companyOwnershipClaimApprovedKeywords),
            Arguments.of(accessToDatasetRequested, accessToDatasetRequestedKeywords),
            Arguments.of(accessToDatasetGranted, accessToDatasetGrantedKeywords),
            Arguments.of(singleDatasetUploadedEngagement, singleDatasetUploadedEngagementKeywords),
            Arguments.of(multipleDatasetsUploadedEngagement, multipleDatasetsUploadedEngagementKeywords),
            Arguments.of(internalEmailContentTable, keyValueTableKeywords),
        )
}
