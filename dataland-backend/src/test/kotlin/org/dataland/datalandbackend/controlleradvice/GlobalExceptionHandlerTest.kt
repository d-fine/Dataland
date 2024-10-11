package org.dataland.datalandbackend.controlleradvice

import org.dataland.datalandbackend.DatalandBackend
import org.dataland.datalandinternalstorage.openApiClient.model.ErrorResponse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.MethodParameter
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.BindingResult
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException

@SpringBootTest(classes = [DatalandBackend::class], properties = ["spring.profiles.active=nodb"])
class GlobalExceptionHandlerTest {
    @Autowired
    lateinit var globalExceptionHandler: GlobalExceptionHandler

    @Test
    fun `handle MethodArgumentNotValidException should return proper error response`() {
        val bindingResult: BindingResult = Mockito.mock(BindingResult::class.java)
        val fieldError = FieldError("objectName", "fieldName", "Invalid value")
        Mockito.`when`(bindingResult.allErrors).thenReturn(listOf(fieldError))

        val methodParameter: MethodParameter = Mockito.mock(MethodParameter::class.java)

        val exception = MethodArgumentNotValidException(methodParameter, bindingResult)

        val response: ResponseEntity<ErrorResponse> = globalExceptionHandler.handleValidationExceptions(exception)

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)

        assertTrue(response.body!!.errors!!.isNotEmpty())

        assertEquals("Invalid input parameter", response.body!!.errors!![0].summary)
    }
}
