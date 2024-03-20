package org.dataland.documentmanager.services

import org.apache.pdfbox.io.IOUtils

class TestUtils {
    fun loadFileBytes(path: String): ByteArray {
        val testFileStream = javaClass.getResourceAsStream(path)
        return IOUtils.toByteArray(testFileStream)
    }
    fun isPdf(byteArray: ByteArray): Boolean {
        val pdfSignature = byteArrayOf(0x25, 0x50, 0x44, 0x46)
        return byteArray.size >= pdfSignature.size && byteArray.sliceArray(0 until pdfSignature.size)
            .contentEquals(pdfSignature)
    }
    fun isNotEmptyFile(byteArray: ByteArray): Boolean {
        return byteArray.size > 4
    }
}
