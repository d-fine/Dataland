package org.dataland.frameworktoolbox.template.components

import org.dataland.datalandspecification.specifications.CalculationRule

/**
 * Parses the value of the "Field Calculation" column of the framework template.
 *
 * The expected format is a list of keyword/value pairs separated by semicolons, e.g.
 *
 * `"Sum": [extendedDecimalScope1GhgEmissionsInTonnes,extendedDecimalScope2GhgEmissionsInTonnes]; "Division": [example1,example2]`
 *
 * Each pair maps a calculation method (the keyword) to a bracketed comma-separated list of input data point identifiers.
 */
object FieldCalculationParser {
    private val rulePattern =
        Regex(
            """\s*"?([A-Za-z][A-Za-z0-9 _-]*)"?\s*:\s*\[([^\[\];]*)]\s*""",
        )

    /**
     * Parse the raw value of the "Field Calculation" column into a list of calculation rules.
     *
     * Returns an empty list if the input is null or blank.
     * @param rawValue raw cell content from the "Field Calculation" column
     * @return parsed calculation rules, or an empty list if the input is null or blank
     */
    fun parse(rawValue: String?): List<CalculationRule> {
        if (rawValue.isNullOrBlank()) return emptyList()

        return rawValue.split(';').map { rule ->
            val match = rulePattern.matchEntire(rule)
            require(match != null) {
                "Field Calculation column has unexpected format: '$rawValue'. " +
                    "Expected one or more entries like '\"Sum\": [input1,input2]; \"Division\": [input3,input4]'."
            }

            val (method, rawInputs) = match.destructured
            val inputs =
                rawInputs
                    .split(',')
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
            require(inputs.isNotEmpty()) {
                "Calculation rule '${method.trim()}' in '$rawValue' has no inputs."
            }
            CalculationRule(inputs = inputs, calculationMethod = method.trim())
        }
    }
}
