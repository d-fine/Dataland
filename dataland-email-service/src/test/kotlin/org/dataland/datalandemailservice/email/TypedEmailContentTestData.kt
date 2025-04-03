package org.dataland.datalandemailservice.email

import org.dataland.datalandmessagequeueutils.messages.email.AccessToDatasetGrantedEmailContent
import org.dataland.datalandmessagequeueutils.messages.email.AccessToDatasetRequestedEmailContent
import org.dataland.datalandmessagequeueutils.messages.email.CompanyOwnershipClaimApprovedEmailContent
import org.dataland.datalandmessagequeueutils.messages.email.DataAvailableEmailContent
import org.dataland.datalandmessagequeueutils.messages.email.DataNonSourceableEmailContent
import org.dataland.datalandmessagequeueutils.messages.email.DataUpdatedEmailContent
import org.dataland.datalandmessagequeueutils.messages.email.DatasetRequestedClaimCompanyOwnershipEmailContent
import org.dataland.datalandmessagequeueutils.messages.email.DatasetUploadedClaimCompanyOwnershipEmailContent
import org.dataland.datalandmessagequeueutils.messages.email.InternalEmailContentTable
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

    private val companyId = UUID.randomUUID().toString()
    val subscriptionUuid = UUID.randomUUID().toString()
    private val dataRequestId = UUID.randomUUID().toString()

    private val datasetRequestedClaimCompanyOwnershipEmailContent =
        DatasetRequestedClaimCompanyOwnershipEmailContent(
            companyId, COMPANY_NAME, REQUESTER_EMAIL, DATA_TYPE_LABEL_A, listOf(REPORTING_PERIOD_A, REPORTING_PERIOD_B),
            MESSAGE, FIRST_NAME, LAST_NAME,
        ).also {
            it.subscriptionUuid = subscriptionUuid
            it.baseUrl = BASE_URL
        }

    private val datasetRequestedClaimOwnershipKeywords =
        listOf(
            companyId, COMPANY_NAME, REQUESTER_EMAIL, DATA_TYPE_LABEL_A, REPORTING_PERIOD_A,
            REPORTING_PERIOD_B, MESSAGE, FIRST_NAME, LAST_NAME, subscriptionUuid, BASE_URL,
            "REGISTER AND CLAIM OWNERSHIP",
        )

    private val dataAvailableEmailContent =
        DataAvailableEmailContent(
            COMPANY_NAME, DATA_TYPE_LABEL_A, REPORTING_PERIOD_A, CREATION_DATE, dataRequestId, NUMBER_OF_DAYS,
        ).also {
            it.baseUrl = BASE_URL
        }

    private val dataAvailableKeywords =
        listOf(
            COMPANY_NAME, DATA_TYPE_LABEL_A, REPORTING_PERIOD_A, CREATION_DATE, dataRequestId, NUMBER_OF_DAYS.toString(), BASE_URL,
            "Your data request has been answered.",
        )

    private val dataUpdatedEmailContent =
        DataUpdatedEmailContent(COMPANY_NAME, DATA_TYPE_LABEL_A, REPORTING_PERIOD_A, CREATION_DATE, dataRequestId).also {
            it.baseUrl = BASE_URL
        }

    private val dataUpdatedKeywords =
        listOf(
            COMPANY_NAME, DATA_TYPE_LABEL_A, REPORTING_PERIOD_A, CREATION_DATE, dataRequestId, BASE_URL,
            "Your data request has been updated with new data.",
        )

    val dataNonSourceableEmailContent =
        DataNonSourceableEmailContent(
            COMPANY_NAME, DATA_TYPE_LABEL_A, REPORTING_PERIOD_A, CREATION_DATE, dataRequestId, NON_SOURCEABLE_COMMENT,
        ).also {
            it.baseUrl = BASE_URL
        }

    val dataNonSourceableKeywords =
        listOf(
            COMPANY_NAME, DATA_TYPE_LABEL_A, REPORTING_PERIOD_A, dataRequestId, BASE_URL, NON_SOURCEABLE_COMMENT,
            "Unfortunately, no public sources could be found for your requested dataset by a data provider.",
            "We will continue to check the status of your request regularly",
            "inform you in case the dataset will be uploaded in the future.",
            "If you are certain the requested data should exist, you may reopen your request ",
        )

    private val companyOwnershipClaimApprovedEmailContent =
        CompanyOwnershipClaimApprovedEmailContent(
            companyId, COMPANY_NAME, NUMBER_OF_OPEN_DATA_REQUEST_FOR_COMPANY,
        ).also {
            it.baseUrl = BASE_URL
        }

    private val companyOwnershipClaimApprovedKeywords =
        listOf(
            companyId, COMPANY_NAME, NUMBER_OF_OPEN_DATA_REQUEST_FOR_COMPANY.toString(), BASE_URL,
            "You've successfully claimed company ownership for",
        )

    val accessToDatasetRequestedEmailContent =
        AccessToDatasetRequestedEmailContent(
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

    private val accessToDatasetGrantedEmailContent =
        AccessToDatasetGrantedEmailContent(
            companyId, COMPANY_NAME, DATA_TYPE_A, DATA_TYPE_LABEL_A, REPORTING_PERIOD_A, CREATION_DATE,
        ).also {
            it.baseUrl = BASE_URL
        }

    private val accessToDatasetGrantedKeywords =
        listOf(
            companyId, COMPANY_NAME, DATA_TYPE_A, DATA_TYPE_LABEL_A, REPORTING_PERIOD_A, CREATION_DATE, BASE_URL,
            "You have now access to the following dataset on Dataland",
        )

    private val datasetUploadedClaimCompanyOwnershipEmailContent =
        DatasetUploadedClaimCompanyOwnershipEmailContent(
            companyId, COMPANY_NAME,
            listOf(
                DatasetUploadedClaimCompanyOwnershipEmailContent
                    .FrameworkData(DATA_TYPE_LABEL_A, listOf(REPORTING_PERIOD_A, REPORTING_PERIOD_B)),
                DatasetUploadedClaimCompanyOwnershipEmailContent.FrameworkData(DATA_TYPE_LABEL_B, listOf(REPORTING_PERIOD_C)),
            ),
        ).also {
            it.baseUrl = BASE_URL
            it.subscriptionUuid = subscriptionUuid
        }

    private val datasetUploadedClaimCompanyOwnershipKeywords =
        listOf(
            companyId, DATA_TYPE_LABEL_A, REPORTING_PERIOD_A, REPORTING_PERIOD_B, DATA_TYPE_LABEL_B, REPORTING_PERIOD_C,
            NUMBER_OF_DAYS.toString(), BASE_URL, subscriptionUuid,
            "CLAIM COMPANY OWNERSHIP",
        )

    private val internalEmailContentTable =
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

    private val keyValueTableKeywords =
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
            Arguments.of(dataAvailableEmailContent, dataAvailableKeywords),
            Arguments.of(dataUpdatedEmailContent, dataUpdatedKeywords),
            Arguments.of(dataNonSourceableEmailContent, dataNonSourceableKeywords),
            Arguments.of(companyOwnershipClaimApprovedEmailContent, companyOwnershipClaimApprovedKeywords),
            Arguments.of(accessToDatasetRequestedEmailContent, accessToDatasetRequestedKeywords),
            Arguments.of(accessToDatasetGrantedEmailContent, accessToDatasetGrantedKeywords),
            Arguments.of(datasetRequestedClaimCompanyOwnershipEmailContent, datasetRequestedClaimOwnershipKeywords),
            Arguments.of(datasetUploadedClaimCompanyOwnershipEmailContent, datasetUploadedClaimCompanyOwnershipKeywords),
            Arguments.of(internalEmailContentTable, keyValueTableKeywords),
        )
}
