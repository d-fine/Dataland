package org.dataland.batchmanager.service

import org.dataland.batchmanager.utils.ZipFileCreator
import org.dataland.datalandbatchmanager.service.GleifCsvParser
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.File

class GleifCsvParserTest {
    @Test
    fun `test if zip files can be read`() {
        val fileContent = "some;csv;content"
        val zipFile = File("zip.zip")

        ZipFileCreator.createZipFile(zipFile, fileContent)

        val bufferedReader = GleifCsvParser().getCsvStreamFromZip(zipFile)
        assertEquals(bufferedReader.readLine(), fileContent)
    }
}
