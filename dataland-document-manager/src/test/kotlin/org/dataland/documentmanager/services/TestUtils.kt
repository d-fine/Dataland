package org.dataland.documentmanager.services

import org.apache.pdfbox.io.IOUtils

class TestUtils {
    fun loadFileBytes(path: String): ByteArray {
        val testFileStream = javaClass.getResourceAsStream(path)
        return IOUtils.toByteArray(testFileStream)
    }
}