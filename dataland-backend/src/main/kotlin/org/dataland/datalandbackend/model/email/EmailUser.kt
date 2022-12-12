package org.dataland.datalandbackend.model.email

import org.json.JSONObject

/**
 * defines the email address and name of a email sender or receiver
 */
data class EmailUser(
    val email: String,
    val name: String
) {
    /**
     * Converts the email user to a json object compatible with the mailjet api send request
     * @return the constructed json object
     */
    fun toJson(): JSONObject {
        return JSONObject()
            .put("Email", email)
            .put("Name", name)
    }
}
