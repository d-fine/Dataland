package org.dataland.datalandmessagequeueutils.messages.email

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
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
    JsonSubTypes.Type(value = KeyValueTable::class, name = "KeyValueTable"),
)
sealed class TypedEmailContent

interface InitializeSubscriptionUuidLater {
    var subscriptionUuid: String
}

interface InitializeBaseUrlLater {
    var baseUrl: String
}

data class DatasetRequestedClaimOwnership(
    val companyId: String,
    val companyName: String,
    val requesterEmail: String,
    val dataTypeLabel: String,
    val reportingPeriods: List<String>,
    val message: String?,
    val firstName: String?,
    val lastName: String?) : TypedEmailContent(), InitializeSubscriptionUuidLater, InitializeBaseUrlLater {
        @JsonIgnore
        override lateinit var subscriptionUuid: String
        @JsonIgnore
        override lateinit var baseUrl: String
    }

data class AccessToDatasetRequested(
    val companyId: String,
    val companyName: String,
    val dataTypeLabel: String,
    val reportingPeriods: List<String>,
    val message: String?,
    val requesterEmail: String?,
    val requesterFirstName: String?,
    val requesterLastName: String?,
) : TypedEmailContent(), InitializeBaseUrlLater {
    @JsonIgnore
    override lateinit var baseUrl: String
}

data class AccessToDatasetGranted(
    val companyId: String,
    val companyName: String,
    val dataType: String,
    val dataTypeLabel: String,
    val reportingPeriod: String,
    val creationDate: String,
) : TypedEmailContent(), InitializeBaseUrlLater {
    @JsonIgnore
    override lateinit var baseUrl: String
}

data class SingleDatasetUploadedEngagement(
    val companyId: String,
    val companyName: String,
    val dataTypeLabel: String,
    val reportingPeriod: String
) : TypedEmailContent(), InitializeSubscriptionUuidLater, InitializeBaseUrlLater {
    @JsonIgnore
    override lateinit var subscriptionUuid: String
    @JsonIgnore
    override lateinit var baseUrl: String
}

data class MultipleDatasetsUploadedEngagement(
    val companyId: String,
    val companyName: String,
    val frameworkData: List<FrameworkData>,
    val numberOfDays: Long?
) : TypedEmailContent(), InitializeSubscriptionUuidLater, InitializeBaseUrlLater {
    data class FrameworkData(
        val dataTypeLabel: String,
        val reportingPeriods: List<String>
    )
    @JsonIgnore
    override lateinit var subscriptionUuid: String
    @JsonIgnore
    override lateinit var baseUrl: String
}

data class CompanyOwnershipClaimApproved(
    val companyId: String,
    val companyName: String,
    val numberOfOpenDataRequestsForCompany : Int
) : TypedEmailContent(), InitializeBaseUrlLater {
    @JsonIgnore
    override lateinit var baseUrl: String
}

data class DataRequestAnswered(
    val companyName: String,
    val dataTypeLabel: String,
    val reportingPeriod: String,
    val creationDate: String,
    val dataRequestId: String,
    val closedInDays: Int,
) : TypedEmailContent(), InitializeBaseUrlLater {
    @JsonIgnore
    override lateinit var baseUrl: String
}

data class DataRequestClosed(
    val companyName: String,
    val dataTypeLabel: String,
    val reportingPeriod: String,
    val creationDate: String,
    val dataRequestId: String,
    val closedInDays: Int,
) : TypedEmailContent(), InitializeBaseUrlLater {
    @JsonIgnore
    override lateinit var baseUrl: String
}

data class KeyValueTable(
    val subject: String,
    val textTitle: String,
    val htmlTitle: String,
    val table: List<Pair<String, Value>>
) : TypedEmailContent(), InitializeBaseUrlLater {
    @JsonIgnore
    override lateinit var baseUrl: String
}

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type"
)
@JsonSubTypes(
    JsonSubTypes.Type(value = Value.Text::class, name="Text"),
    JsonSubTypes.Type(value = Value.List::class, name="List"),
)
sealed class Value {
    abstract val macro_name: String

    data class Text(
        val value: String
    ) : Value() {
        @JsonIgnore
        override val macro_name = "text_macro"
    }

    data class List(
        val values: kotlin.collections.List<Value>,
        val separator: String = ", ",
        val start: String = "",
        val end: String = ""
    ) : Value() {
        constructor(
            vararg values: Value,
            separator: String = ", ",
            start: String = "",
            end: String = ""
        ) : this(values.toList(), separator, start, end)

        @JsonIgnore
        override val macro_name = "list_macro"
    }

    data class RelativeLink(
        val href: String,
        val title: String
    ) : Value() {
        @JsonIgnore
        override val macro_name = "link_macro"
    }

    data class EmailAddressWithSubscriptionStatus(
        val emailAddress: String
    ) : Value() {
        @JsonIgnore
        override val macro_name = "email_address_with_subscription_status"

        @JsonIgnore
        var subscribed: Boolean = false // TODO mention that this variable is initialized later even if no lateinit due to kotlin restriction
    }
}
