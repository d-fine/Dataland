package org.dataland.datalandbackendutils.utils

import java.security.MessageDigest

/**
 * Converts a Byte-Array to a hexadecimal string representing its contents
 */
fun ByteArray.toHex(): String = joinToString(separator = "") { eachByte -> "%02x".format(eachByte) }

/**
 * Calculates the SHA-256 hash of a byte-array and returns the hash as a hex-encoded string
 */
fun ByteArray.sha256(): String {
    val digester = MessageDigest.getInstance("SHA-256")
    val digest = digester.digest(this)
    return digest.toHex()
}
