package org.dataland.batchmanager.service

import okhttp3.OkHttpClient
import org.apache.commons.io.FileUtils
import org.dataland.datalandbatchmanager.service.ExternalFileDownload
import org.dataland.datalandbatchmanager.service.NorthDataAccessor
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.mock
import org.mockito.Mockito.mockStatic
import org.mockito.Mockito.times
import org.mockito.Mockito.`when`
import java.io.File

class NorthDataAccessorTest {

    private val mockHttpClient: OkHttpClient = mock(OkHttpClient::class.java)
    private val externalFileDownload = ExternalFileDownload(mockHttpClient)

    private val dummyUrl = "https://dummy.com"

    @Test
    fun `test if download of full golden copy works`() {
        val mockFileUtils = mockStatic(FileUtils::class.java)
        `when`(FileUtils.copyURLToFile(any(), any())).thenAnswer { }
        NorthDataAccessor(dummyUrl, externalFileDownload).getFullGoldenCopy(File("test"))
        mockFileUtils.verify({ FileUtils.copyURLToFile(any(), any()) }, times(1))
        mockFileUtils.close()
    }
}
