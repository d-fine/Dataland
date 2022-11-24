package org.dataland.datalandbackendutils.utils

import java.util.*
import java.util.zip.CRC32

class EncodingUtils {
    companion object {
        fun encodeToBase64(input: ByteArray): String {
            return Base64.getEncoder().encodeToString(input)
        }

        fun decodeFromBase64(input: String): ByteArray {
            return Base64.getDecoder().decode(input)
        }

        fun calculateCrc32Value(inputByteArray: ByteArray): Long {
            val crc32Instance = CRC32()
            crc32Instance.update(inputByteArray)
            return crc32Instance.value
        }
    }
}
