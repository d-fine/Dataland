package org.dataland.datalandbackend.services

import com.fasterxml.jackson.databind.ObjectMapper
import org.dataland.datalandbackend.DatalandBackend
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.mock.web.MockMultipartFile

@SpringBootTest(classes = [DatalandBackend::class])
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
class FileManagerTest(
    @Autowired val fileManager: FileManager,
    @Autowired val objectMapper: ObjectMapper
) {
    @Test
    fun `check that the upload meta data is stored persistently`() {
        val multipartFile = MockMultipartFile(
            "name.txt",
            "original.txt",
            "multipart/form-data",
            objectMapper.writeValueAsBytes("This is content.")
        )
        val uploadId = fileManager.storeExcelFiles(listOf(multipartFile)).uploadId
        assertEquals(2, fileManager.UploadHistory()[uploadId]!!.size)
    }
}
