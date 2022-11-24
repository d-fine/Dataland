package org.dataland.datalandbackendutils.utils

import java.util.*
import java.util.zip.CRC32

/**
 * This class provides common encoding and checksum computation functionality
 */
object EncodingUtils {
    /**
     * Encodes a byte array using base64
     * @param input is the byte array to be encoded
     * @return the base64 encoded input byte array
     */
    fun encodeToBase64(input: ByteArray): String {
        return Base64.getEncoder().encodeToString(input)
    }

    /**
     * Decodes a string that is base64 encoded
     * @param input the encoded string
     * @return the byte array obtained from decoding the base64 encoded input string
     */
    fun decodeFromBase64(input: String): ByteArray {
        return Base64.getDecoder().decode(input)
    }

    /**
     * Calculates the CRC32 checksum from a byte array
     * @param inputByteArray the byte array to calculate the checksum of
     * @return the CRC32 checksum
     */
    fun calculateCrc32Value(inputByteArray: ByteArray): Long {
        val crc32Instance = CRC32()
        crc32Instance.update(inputByteArray)
        return crc32Instance.value
    }
}
