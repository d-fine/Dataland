package org.dataland.datalandmessagequeueutils.messages

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

/**
 * Message object that is part of each internal email
 */
data class TemplateEmailMessage(
    val emailTemplateType: Type,
    val receiver: EmailRecipient,
    val properties: Map<String, String?>,
) {
    /**
     * This specifies the types of a template email
     */
    enum class Type {
        ClaimOwnership,
        DataRequestedAnswered,
        DataRequestClosed,
        SuccessfullyClaimedOwnership,
        SingleNotification,
        SummaryNotification,
        DataAccessRequested,
        DataAccessGranted,
    }

    /**
     * sealed class that either represents an email or a keycloak user
     */
    @JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type",
    )
    @JsonSubTypes(
        JsonSubTypes.Type(value = EmailAddressEmailRecipient::class, name = "address"),
        JsonSubTypes.Type(value = UserIdEmailRecipient::class, name = "user"),
    )
    sealed class EmailRecipient

    /**
     * data class that represents an email recipient as email
     */
    data class EmailAddressEmailRecipient(val email: String) : EmailRecipient()

    /**
     * data class that represents an email recipient as userId
     */
    data class UserIdEmailRecipient(val userId: String) : EmailRecipient()
}
