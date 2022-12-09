package org.dataland.datalandbackend.email

import com.mailjet.client.resource.Emailv31
import org.json.JSONArray
import org.json.JSONObject

/**
 * A class that stores the sender, receiver and content of an email
 */
data class Email(
    val sender: EmailUser,
    val receiver: EmailUser,
    val content: EmailContent
) {
    /**
     * Converts the stored data to a json object compatible with the mailjet api send request
     * @return the constructed json object
     */
    fun toJson(): JSONObject {
        return JSONObject()
            .put(
                Emailv31.Message.FROM,
                JSONObject()
                    .put("Email", sender.email)
                    .put("Name", sender.name)
            ).put(
                Emailv31.Message.TO,
                JSONArray().put(
                    JSONObject()
                        .put("Email", receiver.email)
                        .put("Name", receiver.name)
                )
            ).put(Emailv31.Message.SUBJECT, content.subject)
            .put(Emailv31.Message.TEXTPART, content.textContent)
            .put(Emailv31.Message.HTMLPART, content.htmlContent)
            .put(Emailv31.Message.ATTACHMENTS, content.attachmentsToJsonArray())
    }
}
