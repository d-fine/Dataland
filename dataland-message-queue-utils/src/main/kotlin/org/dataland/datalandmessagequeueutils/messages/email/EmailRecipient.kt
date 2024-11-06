package org.dataland.datalandmessagequeueutils.messages.email

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo

/**
 * sealed class that either represents an email or a keycloak user
 */
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type",
)
@JsonSubTypes(
    JsonSubTypes.Type(value = EmailAddressRecipient::class, name = "EmailAddressRecipient"),
    JsonSubTypes.Type(value = UserIdRecipient::class, name = "UserIdRecipient"),
    JsonSubTypes.Type(value = InternalRecipients::class, name = "InternalRecipients"),
    JsonSubTypes.Type(value = InternalRecipients::class, name = "InternalCcRecipients"),
)
sealed class EmailRecipient

/**
 * data class that represents an email recipient as email
 */
data class EmailAddressRecipient(
    val email: String,
) : EmailRecipient()

/**
 * data class that represents an email recipient as userId
 */
data class UserIdRecipient(
    val userId: String,
) : EmailRecipient()

/**
 * data class that represents the internal email recipients
 */
data object InternalRecipients : EmailRecipient()

/**
 * data class that represents the internal cc email recipients
 */
data object InternalCcRecipients : EmailRecipient()

