package org.dataland.batchmanager.service

import org.apache.commons.io.FileUtils
import org.dataland.datalandbatchmanager.service.GleifApiAccessor
import org.junit.jupiter.api.*
import org.mockito.ArgumentMatchers.any
import org.mockito.MockedStatic
import org.mockito.Mockito.*
import java.io.File
import java.io.FileNotFoundException
import java.net.SocketException
import java.net.URL

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GleifApiAccessorTest {

    lateinit var mockFileUtils: MockedStatic<FileUtils>

    val dummyUrl = "https://dummy.com"

    @BeforeAll
    fun setup() {
        mockFileUtils = mockStatic(FileUtils::class.java)
    }

    @BeforeEach
    fun setupTest() {
        reset(FileUtils::class.java)
    }

    @Test
    fun `test download failure if there are socket exceptions`() {
        `when`(FileUtils.copyURLToFile(any(), any())).thenThrow(SocketException("Test Exception"))
        assertThrows<FileNotFoundException> {
            GleifApiAccessor(dummyUrl, dummyUrl).getFullGoldenCopy(File("test"))
        }
        mockFileUtils.verify({ FileUtils.copyURLToFile(any(), any()) }, times(3))
    }

    @Test
    fun `test if download full golden copy works fine under the right conditions`() {
        `when`(FileUtils.copyURLToFile(any(), any())).thenAnswer { }
        GleifApiAccessor(dummyUrl, dummyUrl).getFullGoldenCopy(File("test"))
        mockFileUtils.verify({ FileUtils.copyURLToFile(any(), any()) }, times(1))
    }

    @Test
    fun `test if download delta file works fine under the right conditions`() {
        `when`(FileUtils.copyURLToFile(any(), any())).thenAnswer { }
        GleifApiAccessor(dummyUrl, dummyUrl).getLastMonthGoldenCopyDelta(File("test"))
        mockFileUtils.verify({ FileUtils.copyURLToFile(any(), any()) }, times(1))
    }

    @Test
    fun `test for LEI ISIN mapping if unzip works and csv emerges`() {
        val providedTestFile = File(javaClass.getResource("/testApiAccessor.zip")!!.path)
        val zipFile = File("gleif_mapping_update.zip")
        val url = URL("https://mapping.gleif.org/api/v2/isin-lei/latest/download")
        val gleifApiAccessorMock = spy(GleifApiAccessor(dummyUrl, dummyUrl))

        `when`(
            gleifApiAccessorMock.downloadFile(
                any()?:url,
                any()?:zipFile,
                ),
            )
                .then { providedTestFile.copyTo(it.arguments[1] as File, true) }

        val tempCsvFile = File.createTempFile("gleif_mapping_update", ".csv")
        gleifApiAccessorMock.getFullIsinMappingFile(tempCsvFile)
        Assertions.assertEquals("1111,2222", tempCsvFile.readText())
    }
}
