package org.dataland.batchmanager.service

import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody
import org.apache.commons.io.FileUtils
import org.dataland.datalandbatchmanager.service.GleifApiAccessor
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentMatchers.any
import org.mockito.MockedStatic
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.mock
import org.mockito.Mockito.mockStatic
import org.mockito.Mockito.reset
import org.mockito.Mockito.spy
import org.mockito.Mockito.times
import org.mockito.Mockito.`when`
import java.io.File
import java.io.FileNotFoundException
import java.net.SocketException

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GleifApiAccessorTest {

    lateinit var mockFileUtils: MockedStatic<FileUtils>
    val mockHttpClient: OkHttpClient = mock(OkHttpClient::class.java)

    val dummyUrl = "https://dummy.com"

    @BeforeAll
    fun setup() {
        mockFileUtils = mockStatic(FileUtils::class.java)
    }

    @BeforeEach
    fun setupTest() {
        reset(FileUtils::class.java)
        reset(mockHttpClient)
    }

    @Test
    fun `test download failure if there are socket exceptions`() {
        `when`(FileUtils.copyURLToFile(any(), any())).thenThrow(SocketException("Test Exception"))
        assertThrows<FileNotFoundException> {
            GleifApiAccessor(mockHttpClient, dummyUrl, dummyUrl).getFullGoldenCopy(File("test"))
        }
        mockFileUtils.verify({ FileUtils.copyURLToFile(any(), any()) }, times(3))
    }

    @Test
    fun `test if download full golden copy works fine under the right conditions`() {
        `when`(FileUtils.copyURLToFile(any(), any())).thenAnswer { }
        GleifApiAccessor(mockHttpClient, dummyUrl, dummyUrl).getFullGoldenCopy(File("test"))
        mockFileUtils.verify({ FileUtils.copyURLToFile(any(), any()) }, times(1))
    }

    @Test
    fun `test if download delta file works fine under the right conditions`() {
        `when`(FileUtils.copyURLToFile(any(), any())).thenAnswer { }
        GleifApiAccessor(mockHttpClient, dummyUrl, dummyUrl).getLastMonthGoldenCopyDelta(File("test"))
        mockFileUtils.verify({ FileUtils.copyURLToFile(any(), any()) }, times(1))
    }

    @Test
    fun `test for LEI ISIN mapping if unzip works and csv emerges`() {
        val providedTestFile = File(javaClass.getResource("/testApiAccessor.zip")!!.path)
        val gleifApiAccessorMock = spy(GleifApiAccessor(mockHttpClient, dummyUrl, dummyUrl))

        val mockResponseBody = mock(ResponseBody::class.java)
        doReturn(providedTestFile.readBytes()).`when`(mockResponseBody).bytes()
        val mockResponse = mock(Response::class.java)
        doReturn(mockResponseBody).`when`(mockResponse).body
        val mockCall = mock(Call::class.java)
        doReturn(mockResponse).`when`(mockCall).execute()
        `when`(mockHttpClient.newCall(any() ?: Request.Builder().url(dummyUrl).build())).thenReturn(mockCall)

        val tempCsvFile = File.createTempFile("gleif_mapping_update", ".csv")
        gleifApiAccessorMock.getFullIsinMappingFile(tempCsvFile)
        Assertions.assertEquals("1111,2222", tempCsvFile.readText())
    }
}
