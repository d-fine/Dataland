package org.dataland.batchmanager.service

import org.dataland.batchmanager.utils.ZipFileCreator
import org.dataland.datalandbatchmanager.service.CsvParser
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.File

class CsvParserTest {
    @Test
    fun `test if zip files can be read`() {
        val fileContent = "some;csv;content"
        val zipFile = File("zip.zip")

        ZipFileCreator.createZipFile(zipFile, fileContent)

        val bufferedReader = CsvParser().getCsvStreamFromZip(zipFile)
        assertEquals(bufferedReader.readLine(), fileContent)
    }
}
