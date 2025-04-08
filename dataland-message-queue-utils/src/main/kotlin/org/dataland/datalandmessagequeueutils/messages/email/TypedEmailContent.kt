package org.dataland.datalandmessagequeueutils.messages.email

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

/**
 * An abstract base class for the different email types that are send by the email-service.
 * Every subclass of [TypedEmailContent] has attached templates for the text and html content of the email.
 * In the email service the templates are build and the variables defined in the subclass of [TypedEmailContent]
 * are used (exactly!) as the context of the template building process.
 * Most variables are known when the email-service receives the content of the email.
 * However, some information is stored inside the email-service and thus some variables are defined as lateinit var's.
 * These variables are then initialized within the email-service.
 * Note, that these variables are annotated with @JsonIgnore to prevent the objectMapper from including these into the json.
 */
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type",
)
@JsonSubTypes(
    JsonSubTypes.Type(value = AccessToDatasetRequestedEmailContent::class, name = "AccessToDatasetRequestedEmailContent"),
    JsonSubTypes.Type(value = AccessToDatasetGrantedEmailContent::class, name = "AccessToDatasetGrantedEmailContent"),
    JsonSubTypes.Type(value = DataAvailableEmailContent::class, name = "DataAvailableEmailContent"),
    JsonSubTypes.Type(value = DataNonSourceableEmailContent::class, name = "DataNonSourceableEmailContent"),
    JsonSubTypes.Type(value = DataUpdatedEmailContent::class, name = "DataUpdatedEmailContent"),
    JsonSubTypes.Type(value = DataRequestSummaryEmailContent::class, name = "DataRequestSummaryEmailContent"),
    JsonSubTypes.Type(value = CompanyOwnershipClaimApprovedEmailContent::class, name = "CompanyOwnershipClaimApprovedEmailContent"),
    JsonSubTypes
        .Type(value = DatasetRequestedClaimCompanyOwnershipEmailContent::class, name = "DatasetRequestedClaimCompanyOwnershipEmailContent"),
    JsonSubTypes
        .Type(value = DatasetAvailableClaimCompanyOwnershipEmailContent::class, name = "DatasetAvailableClaimCompanyOwnershipEmailContent"),
    JsonSubTypes.Type(value = InternalEmailContentTable::class, name = "InternalEmailContentTable"),
)
sealed class TypedEmailContent {
    abstract val subject: String
    abstract val templateName: String
    var textTemplate: String = "to be initialized during build"
    var htmlTemplate: String = "to be initialized during build"
}

/**
 * Interface used to indicate the email-service that the subscriptionUuid should be initialized within the email-service.
 * Note, that this only works if the email has only one receiver.
 */
interface InitializeSubscriptionUuidLater {
    var subscriptionUuid: String
}

/**
 * Interface used to indicate the email-service that the baseUrl variable should be initialized within the email-service.
 * The baseUrl refers to the primaryProxyUrl of the dataland environment.
 */
interface InitializeBaseUrlLater {
    var baseUrl: String
}

/**
 * Content of an email sent to the company owner, when a user requests access to a company dataset.
 */
data class AccessToDatasetRequestedEmailContent(
    val companyId: String,
    val companyName: String,
    val dataTypeLabel: String,
    val reportingPeriods: List<String>,
    val message: String?,
    val requesterEmail: String?,
    val requesterFirstName: String?,
    val requesterLastName: String?,
) : TypedEmailContent(),
    InitializeBaseUrlLater {
    override val subject = "Access to your data has been requested on Dataland!"
    override val templateName = "access_to_dataset_requested.ftl"

    @JsonIgnore
    override lateinit var baseUrl: String
}

/**
 * Content of an email sent to the user, once the company grants access to a dataset requested by the user.
 */
data class AccessToDatasetGrantedEmailContent(
    val companyId: String,
    val companyName: String,
    val dataType: String,
    val dataTypeLabel: String,
    val reportingPeriod: String,
    val creationDate: String,
) : TypedEmailContent(),
    InitializeBaseUrlLater {
    override val subject = "Your Dataland Access Request has been granted!"
    override val templateName = "access_to_dataset_granted.ftl"

    @JsonIgnore
    override lateinit var baseUrl: String
}

/**
 * Content of an email sent to the user, when the user has opted for immediate notifications and
 * data for a data request becomes available for the first time.
 */
data class DataAvailableEmailContent(
    val companyName: String,
    val dataTypeLabel: String,
    val reportingPeriod: String,
    val creationDate: String,
    val dataRequestId: String,
    val closedInDays: Int,
) : TypedEmailContent(),
    InitializeBaseUrlLater {
    override val subject = "Your data request has been answered!"
    override val templateName = "data_request_immediate_notification_on_data_available.ftl"

    @JsonIgnore
    override lateinit var baseUrl: String
}

/**
 * Content of an email sent to the user, when the user has opted for immediate notifications and
 * data for a data request becomes was updated.
 */
data class DataUpdatedEmailContent(
    val companyName: String,
    val dataTypeLabel: String,
    val reportingPeriod: String,
    val creationDate: String,
    val dataRequestId: String,
) : TypedEmailContent(),
    InitializeBaseUrlLater {
    override val subject = "Your data request has been updated!"
    override val templateName = "data_request_immediate_notification_on_data_updated.ftl"

    @JsonIgnore
    override lateinit var baseUrl: String
}

/**
 * Content of an email sent to the user, when the user has opted for immediate notifications and
 * data for a data request is not providable.
 */
data class DataNonSourceableEmailContent(
    val companyName: String,
    val dataTypeLabel: String,
    val reportingPeriod: String,
    val creationDate: String,
    val dataRequestId: String,
    val nonSourceableComment: String?,
) : TypedEmailContent(),
    InitializeBaseUrlLater {
    override val subject = "There are no sources for your requested data available!"
    override val templateName = "data_request_immediate_notification_on_data_non_sourceable.ftl"

    @JsonIgnore
    override lateinit var baseUrl: String
}

/**
 * Content of an email sent to the user, when the user receives weekly update summaries (default) and
 * data for data requests is available, updated or not sourceable.
 */
data class DataRequestSummaryEmailContent(
    val newData: List<FrameworkData>,
    val updatedData: List<FrameworkData>,
    val nonsourceableData: List<FrameworkData>,
) : TypedEmailContent(),
    InitializeBaseUrlLater {
    override val subject = "Summary for your data requests changes!"
    override val templateName = "data_request_summary.ftl"

    /**
     * A class that stores the information about the multiple frameworks that have been changed.
     */
    data class FrameworkData(
        val dataTypeLabel: String,
        val reportingPeriod: String,
        val companies: List<String>,
    )

    @JsonIgnore
    override lateinit var baseUrl: String
}

/**
 * Content of an email sent to the company's contact, when their request to claim ownership is approved.
 */
data class CompanyOwnershipClaimApprovedEmailContent(
    val companyId: String,
    val companyName: String,
    val numberOfOpenDataRequestsForCompany: Int,
) : TypedEmailContent(),
    InitializeBaseUrlLater {
    override val subject = "Your company ownership claim for ${this.companyName} is confirmed!"
    override val templateName = "company_ownership_claim_approved.ftl"

    @JsonIgnore
    override lateinit var baseUrl: String
}

/**
 * Content of an email sent to the company's contact prompting them to claim ownership,
 * triggered when a dataset is requested for this company that has no designated owner.
 */
data class DatasetRequestedClaimCompanyOwnershipEmailContent(
    val companyId: String,
    val companyName: String,
    val requesterEmail: String,
    val dataTypeLabel: String,
    val reportingPeriods: List<String>,
    val message: String?,
    val firstName: String?,
    val lastName: String?,
) : TypedEmailContent(),
    InitializeSubscriptionUuidLater,
    InitializeBaseUrlLater {
    override val subject = "A message from Dataland: Your data are high on demand!"
    override val templateName = "company_ownership_claim_request_prompt_on_dataset_requested.ftl"

    @JsonIgnore
    override lateinit var subscriptionUuid: String

    @JsonIgnore
    override lateinit var baseUrl: String
}

/**
 * "Investor Relationships" Email Content:
 * Content of an email sent to the company's contact prompting them to claim ownership,
 * triggered when a dataset is available (uploaded + approved) for this company that has no designated owner.
 */
data class DatasetAvailableClaimCompanyOwnershipEmailContent(
    val companyId: String,
    val companyName: String,
    val frameworkData: List<FrameworkData>,
) : TypedEmailContent(),
    InitializeSubscriptionUuidLater,
    InitializeBaseUrlLater {
    override val subject = "New data for ${this.companyName} on Dataland"
    override val templateName = "company_ownership_claim_request_prompt_on_dataset_available.ftl"

    /**
     * A class that stores the information about the frameworks that have been uploaded for the company.
     */
    data class FrameworkData(
        val dataTypeLabel: String,
        val reportingPeriods: List<String>,
    )

    @JsonIgnore
    override lateinit var subscriptionUuid: String

    @JsonIgnore
    override lateinit var baseUrl: String
}

/**
 * A class for the generic KeyValueTable email content, used for internal purposes.
 * E.g. sent simultaneously with: Dataset Available - Claim Company Ownership email
 */
data class InternalEmailContentTable(
    override val subject: String,
    val title: String,
    val table: List<Pair<String, Value>>,
) : TypedEmailContent(),
    InitializeBaseUrlLater {
    override val templateName = "internal_key_value_table.ftl"

    @JsonIgnore
    override lateinit var baseUrl: String
}

/**
 * The KeyValueTable can display generic Values in the Table.
 * These values can be Text, Links, Lists etc.
 * This type is the abstract base class for the different values.
 * Note, that every value requires a macroName that is used within the ftl template (for text and html) to convert the
 * value into the displayed html or text.
 */
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type",
)
@JsonSubTypes(
    JsonSubTypes.Type(value = Value.Text::class, name = "Text"),
    JsonSubTypes.Type(value = Value.List::class, name = "List"),
)
sealed class Value {
    abstract val macroName: String

    /**
     * A value that represents text.
     */
    data class Text(
        val value: String,
    ) : Value() {
        @JsonIgnore
        override val macroName = "text_macro"
    }

    /**
     * A value that represents a list.
     */
    data class List(
        val values: kotlin.collections.List<Value>,
        val separator: String = ", ",
        val start: String = "",
        val end: String = "",
    ) : Value() {
        constructor(
            vararg values: Value,
            separator: String = ", ",
            start: String = "",
            end: String = "",
        ) : this(values.toList(), separator, start, end)

        @JsonIgnore
        override val macroName = "list_macro"
    }

    /**
     * A value that represents link relative to the dataland baseUrl.
     */
    data class RelativeLink(
        val href: String,
        val title: String,
    ) : Value() {
        @JsonIgnore
        override val macroName = "link_macro"
    }

    /**
     * A value that represents an email address with its subscription status.
     * Note, that the subscription status is set within the email-service.
     * (Also note that a boolean cannot be a lateinit var.)
     */
    data class EmailAddressWithSubscriptionStatus(
        val emailAddress: String,
    ) : Value() {
        @JsonIgnore
        override val macroName = "email_address_with_subscription_status_macro"

        @JsonIgnore
        var subscribed: Boolean = false
    }
}
