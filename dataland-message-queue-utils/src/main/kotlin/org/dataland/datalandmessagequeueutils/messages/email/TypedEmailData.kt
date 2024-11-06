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
)
sealed class TypedEmailData

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
    val dataType: String,
    val reportingPeriods: List<String>,
    val message: String?,
    val firstName: String?,
    val lastName: String?) : TypedEmailData(), InitializeSubscriptionUuidLater, InitializeBaseUrlLater {
        @JsonIgnore
        override lateinit var subscriptionUuid: String
        @JsonIgnore
        override lateinit var baseUrl: String
    }

