package org.dataland.e2etests.utils.api

import org.awaitility.Awaitility
import org.awaitility.core.ConditionFactory
import org.springframework.http.HttpStatus
import java.util.concurrent.TimeUnit
import kotlin.reflect.KProperty1

/**
 * Utility class for waiting for API responses.
 */
object ApiAwait {
    private fun isApiExceptionOfStatusCode(
        allowedCodes: Set<HttpStatus>,
        exception: Throwable,
    ): Boolean {
        val isApiError = exception.javaClass.simpleName == "ClientException" || exception.javaClass.simpleName == "ServerException"
        if (!isApiError) return false
        val statusCodeProperty = exception::class.members.first { it.name == "statusCode" } as KProperty1<Any, *>
        val statusCode = statusCodeProperty.get(exception) as Int

        return HttpStatus.valueOf(statusCode) in allowedCodes
    }

    private fun awaitBase(
        timeoutInSeconds: Long,
        ignoredHttpErrors: Set<HttpStatus>,
    ): ConditionFactory =
        Awaitility
            .given()
            .ignoreExceptionsMatching {
                isApiExceptionOfStatusCode(ignoredHttpErrors, it)
            }.atMost(timeoutInSeconds, TimeUnit.SECONDS)
            .await()

    /**
     * Waits for the supplier to execute without an allowed HTTP error being thrown.
     * @param timeoutInSeconds The maximum time to wait for the supplier to execute.
     * @param retryOnHttpErrors The set of HTTP statuses that are allowed to be thrown by the supplier.
     * @param supplier The supplier to execute.
     */
    fun waitForSuccess(
        timeoutInSeconds: Long = 5,
        retryOnHttpErrors: Set<HttpStatus> = setOf(),
        supplier: () -> Unit,
    ) {
        awaitBase(timeoutInSeconds, retryOnHttpErrors)
            .until {
                supplier()
                true
            }
    }

    /**
     * Waits for the supplier to return a value matching the condition without an allowed HTTP error being thrown.
     * @param timeoutInSeconds The maximum time to wait for the supplier to execute.
     * @param retryOnHttpErrors The set of HTTP statuses that are allowed to be thrown by the supplier.
     * @param condition The condition that the supplier's return value must match.
     * @param supplier The supplier to execute.
     */
    fun <T> waitForData(
        timeoutInSeconds: Long = 5,
        retryOnHttpErrors: Set<HttpStatus> = setOf(),
        condition: (it: T) -> Boolean = { true },
        supplier: () -> T,
    ): T =
        awaitBase(timeoutInSeconds, retryOnHttpErrors)
            .until(supplier, condition)

    /**
     * Waits for the condition to be true without an allowed HTTP error being thrown.
     * @param timeoutInSeconds The maximum time to wait for the condition to be true.
     * @param retryOnHttpErrors The set of HTTP statuses that are allowed to be thrown by the condition.
     * @param condition The condition to wait for.
     */
    fun waitForCondition(
        timeoutInSeconds: Long = 5,
        retryOnHttpErrors: Set<HttpStatus> = setOf(),
        condition: () -> Boolean,
    ) {
        awaitBase(timeoutInSeconds, retryOnHttpErrors)
            .until(condition)
    }
}
