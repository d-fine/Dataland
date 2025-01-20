/**
 * A ApiRetryException should be thrown if the api calls for multiple times in a row due to common Api errors
 */
class ApiRetryException(
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause)
