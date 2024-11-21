package org.dataland.frameworktoolbox.utils

import org.apache.commons.codec.binary.Hex
import java.security.MessageDigest
import java.util.Locale

/**
 * Return the string while ensuring that the first letter is capitalized with respect
 * to the english alphabet
 */
fun String.capitalizeEn(): String = replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ENGLISH) else it.toString() }

/**
 * Calculate the first 8 characters of the SHA2-256 checksum of this string
 */
fun String.shortSha(): String {
    val digest = MessageDigest.getInstance("SHA-256")
    val hash = digest.digest(this.encodeToByteArray())
    @Suppress("MagicNumber")
    return Hex.encodeHexString(hash).substring(0..7)
}
