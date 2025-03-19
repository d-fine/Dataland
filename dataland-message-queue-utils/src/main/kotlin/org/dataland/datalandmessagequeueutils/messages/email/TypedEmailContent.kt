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
    JsonSubTypes.Type(value = DatasetRequestedClaimOwnership::class, name = "DatasetRequestedClaimOwnership"),
    JsonSubTypes.Type(value = AccessToDatasetRequested::class, name = "AccessToDatasetRequested"),
    JsonSubTypes.Type(value = AccessToDatasetRequested::class, name = "AccessToDatasetGranted"),
    JsonSubTypes.Type(value = SingleDatasetUploadedEngagement::class, name = "SingleDatasetUploadedEngagement"),
    JsonSubTypes.Type(value = MultipleDatasetsUploadedEngagement::class, name = "MultipleDatasetsUploadedEngagement"),
    JsonSubTypes.Type(value = CompanyOwnershipClaimApproved::class, name = "CompanyOwnershipClaimApproved"),
    JsonSubTypes.Type(value = DataRequestAnswered::class, name = "DataRequestAnswered"),
    JsonSubTypes.Type(value = DataRequestAnswered::class, name = "DataRequestAnswered"),
    JsonSubTypes.Type(value = DataRequestClosed::class, name = "DataRequestClosed"),
    JsonSubTypes.Type(value = InternalEmailContentTable::class, name = "KeyValueTable"),
)
sealed class TypedEmailContent {
    abstract val subject: String
    abstract val textTemplate: String
    abstract val htmlTemplate: String
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
 * A class for the DatasetRequestedClaimOwnership email.
 */
data class DatasetRequestedClaimOwnership(
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
    override val textTemplate = "/text/company_ownership_claim_dataset_requested.ftl"
    override val htmlTemplate = "/html/company_ownership_claim_dataset_requested.ftl"

    @JsonIgnore
    override lateinit var subscriptionUuid: String

    @JsonIgnore
    override lateinit var baseUrl: String
}

/**
 * A class for the AccessToDatasetRequested email.
 */
data class AccessToDatasetRequested(
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
    override val textTemplate = "/text/access_to_dataset_requested.ftl"
    override val htmlTemplate = "/html/access_to_dataset_requested.ftl"

    @JsonIgnore
    override lateinit var baseUrl: String
}

/**
 * A class for the AccessToDatasetGranted email.
 */
data class AccessToDatasetGranted(
    val companyId: String,
    val companyName: String,
    val dataType: String,
    val dataTypeLabel: String,
    val reportingPeriod: String,
    val creationDate: String,
) : TypedEmailContent(),
    InitializeBaseUrlLater {
    override val subject = "Your Dataland Access Request has been granted!"
    override val textTemplate = "/text/access_to_dataset_granted.ftl"
    override val htmlTemplate = "/html/access_to_dataset_granted.ftl"

    @JsonIgnore
    override lateinit var baseUrl: String
}

/**
 * A class for the SingleDatasetUploadedEngagement email.
 */
data class SingleDatasetUploadedEngagement(
    val companyId: String,
    val companyName: String,
    val dataTypeLabel: String,
    val reportingPeriod: String,
) : TypedEmailContent(),
    InitializeSubscriptionUuidLater,
    InitializeBaseUrlLater {
    override val subject = "New data for ${this.companyName} on Dataland"
    override val textTemplate = "/text/single_dataset_uploaded_engagement.ftl"
    override val htmlTemplate = "/html/single_dataset_uploaded_engagement.ftl"

    @JsonIgnore
    override lateinit var subscriptionUuid: String

    @JsonIgnore
    override lateinit var baseUrl: String
}

/**
 * A class for the MultipleDatasetsUploadedEngagement email.
 */
data class MultipleDatasetsUploadedEngagement(
    val companyId: String,
    val companyName: String,
    val frameworkData: List<FrameworkData>,
    val numberOfDays: Long?,
) : TypedEmailContent(),
    InitializeSubscriptionUuidLater,
    InitializeBaseUrlLater {
    override val subject = "New data for ${this.companyName} on Dataland"
    override val textTemplate = "/text/company_ownership_claim_dataset_uploaded.ftl"
    override val htmlTemplate = "/html/company_ownership_claim_dataset_uploaded.ftl"

    /**
     * A class that stores the information about the multiple frameworks that have been uploaded for the company.
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
 * A class for the CompanyOwnershipClaimApproved email.
 */
data class CompanyOwnershipClaimApproved(
    val companyId: String,
    val companyName: String,
    val numberOfOpenDataRequestsForCompany: Int,
) : TypedEmailContent(),
    InitializeBaseUrlLater {
    override val subject = "Your company ownership claim for ${this.companyName} is confirmed!"
    override val textTemplate = "/text/company_ownership_claim_approved.ftl"
    override val htmlTemplate = "/html/company_ownership_claim_approved.ftl"

    @JsonIgnore
    override lateinit var baseUrl: String
}

/**
 * A class for the DataRequestAnswered email.
 */
data class DataRequestAnswered(
    val companyName: String,
    val dataTypeLabel: String,
    val reportingPeriod: String,
    val creationDate: String,
    val dataRequestId: String,
    val closedInDays: Int,
) : TypedEmailContent(),
    InitializeBaseUrlLater {
    override val subject = "Your data request has been answered!"
    override val textTemplate = "/text/data_request_available.ftl"
    override val htmlTemplate = "/html/data_request_available.ftl"

    @JsonIgnore
    override lateinit var baseUrl: String
}

/**
 * A class for the DataRequestUpdated email.
 */
data class DataRequestUpdated(
    val companyName: String,
    val dataTypeLabel: String,
    val reportingPeriod: String,
    val creationDate: String,
    val dataRequestId: String,
) : TypedEmailContent(),
    InitializeBaseUrlLater {
    override val subject = "Your data request has been updated!"
    override val textTemplate = "/text/data_request_updated.ftl"
    override val htmlTemplate = "/html/data_request_updated.ftl"

    @JsonIgnore
    override lateinit var baseUrl: String
}

/**
 * A class for the DataRequestClosed email.
 */
data class DataRequestClosed(
    val companyName: String,
    val dataTypeLabel: String,
    val reportingPeriod: String,
    val creationDate: String,
    val dataRequestId: String,
    val closedInDays: Int,
) : TypedEmailContent(),
    InitializeBaseUrlLater {
    override val subject = "Your data request has been closed!"
    override val textTemplate = "/text/data_request_closed.ftl"
    override val htmlTemplate = "/html/data_request_closed.ftl"

    @JsonIgnore
    override lateinit var baseUrl: String
}

/**
 * A class for the DataRequestNonSourceable email.
 */
data class DataRequestNonSourceable(
    val companyName: String,
    val dataTypeLabel: String,
    val reportingPeriod: String,
    val creationDate: String,
    val dataRequestId: String,
    val nonSourceableComment: String?,
) : TypedEmailContent(),
    InitializeBaseUrlLater {
    override val subject = "There are no sources for your requested data available!"
    override val textTemplate = "/text/data_request_non_sourceable.ftl"
    override val htmlTemplate = "/html/data_request_non_sourceable.ftl"

    @JsonIgnore
    override lateinit var baseUrl: String
}

/**
 * A class for the KeyValueTable email. User for internal purposes.
 */
data class InternalEmailContentTable(
    override val subject: String,
    val textTitle: String,
    val htmlTitle: String,
    val table: List<Pair<String, Value>>,
) : TypedEmailContent(),
    InitializeBaseUrlLater {
    override val textTemplate = "/text/company_ownership_claim_dataset_uploaded_internal.ftl"
    override val htmlTemplate = "/html/company_ownership_claim_dataset_uploaded_internal.ftl"

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
