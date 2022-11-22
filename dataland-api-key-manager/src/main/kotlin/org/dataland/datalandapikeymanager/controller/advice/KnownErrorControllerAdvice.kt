package org.dataland.datalandapikeymanager.controller.advice

import org.apache.commons.lang3.exception.ExceptionUtils
import org.dataland.datalandapikeymanager.exceptions.SingleApiException
import org.dataland.datalandapikeymanager.model.ErrorDetails
import org.dataland.datalandapikeymanager.model.ErrorResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.servlet.NoHandlerFoundException

/**
 * This class contains error handlers for commonly thrown errors
 */
@Order(1)
@ControllerAdvice
class KnownErrorControllerAdvice(
    @Value("\${dataland.expose-error-stack-trace-to-api:false}")
    private val trace: Boolean
) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private fun prepareResponse(error: ErrorDetails, exception: Exception): ResponseEntity<ErrorResponse> {
        val returnedError = if (trace) error.copy(stackTrace = ExceptionUtils.getStackTrace(exception)) else error
        return ResponseEntity.status(error.httpStatus).body(
            ErrorResponse(
                errors = listOf(returnedError)
            )
        )
    }

    /**
     * Handles HttpMessageNotReadbleException errors. These occur i.e. when the request body cannot be parsed
     */
    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadableException(ex: HttpMessageNotReadableException): ResponseEntity<ErrorResponse> {
        return prepareResponse(
            ErrorDetails(
                errorType = "message-not-readable",
                summary = "Message not readable",
                message = ex.message ?: "Message not readable",
                httpStatus = HttpStatus.BAD_REQUEST
            ),
            ex
        )
    }

    /**
     * Handles AccessDeniedException errors. These occur i.e. if the user does not have permissions to perform an action
     */
    @ExceptionHandler(org.springframework.security.access.AccessDeniedException::class)
    fun handleAccessDeniedException(
        ex: org.springframework.security.access.AccessDeniedException
    ): ResponseEntity<ErrorResponse> {
        return prepareResponse(
            ErrorDetails(
                errorType = "access-denied",
                summary = "Access Denied",
                message = "Access to this resource has been denied. " +
                    "Please contact support if you believe this to be an error",
                httpStatus = HttpStatus.FORBIDDEN
            ),
            ex
        )
    }

    /**
     * Handles NoHandlerFoundException errors (aka 404-errors)
     */
    @ExceptionHandler(NoHandlerFoundException::class)
    fun handleNoHandlerFoundException(ex: NoHandlerFoundException): ResponseEntity<ErrorResponse> {
        return prepareResponse(
            ErrorDetails(
                errorType = "route-not-found",
                summary = "Route not found",
                message = "The requested route ${ex.requestURL} could not be located",
                httpStatus = HttpStatus.NOT_FOUND
            ),
            ex
        )
    }

    /**
     * Handles HttpRequestMethodNotSupportedException errors. These occur whenever someone calls an endpoint
     * with a non-implemented HTTP-Method
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    fun handleMethodNotSupportException(ex: HttpRequestMethodNotSupportedException): ResponseEntity<ErrorResponse> {
        return prepareResponse(
            ErrorDetails(
                errorType = "method-not-allowed",
                summary = "Method ${ex.method} not allowed.",
                message = "The HTTP-Method ${ex.method} is not allowed. Please refer to the API documentation " +
                    "for a list of supported HTTP methods",
                httpStatus = HttpStatus.METHOD_NOT_ALLOWED
            ),
            ex
        )
    }

    /**
     * Handles SingleApiException errors. These occur whenever custom dataland-exceptions are thrown
     */
    @ExceptionHandler(SingleApiException::class)
    fun handleApiException(ex: SingleApiException): ResponseEntity<ErrorResponse> {
        val errorResponse = ex.getErrorResponse()
        if (errorResponse.httpStatus.is5xxServerError) {
            logger.error("A server-side error occurred: $errorResponse", ex)
        }
        return prepareResponse(ex.getErrorResponse(), ex)
    }
}
