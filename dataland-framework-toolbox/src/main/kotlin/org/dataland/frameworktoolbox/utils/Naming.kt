package org.dataland.frameworktoolbox.utils

import org.apache.commons.text.CaseUtils

/**
 * A utility for converting between naming conventions
 */
object Naming {
    /**
     * Converts an input label (in "Sentence case") to camelCase (e.g., "sentenceCase")
     */
    fun getNameFromLabel(label: String, useCapitalCase: Boolean = false): String {
        val sanitizedLabel = label.replace("&", " And ")
        return removeUnallowedJavaIdentifierCharacters(
            CaseUtils.toCamelCase(sanitizedLabel, useCapitalCase, '-', ',', '/', '(', ')', ':', '.', '"'),
        )
    }

    /**
     * Removes characters that are not allowed in a java identifier from a string
     * @param inputString the string to remove unallowed characters from
     */
    fun removeUnallowedJavaIdentifierCharacters(inputString: String): String {
        val regex = Regex("[-,/():.?]")
        return inputString.replace(regex, "")
    }
}
