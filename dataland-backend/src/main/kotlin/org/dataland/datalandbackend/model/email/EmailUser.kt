package org.dataland.datalandbackend.model.email

/**
 * defines the email address and name of a email sender or receiver
 */
data class EmailUser(
    val email: String,
    val name: String
)