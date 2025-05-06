package org.dataland.keycloakAdapter.utils

import org.dataland.keycloakAdapter.auth.DatalandApiKeyAuthentication
import org.dataland.keycloakAdapter.auth.DatalandAuthentication
import org.dataland.keycloakAdapter.auth.DatalandJwtAuthentication
import org.slf4j.Logger
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

fun logAccess(logger: Logger) {
    val authentication = DatalandAuthentication.fromContextOrNull()
    val authenticationType = when (authentication) {
        is DatalandJwtAuthentication -> "JWT Authentication"
        is DatalandApiKeyAuthentication -> "API Key Authentication"
        else -> "No Authentication"
    }
    val userId = authentication?.userId ?: "None"

    val attributes = RequestContextHolder.getRequestAttributes() as? ServletRequestAttributes
    val request = attributes?.request
    val url = request?.requestURL.toString()
    val method = request?.method

    logger.info("'$method'-type request to endpoint: '$url' by user: '$userId' using authentication: '$authenticationType'.")
}
