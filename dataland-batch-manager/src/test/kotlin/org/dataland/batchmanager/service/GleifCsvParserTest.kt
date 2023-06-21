package org.dataland.batchmanager.service

import org.dataland.datalandbatchmanager.service.GleifCsvParser
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class GleifCsvParserTest {
    @Test
    fun `test if zip files can be read`() {
        val fileContent = "some;csv;content"
        val zipBytes = ByteArrayOutputStream()
        val zipStream = ZipOutputStream(zipBytes)
        val buffer = fileContent.toByteArray()
        zipStream.putNextEntry(ZipEntry("some.csv"))
        zipStream.write(buffer, 0, buffer.size)
        zipStream.closeEntry()
        zipStream.close()
        val zipFile = File("zip.zip")
        zipFile.writeBytes(zipBytes.toByteArray())
        val bufferedReader = GleifCsvParser().getCsvStreamFromZip(zipFile)
        assertEquals(bufferedReader.readLine(), fileContent)
    }
}
