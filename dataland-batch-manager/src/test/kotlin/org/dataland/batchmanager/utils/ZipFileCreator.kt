package org.dataland.batchmanager.utils

import java.io.ByteArrayOutputStream
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object ZipFileCreator {
    /*
     * In the specified file, create a zip entry "some.csv" containing the provided file content.
     */
    fun createZipFile(
        outPutFile: File,
        fileContent: String,
    ) {
        val zipBytes = ByteArrayOutputStream()
        val zipStream = ZipOutputStream(zipBytes)
        val buffer = fileContent.toByteArray()
        zipStream.putNextEntry(ZipEntry("some.csv"))
        zipStream.write(buffer, 0, buffer.size)
        zipStream.closeEntry()
        zipStream.close()
        outPutFile.writeBytes(zipBytes.toByteArray())
    }
}
