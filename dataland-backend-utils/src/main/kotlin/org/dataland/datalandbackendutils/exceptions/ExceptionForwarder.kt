package org.dataland.datalandbackendutils.exceptions

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component

/**
 * When a user-request to service A leads to a call from service A to service B and this call ends up with an
 * exception, the user will normally receive an internal-server-error response.
 * We avoid that by making service A able to handle such an exception by throwing a custom exception which is known
 * to the Dataland ExceptionHandler. This leads to a proper response.
 * This class contains logic to do just that.
 */
@Component
class ExceptionForwarder {
    /**
     * Catches a bad-request-client-exception due to a too short search string and throws a custom exception which is
     * known to the ExceptionHandler.
     * @param response the response in the inter-microservice-request that has a client error
     * @param statusCode the status code of the client error
     * @param throwable the client error itself
     */
    fun catchSearchStringTooShortClientException(
        response: String,
        statusCode: Int,
        throwable: Throwable,
    ) {
        if (statusCode == HttpStatus.BAD_REQUEST.value() && response.contains(SEARCHSTRING_TOO_SHORT_VALIDATION_MESSAGE)) {
            throw InvalidInputApiException(
                summary = "Failed to retrieve companies by search string.",
                message = "$SEARCHSTRING_TOO_SHORT_VALIDATION_MESSAGE: $SEARCHSTRING_TOO_SHORT_THRESHOLD",
                cause = throwable,
            )
        }
    }

    /**
     * Catches a bad-request-client-exception due to a validation error of a data point and throws a custom exception
     * which is known to the ExceptionHandler.
     * @param response the response in the inter-microservice-request that has a client error
     * @param statusCode the status code of the client error
     * @param throwable the client error itself
     */
    fun catchDataPointValidationClassNotFoundClientException(
        response: String,
        statusCode: Int,
        throwable: Throwable,
    ) {
        if (statusCode == HttpStatus.NOT_FOUND.value() && response.contains(DATAPOINT_VALIDATION_CLASS_NOT_FOUND_MESSAGE)) {
            throw InvalidInputApiException(
                summary = "Failed to retrieve validation class for data point.",
                message = DATAPOINT_VALIDATION_CLASS_NOT_FOUND_MESSAGE,
                cause = throwable,
            )
        }
    }
}
