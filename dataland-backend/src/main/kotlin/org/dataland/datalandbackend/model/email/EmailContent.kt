package org.dataland.datalandbackend.model.email

import org.json.JSONArray

/**
 * A class to represent the subject, content and attachments of an email
 */
data class EmailContent(
    val subject: String,
    val textContent: String,
    val htmlContent: String,
    val attachmentsFilenames: List<EmailAttachment>
) {
    /**
     * A method that converts the attachmentsFilenames ot a json array comaptible with the mailjet api send request
     */
    fun attachmentsToJsonArray(): JSONArray {
        val jsonArray = JSONArray()
        attachmentsFilenames.forEach { jsonArray.put(it.toJson()) }
        return jsonArray
    }
}
