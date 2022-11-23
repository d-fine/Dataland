package org.dataland.datalandspringbase.controller.advice

import org.apache.commons.lang3.exception.ExceptionUtils
import org.dataland.datalandspringbase.model.ErrorDetails
import org.dataland.datalandspringbase.model.ErrorResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

/**
 * This class handles responses for all errors that have not been handled yet.
 * It implements a fallback responder that responds with internal server errors
 */
@Order
@ControllerAdvice
class UnknownErrorControllerAdvice(
    @Value("\${dataland.expose-error-stack-trace-to-api:false}")
    val trace: Boolean
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    /**
     * Handles all exceptions returning a generic Internal server error response.
     * This is intended as a fallback error handler
     */
    @ExceptionHandler(Exception::class)
    fun handleUnknownException(ex: Exception): ResponseEntity<ErrorResponse> {
        val preparedError = ErrorDetails(
            errorType = "unknown-internal-server-error",
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR,
            summary = "An internal server error occurred",
            message = "An unexpected internal server error occurred. Please contact support if this error persists",
            stackTrace = if (trace) ExceptionUtils.getStackTrace(ex) else null
        )
        logger.error("An unknown internal server error occurred: $preparedError", ex)
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ErrorResponse(errors = listOf(preparedError)))
    }
}
