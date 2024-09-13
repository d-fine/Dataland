package org.dataland.datalandbackend.validator

/**
 * Validator class for checking if a page range string is valid.
 */
class PageRangeValidator {
    companion object {
        private val regexPage = """^([1-9]\d*)(?:-([1-9]\d*))?$""".toRegex()
    }

    /**
     * Checks whether the provided page range string is valid.
     *
     * @param value The string representing the page range.
     * @return `true` if the page range is valid, `false` otherwise.
     */
    fun isValid(value: String?): Boolean {
        if (value == null) return true

        val matchResult = regexPage.matchEntire(value)
        return if (matchResult != null) {
            val (a, b) = matchResult.destructured
            if (b.isEmpty()) {
                true
            } else {
                val pageA = a.toInt()
                val pageB = b.toInt()
                pageA < pageB
            }
        } else {
            false
        }
    }
}
