package org.dataland.batchmanager.service

import org.dataland.datalandbatchmanager.service.GleifCsvParser
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class GleifCsvParserTest {
    @Test
    fun `test if zip files can be read`() {
        val fileContent = "some;csv;content"
        val zipout = ByteArrayOutputStream()
        val zos = ZipOutputStream(zipout)
        val buffer = fileContent.toByteArray()
        zos.putNextEntry(ZipEntry("some.csv"))
        zos.write(buffer, 0, buffer.size)
        zos.closeEntry()
        zos.close()
        val zipFile = File("zip.zip")
        zipFile.writeBytes(zipout.toByteArray())
        val bufferedReader = GleifCsvParser().getCsvStreamFromZip(zipFile)
        assertEquals(bufferedReader.readLine(), fileContent)
    }
}
