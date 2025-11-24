package org.dataland.datalandbackendutils.services.utils

import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.dataland.datalandbackendutils.validator.CompanyExistsValidator
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class CompanyExistsValidatorTest {
    private val backendBaseUrl = "http://localhost/api"
    private val mockOkHttpClient = mock<OkHttpClient>()
    private val mockCall = mock<Call>()
    private val mockResponse = mock<Response>()
    private lateinit var validator: CompanyExistsValidator
    private val validUuid = "00000000-0000-0000-0000-000000000000"

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
    fun `callCompanyDataApiAndCheckCompanyId builds correct url`() {
        val captor = argumentCaptor<Request>()
        whenever(mockOkHttpClient.newCall(any())).thenReturn(mockCall)
        whenever(mockCall.execute()).thenReturn(mockResponse)
        whenever(mockResponse.isSuccessful).thenReturn(true)

        validator.isValid(validUuid, null)

        verify(mockOkHttpClient).newCall(captor.capture())
        assertEquals("$backendBaseUrl/companies/$validUuid", captor.firstValue.url.toString())
        assertEquals("HEAD", captor.firstValue.method)
    }
}
