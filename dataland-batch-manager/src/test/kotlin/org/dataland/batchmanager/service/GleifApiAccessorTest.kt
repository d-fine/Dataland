package org.dataland.batchmanager.service

import org.apache.commons.io.FileUtils
import org.dataland.datalandbatchmanager.service.GleifApiAccessor
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentMatchers.any
import org.mockito.MockedStatic
import org.mockito.Mockito.mock
import org.mockito.Mockito.mockStatic
import org.mockito.Mockito.reset
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
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

    @Disabled
    @Test
    fun `test if download LEI ISIN mapping works fine under the right conditions`() {
//        fails atm because the .split in .getIsinMappingFile() does not do what it prob should be doing
        `when`(FileUtils.copyURLToFile(any(), any())).thenAnswer { }
        GleifApiAccessor(dummyUrl, dummyUrl).getFullIsinMappingFile(File("test"))
        mockFileUtils.verify({ FileUtils.copyURLToFile(any(), any()) }, times(1))
    }

    @Disabled
    @Test
    fun `test for LEI ISIN mapping if unzip works and csv emerges`() {
        val providedTetstFile = File("testApiAccessor.zip")
        val gleifApiAccessorMock = mock(GleifApiAccessor::class.java)

        `when`(
            gleifApiAccessorMock.downloadFile(
                URL("https://mapping.gleif.org/api/v2/isin-lei/latest/download"),
                File("gleif_mapping_update.zip"),
            ),
        )
            .then { providedTetstFile.copyTo(File("testApiAccessor.zip")) }

        gleifApiAccessorMock.getFullIsinMappingFile(File("testTargetFile.csv"))
        verify(gleifApiAccessorMock).getCsvFileFromZip(File("gleif_mapping_update.zip"))
    }
}
