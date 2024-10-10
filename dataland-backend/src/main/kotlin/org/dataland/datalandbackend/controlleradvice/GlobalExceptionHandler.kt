package org.dataland.datalandbackend.controlleradvice

import org.dataland.datalandinternalstorage.openApiClient.model.ErrorDetails
import org.dataland.datalandinternalstorage.openApiClient.model.ErrorResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import java.math.BigDecimal

/**
 * A global exception handler that intercepts exceptions thrown by controller methods.
 *
 * This class handles specific exceptions and constructs appropriate error responses
 * to be returned to the client.
 */
@ControllerAdvice
class GlobalExceptionHandler {
    /**
     * Handles exceptions thrown when method arguments fail validation.
     *
     * @param ex The [MethodArgumentNotValidException] instance containing validation error details.
     * @return A [ResponseEntity] containing an [ErrorResponse] with validation error details.
     */
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationExceptions(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val errorDetailsList =
            ex.bindingResult.allErrors.map { error ->
                val fieldError = error as? FieldError
                val fieldName = fieldError?.field
                val defaultMessage = error.defaultMessage

                ErrorDetails(
                    httpStatus = BigDecimal(HttpStatus.BAD_REQUEST.value()),
                    metaInformation = fieldName, // You can adjust this as needed
                    errorType = "validation-error",
                    summary = "Invalid input parameter",
                    message = defaultMessage,
                )
            }
        val errorResponse =
            ErrorResponse(
                errors = errorDetailsList,
            )
        return ResponseEntity(errorResponse, HttpStatus.BAD_REQUEST)
    }
}
