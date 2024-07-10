package org.dataland.e2etests.utils

import org.dataland.datalandbackend.openApiClient.infrastructure.ClientException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.assertThrows

object ExceptionUtils {

    fun assertAccessDeniedWrapper(
        operation: () -> Any,
    ) {
        val expectedAccessDeniedClientException = assertThrows<ClientException> {
            operation()
        }
        assertEquals("Client error : 403 ", expectedAccessDeniedClientException.message)
    }
}
