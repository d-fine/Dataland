package org.dataland.datalandbackendutils.services.utils

import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.dataland.datalandbackendutils.validator.CompanyExistsValidator
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.io.IOException

class CompanyExistsValidatorTest {
    private val backendBaseUrl = "http://localhost/api"
    private val mockOkHttpClient = mock<OkHttpClient>()
    private val mockCall = mock<Call>()
    private val mockResponse = mock<Response>()
    private lateinit var validator: CompanyExistsValidator

    @BeforeEach
    fun setUp() {
        validator = CompanyExistsValidator(backendBaseUrl, mockOkHttpClient)
    }

    @Test
    fun `isValid returns true if companyId is null`() {
        assertTrue(validator.isValid(null, null))
    }

    @Test
    fun `isValid returns true if response is successful`() {
        whenever(mockOkHttpClient.newCall(any())).thenReturn(mockCall)
        whenever(mockCall.execute()).thenReturn(mockResponse)
        whenever(mockResponse.isSuccessful).thenReturn(true)

        assertTrue(validator.isValid("abc123", null))
    }

    @Test
    fun `isValid returns false if response is not successful`() {
        whenever(mockOkHttpClient.newCall(any())).thenReturn(mockCall)
        whenever(mockCall.execute()).thenReturn(mockResponse)
        whenever(mockResponse.isSuccessful).thenReturn(false)
        whenever(mockResponse.code).thenReturn(404)

        assertFalse(validator.isValid("abc123", null))
    }

    @Test
    fun `isValid returns false on IOException`() {
        whenever(mockOkHttpClient.newCall(any())).thenReturn(mockCall)
        whenever(mockCall.execute()).thenThrow(IOException("Network error"))

        assertFalse(validator.isValid("abc123", null))
    }

    @Test
    fun `callCompanyDataApiAndCheckCompanyId builds correct url`() {
        val captor = argumentCaptor<Request>()
        whenever(mockOkHttpClient.newCall(any())).thenReturn(mockCall)
        whenever(mockCall.execute()).thenReturn(mockResponse)
        whenever(mockResponse.isSuccessful).thenReturn(true)

        validator.isValid("myCompanyId", null)

        verify(mockOkHttpClient).newCall(captor.capture())
        assertEquals("$backendBaseUrl/companies/myCompanyId", captor.firstValue.url.toString())
        assertEquals("HEAD", captor.firstValue.method)
    }
}
