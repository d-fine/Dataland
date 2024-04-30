package org.dataland.datalandmessagequeueutils.messages

/**
 * Message object that is part of each internal email
 */
data class TemplateEmailMessage(
    val emailTemplateType: Type,
    val receiver: String,
    val properties: Map<String, String?>,
) {
    /**
     * This specifies the types of a template email
     */
    enum class Type {
        ClaimOwnership,
        DataRequestedAnswered,
        SuccessfullyClaimedOwnership,
    }
}
