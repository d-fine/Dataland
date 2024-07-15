package org.dataland.batchmanager.service

import org.apache.commons.io.FileUtils
import org.dataland.datalandbatchmanager.service.NorthDataAccessor
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.mockStatic
import org.mockito.Mockito.times
import org.mockito.Mockito.`when`
import java.io.File

class NorthDataAccessorTest {

    private val dummyUrl = "https://dummy.com"

    @Test
    fun `test if download of full golden copy works`() {
        val mockFileUtils = mockStatic(FileUtils::class.java)
        `when`(FileUtils.copyURLToFile(any(), any())).thenAnswer { }
        NorthDataAccessor(dummyUrl).getFullGoldenCopy(File("test"))
        mockFileUtils.verify({ FileUtils.copyURLToFile(any(), any()) }, times(0))
        mockFileUtils.close()
    }
}
