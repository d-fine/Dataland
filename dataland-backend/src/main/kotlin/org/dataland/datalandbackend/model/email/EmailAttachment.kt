package org.dataland.datalandbackend.model.email

import org.dataland.datalandbackendutils.utils.EncodingUtils
import org.json.JSONObject

/**
 * A class for representing an email attachment
 */
data class EmailAttachment(
    val filename: String,
    val content: ByteArray
) {
    /**
     * Converts the email attachment to a json object compatible with the mailjet api send request
     * @return the constructed json object
     */
    fun toJson(): JSONObject {
        val fileContentBase64Encoded = EncodingUtils.encodeToBase64(content)
        return JSONObject()
            .put("ContentType", "text/plain")
            .put("Filename", filename)
            .put("Base64Content", fileContentBase64Encoded)
    }
}