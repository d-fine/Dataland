package org.dataland.datalandmessagequeueutils.messages

/**
 * Message object that is part of each internal email
 */
data class InternalEmailMessage(
    val subject: String,
    val textTitle: String,
    val htmlTitle: String,
    val properties: Map<String, String?>,
)
