package org.dataland.datalandmessagequeueutils.messages.email

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

/**
 * sealed class that either represents an email, a keycloak user, the internal receivers or the internal cc receivers.
 */
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type",
)
@JsonSubTypes(
    JsonSubTypes.Type(value = EmailRecipient.EmailAddress::class, name = "EmailAddressRecipient"),
    JsonSubTypes.Type(value = EmailRecipient.UserId::class, name = "UserIdRecipient"),
    JsonSubTypes.Type(value = EmailRecipient.Internal::class, name = "InternalRecipients"),
    JsonSubTypes.Type(value = EmailRecipient.InternalCc::class, name = "InternalCcRecipients"),
)
sealed class EmailRecipient {
    /**
     * data class that represents an email recipient as email
     */
    data class EmailAddress(
        val email: String,
    ) : EmailRecipient()

    /**
     * data class that represents an email recipient as userId
     */
    data class UserId(
        val userId: String,
    ) : EmailRecipient()

    /**
     * data class that represents the internal email recipients
     */
    data object Internal : EmailRecipient()

    /**
     * data class that represents the internal cc email recipients
     */
    data object InternalCc : EmailRecipient()
}
