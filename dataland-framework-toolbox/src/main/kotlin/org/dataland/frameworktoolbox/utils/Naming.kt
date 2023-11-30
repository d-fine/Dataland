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
        return CaseUtils.toCamelCase(sanitizedLabel, useCapitalCase, '-', ',', '/', '(', ')', ':', '.', '"')
    }
}
