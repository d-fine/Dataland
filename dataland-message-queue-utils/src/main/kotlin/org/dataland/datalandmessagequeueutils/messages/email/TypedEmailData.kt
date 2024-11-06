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
    JsonSubTypes.Type(value = MultipleDatasetsUploadedEngagement::class, name = "MultipleDatasetsUploadedEngagement"),
    JsonSubTypes.Type(value = SingleDatasetUploadedEngagement::class, name = "SingleDatasetUploadedEngagement"),
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

data class AccessToDatasetRequested(
    val companyId: String,
    val companyName: String,
    val dataType: String,
    val reportingPeriods: List<String>,
    val message: String?,
    val requesterEmail: String?,
    val requesterFirstName: String?,
    val requesterLastName: String?,
) : TypedEmailData(), InitializeBaseUrlLater {
    @JsonIgnore
    override lateinit var baseUrl: String
}

data class SingleDatasetUploadedEngagement(
    val companyId: String,
    val companyName: String,
    val dataType: String,
    val reportingPeriod: String
) : TypedEmailData(), InitializeSubscriptionUuidLater, InitializeBaseUrlLater {
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
) : TypedEmailData(), InitializeSubscriptionUuidLater, InitializeBaseUrlLater {
    data class FrameworkData(
        val dataType: String,
        val reportingPeriods: List<String>
    )
    @JsonIgnore
    override lateinit var subscriptionUuid: String
    @JsonIgnore
    override lateinit var baseUrl: String
}