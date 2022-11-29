package org.dataland.keycloakAdapter.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandapikeymanager.openApiClient.model.ErrorDetails
import org.dataland.datalandapikeymanager.openApiClient.model.ErrorResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.authentication.AuthenticationFailureHandler
import org.springframework.stereotype.Component
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class ApiKeyAuthenticationFailureHandler(
    @Value("\${dataland.expose-error-stack-trace-to-api:false}")
    val displayDetailedErrorMessage: Boolean
) : AuthenticationFailureHandler {

    override fun onAuthenticationFailure(
        request: HttpServletRequest?,
        response: HttpServletResponse?,
        exception: AuthenticationException?
    ) {
        val unauthorizedMessage = "Unauthorized"
        val error = ErrorDetails(
            errorType = "unauthorized",
            summary = "Unauthorized",
            message = if (displayDetailedErrorMessage)
                exception?.message ?: unauthorizedMessage
            else
                unauthorizedMessage,
            httpStatus = HttpStatus.UNAUTHORIZED.value().toBigDecimal()
        )
        response!!.contentType = "application/json"
        response.status = HttpServletResponse.SC_UNAUTHORIZED
        response.outputStream.println(
            ObjectMapper().writeValueAsString(
                ErrorResponse(
                    errors = listOf(error)
                )
            )
        )
        response.outputStream.close()
    }
}
