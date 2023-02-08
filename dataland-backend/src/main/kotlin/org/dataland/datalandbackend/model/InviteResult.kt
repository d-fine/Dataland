package org.dataland.datalandbackend.model

/**
 * --- Non-API model ---
 * Class for defining the outcome of an invite
 * @param isInviteSuccessful describes if the invite was successfully processed
 * @param inviteResultMessage gives more detailed information about the outcome of the invite
 */
data class InviteResult(
    val isInviteSuccessful: Boolean,
    val inviteResultMessage: String,
)
