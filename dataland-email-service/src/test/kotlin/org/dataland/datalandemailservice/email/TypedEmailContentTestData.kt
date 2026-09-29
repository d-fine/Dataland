package org.dataland.datalandemailservice.email

import org.dataland.datalandmessagequeueutils.messages.email.CompanyOwnershipClaimApprovedEmailContent
import org.dataland.datalandmessagequeueutils.messages.email.DatasetAvailableClaimCompanyOwnershipEmailContent
import org.dataland.datalandmessagequeueutils.messages.email.InternalEmailContentTable
import org.dataland.datalandmessagequeueutils.messages.email.PortfolioMonitoringUpdateSummaryEmailContent
import org.dataland.datalandmessagequeueutils.messages.email.Value
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import java.util.UUID
import java.util.stream.Stream

class TypedEmailContentTestData : ArgumentsProvider {
    companion object {
        const val DATA_TYPE_A = "eutaxonomy-non-financials"
        const val DATA_TYPE_LABEL_A = "EU Taxonomy for non-financial companies"
        const val DATA_TYPE_LABEL_B = "PCAF"
        const val REPORTING_PERIOD_A = "2020"
        const val REPORTING_PERIOD_B = "2023"
        const val REPORTING_PERIOD_C = "2024"
        const val COMPANY_NAME = "Banana Inc."
        const val BASE_URL = "https://test.dataland.com"
        const val NUMBER_OF_DAYS = 23
        const val EMAIL_TITLE = "Email-Title"
    }

    private val companyId = UUID.randomUUID().toString()
    val subscriptionUuid = UUID.randomUUID().toString()

    private val companyOwnershipClaimApprovedEmailContent =
        CompanyOwnershipClaimApprovedEmailContent(
            companyId, COMPANY_NAME,
        ).also {
            it.baseUrl = BASE_URL
        }

    private val companyOwnershipClaimApprovedKeywords =
        listOf(
            companyId, COMPANY_NAME, BASE_URL,
            "You've successfully claimed company ownership for",
        )

    val portfolioChangesSummaryEmailContent =
        PortfolioMonitoringUpdateSummaryEmailContent(
            listOf(PortfolioMonitoringUpdateSummaryEmailContent.FrameworkData(DATA_TYPE_LABEL_A, REPORTING_PERIOD_A, listOf(COMPANY_NAME))),
            listOf(),
            listOf(),
            "Weekly",
            "SFDR portfolio",
        )

    val portfolioChangesSummaryKeywords =
        listOf(
            "summary for your portfolio(s):",
            "New Data", "Framework", DATA_TYPE_LABEL_A,
            "Reporting", REPORTING_PERIOD_A, // html has "Reporting Period", text has "Reporting period"
            "Company", COMPANY_NAME,
        )

    private val datasetUploadedClaimCompanyOwnershipEmailContent =
        DatasetAvailableClaimCompanyOwnershipEmailContent(
            companyId, COMPANY_NAME,
            listOf(
                DatasetAvailableClaimCompanyOwnershipEmailContent
                    .FrameworkData(DATA_TYPE_LABEL_A, listOf(REPORTING_PERIOD_A, REPORTING_PERIOD_B)),
                DatasetAvailableClaimCompanyOwnershipEmailContent.FrameworkData(DATA_TYPE_LABEL_B, listOf(REPORTING_PERIOD_C)),
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
            "subject", EMAIL_TITLE,
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
            Arguments.of(companyOwnershipClaimApprovedEmailContent, companyOwnershipClaimApprovedKeywords),
            Arguments.of(datasetUploadedClaimCompanyOwnershipEmailContent, datasetUploadedClaimCompanyOwnershipKeywords),
            Arguments.of(internalEmailContentTable, keyValueTableKeywords),
        )
}
