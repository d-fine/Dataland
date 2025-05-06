package org.dataland.e2etests.utils

import org.dataland.datalandbackend.openApiClient.infrastructure.ClientException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.assertThrows

object ExceptionUtils {
    private fun assertClientException(
        operation: () -> Any,
        expectedMessage: String,
    ) = assertEquals(
        expectedMessage,
        assertThrows<ClientException> { operation() }.message,
    )

    fun assertAccessDeniedWrapper(operation: () -> Any) = assertClientException(operation, "Client error : 403 ")

    fun assertResourceNotFoundWrapper(operation: () -> Any) = assertClientException(operation, "Client error : 404 ")
}
