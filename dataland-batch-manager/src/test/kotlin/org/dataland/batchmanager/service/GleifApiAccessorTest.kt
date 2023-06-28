package org.dataland.batchmanager.service

import org.apache.commons.io.FileUtils
import org.dataland.datalandbatchmanager.service.GleifApiAccessor
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentMatchers.any
import org.mockito.MockedStatic
import org.mockito.Mockito.mockStatic
import org.mockito.Mockito.reset
import org.mockito.Mockito.times
import org.mockito.Mockito.`when`
import java.io.File
import java.io.FileNotFoundException
import java.net.SocketException

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GleifApiAccessorTest {

    lateinit var mockFileUtils: MockedStatic<FileUtils>

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
            GleifApiAccessor("dummy").getFullGoldenCopy(File("test"))
        }
        mockFileUtils.verify({ FileUtils.copyURLToFile(any(), any()) }, times(3))
    }

    @Test
    fun `test if download full golden copy works fine under the right conditions`() {
        `when`(FileUtils.copyURLToFile(any(), any())).thenAnswer { }
        GleifApiAccessor("dummy").getFullGoldenCopy(File("test"))
        mockFileUtils.verify({ FileUtils.copyURLToFile(any(), any()) }, times(1))
    }

    @Test
    fun `test if download delta file works fine under the right conditions`() {
        `when`(FileUtils.copyURLToFile(any(), any())).thenAnswer { }
        GleifApiAccessor("dummy").getLastMonthGoldenCopyDelta(File("test"))
        mockFileUtils.verify({ FileUtils.copyURLToFile(any(), any()) }, times(1))
    }
}
