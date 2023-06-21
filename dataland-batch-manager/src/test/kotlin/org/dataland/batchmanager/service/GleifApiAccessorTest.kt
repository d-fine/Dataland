package org.dataland.batchmanager.service

import org.apache.commons.io.FileUtils
import org.dataland.datalandbatchmanager.service.GleifApiAccessor
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.mockStatic
import org.mockito.Mockito.`when`
import java.io.File
import java.io.FileNotFoundException
import java.net.SocketException

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GleifApiAccessorTest {
    @BeforeAll
    fun setup() {
        mockStatic(FileUtils::class.java)
    }

    @Test
    fun `test download failure`() {
        `when`(FileUtils.copyURLToFile(any(), any())).thenThrow(SocketException("Test Exception"))
        assertThrows<FileNotFoundException> {
            GleifApiAccessor().getFullGoldenCopy(File("test"))
        }
    }

    @Test
    fun `test download full golden copy`() {
        `when`(FileUtils.copyURLToFile(any(), any())).thenAnswer { }
        GleifApiAccessor().getFullGoldenCopy(File("test"))
    }

    @Test
    fun `test download delta file`() {
        `when`(FileUtils.copyURLToFile(any(), any())).thenAnswer { }
        GleifApiAccessor().getFullGoldenCopy(File("test"))
    }
}