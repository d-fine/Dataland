package org.dataland.documentmanager.services

import org.apache.pdfbox.io.IOUtils

class TestUtils {
    fun loadFileBytes(path: String): ByteArray {
        val testFileStream = javaClass.getResourceAsStream(path)
        return IOUtils.toByteArray(testFileStream)
    }
    fun isPDF(byteArray: ByteArray): Boolean {
        val pdfSignature = byteArrayOf(0x25, 0x50, 0x44, 0x46)
        return byteArray.sliceArray(0 until pdfSignature.size)
            .contentEquals(pdfSignature)
    }
    fun isNotEmptyPDF(byteArray: ByteArray): Boolean {
        return byteArray.size > 4
    }
}
