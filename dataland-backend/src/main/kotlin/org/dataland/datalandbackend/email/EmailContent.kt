package org.dataland.datalandbackend.email

import org.dataland.datalandbackendutils.utils.EncodingUtils
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.nio.file.Paths

/**
 * A class to represent the subject, content and attachments of an email
 */
data class EmailContent(
    val subject: String,
    val textContent: String,
    val htmlContent: String,
    val attachmentsFilenames: List<String>
) {
    /**
     * A method that converts the attachmentsFilenames ot a json array comaptible with the mailjet api send request
     */
    fun attachmentsToJsonArray(): JSONArray {
        val jsonArray = JSONArray()
        attachmentsFilenames.forEach { jsonArray.put(fileToAttachment(it)) }
        return jsonArray
    }

    private fun fileToAttachment(filename: String): JSONObject {
        val fileContent = File(filename).readBytes()
        val fileContentBase64Encoded = EncodingUtils.encodeToBase64(fileContent)
        return JSONObject()
            .put("ContentType", "text/plain")
            .put("Filename", Paths.get(filename).fileName.toString())
            .put("Base64Content", fileContentBase64Encoded)
    }
}
