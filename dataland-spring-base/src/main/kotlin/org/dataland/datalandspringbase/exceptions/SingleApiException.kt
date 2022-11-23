package org.dataland.datalandspringbase.exceptions

import org.dataland.datalandspringbase.model.ErrorDetails

/**
 * This abstract class serves as a basis for Dataland-Specific ApiExceptions that only return a single error
 */
abstract class SingleApiException : Exception {

    constructor() : super()
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable?) : super(message, cause)
    constructor(cause: Throwable?) : super(cause)

    /**
     * Returns the ErrorDetails APIModel that describes this error
     */
    abstract fun getErrorResponse(): ErrorDetails
}
