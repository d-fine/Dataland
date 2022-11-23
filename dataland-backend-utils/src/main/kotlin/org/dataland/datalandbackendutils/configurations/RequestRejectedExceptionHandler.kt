package org.dataland.datalandbackendutils.configurations

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.commons.lang3.exception.ExceptionUtils
import org.dataland.datalandbackendutils.model.ErrorDetails
import org.dataland.datalandbackendutils.model.ErrorResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.security.web.firewall.RequestRejectedException
import org.springframework.security.web.firewall.RequestRejectedHandler
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * Handles RequestRejectedException errors. Because these errors are handeled on the servlet level,
 * they do not pass through the default error-handling chain. These occur when someone
 * e.g. uses a non-allowed HTTP methods or tries some other funny stuff (These are Spring Security errors)
 */
@Configuration
class RequestRejectedExceptionHandler(
    @Value("\${dataland.expose-error-stack-trace-to-api:false}")
    private val trace: Boolean,
    @Autowired
    private val objectMapper: ObjectMapper
) : RequestRejectedHandler {
    override fun handle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        ex: RequestRejectedException
    ) {
        val responseObject = ErrorResponse(
            errors = listOf(
                ErrorDetails(
                    errorType = "request-rejected",
                    summary = "Your request has been rejected",
                    message = ex.message ?: "Your request has been rejected by our internal firewall",
                    httpStatus = HttpStatus.BAD_REQUEST,
                    stackTrace = if (trace) ExceptionUtils.getStackTrace(ex) else null
                ),
            )
        )
        val responseString = objectMapper.writeValueAsString(responseObject)
        val printWriter = response.writer
        response.contentType = "application/json"
        response.characterEncoding = "UTF-8"
        response.status = HttpStatus.BAD_REQUEST.value()
        printWriter.print(responseString)
        printWriter.flush()
    }
}
