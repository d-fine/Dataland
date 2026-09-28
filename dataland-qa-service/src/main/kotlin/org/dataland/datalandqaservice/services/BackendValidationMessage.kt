package org.dataland.datalandqaservice.org.dataland.datalandqaservice.services

import org.dataland.datalandbackend.openApiClient.infrastructure.ClientError
import org.dataland.datalandbackend.openApiClient.infrastructure.ClientException
import org.dataland.datalandbackendutils.utils.JsonUtils

/** Returns the backend's validation detail, or the local description if the response cannot be read. */
internal fun ClientException.validationMessageOr(fallback: String): String {
    val body = (response as? ClientError<*>)?.body as? String ?: return fallback
    return try {
        JsonUtils.defaultObjectMapper
            .readTree(body)
            .path("errors")
            .path(0)
            .path("message")
            .takeIf { it.isTextual }
            ?.asText()
            ?.takeIf { it.isNotBlank() } ?: fallback
    } catch (_: Exception) {
        fallback
    }
}
