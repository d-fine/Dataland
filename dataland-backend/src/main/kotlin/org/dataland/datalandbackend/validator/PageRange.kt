package org.dataland.datalandbackend.validator

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import kotlin.reflect.KClass

/**
 * Annotation for validating a page range as a string.
 */
@Target(AnnotationTarget.FIELD)
@Constraint(validatedBy = [PageRangeValidator::class])
annotation class PageRange(
    val message: String =
        "Valid inputs are a comma-separated list of positive page numbers and/or ascending page ranges " +
            "(e.g. '4', '4-5' or '4, 112'). Numbers must not begin with a zero, ranges must consist of two " +
            "ascending numbers separated by '-', and list entries must be listed in strictly ascending, " +
            "non-overlapping order.",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)

/**
 * Validator class for validating a page reference consisting of a comma-separated list of single page
 * numbers and/or ascending page ranges (e.g. "4", "4-5" or "4, 112"). List entries must be strictly
 * ascending and non-overlapping.
 */
class PageRangeValidator : ConstraintValidator<PageRange, String> {
    /**
     * A single parsed list entry, e.g. "4" becomes PageEntry(4, 4) and "4-7" becomes PageEntry(4, 7).
     */
    private data class PageEntry(
        val start: Int,
        val end: Int,
    )

    companion object {
        private val regexEntry = """^([1-9]\d*)(?:-([1-9]\d*))?$""".toRegex()
    }

    override fun initialize(constraintAnnotation: PageRange) {
        // No initialization needed
    }

    override fun isValid(
        value: String?,
        context: ConstraintValidatorContext?,
    ): Boolean {
        if (value == null) return true

        val entries = value.split(",").map { parseEntry(it.trim()) }
        return entries.all { it != null } &&
            entries.filterNotNull().zipWithNext().all { (previous, current) -> current.start > previous.end }
    }

    /**
     * Parses a single trimmed list entry into a [PageEntry]. Returns null if the entry is not a valid
     * single page number or an ascending page range.
     */
    private fun parseEntry(entry: String): PageEntry? {
        val matchResult = regexEntry.matchEntire(entry) ?: return null
        val (startText, endText) = matchResult.destructured
        val start = startText.toInt()
        val end = if (endText.isEmpty()) start else endText.toInt()
        return if (endText.isNotEmpty() && start >= end) null else PageEntry(start, end)
    }
}
