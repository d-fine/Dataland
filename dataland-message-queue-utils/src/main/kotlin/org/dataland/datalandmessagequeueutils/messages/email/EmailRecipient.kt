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
    JsonSubTypes.Type(value = EmailAddressRecipient::class, name = "address"),
    JsonSubTypes.Type(value = UserIdRecipient::class, name = "user"),
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

