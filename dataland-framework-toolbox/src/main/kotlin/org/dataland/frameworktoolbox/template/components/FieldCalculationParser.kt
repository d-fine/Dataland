package org.dataland.frameworktoolbox.template.components

import org.dataland.datalandspecification.specifications.CalculationRule

/**
 * Parses the value of the "Field Calculation" column of the framework template.
 *
 * The expected format is a list of keyword/value pairs separated by semi-colons, e.g.
 *
 * `"Sum": [extendedDecimalScope1GhgEmissionsInTonnes;extendedDecimalScope2GhgEmissionsInTonnes]; "Division": [example1,example2]`
 *
 * Each pair maps a calculation method (the keyword) to a bracketed list of input data point identifiers.
 * Inside the brackets, inputs may be separated by either a semi-colon or a comma.
 */
object FieldCalculationParser {
    private val rulePattern =
        Regex(
            """"?(?<method>[A-Za-z][A-Za-z0-9 _-]*)"?\s*:\s*\[(?<inputs>[^\[\]]*)\]""",
        )

    /**
     * Parse the raw value of the "Field Calculation" column into a list of calculation rules.
     * Returns null if the input is null or blank.
     */
    fun parse(rawValue: String?): List<CalculationRule>? {
        if (rawValue.isNullOrBlank()) return null

        val matches = rulePattern.findAll(rawValue).toList()
        require(matches.isNotEmpty()) {
            "Field Calculation column has unexpected format: '$rawValue'. " +
                "Expected one or more entries like '\"Sum\": [input1;input2]; \"Division\": [input3,input4]'."
        }

        return matches.map { match ->
            val method = match.groups["method"]!!.value.trim()
            val inputs =
                match.groups["inputs"]!!
                    .value
                    .split(';', ',')
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
            require(inputs.isNotEmpty()) {
                "Calculation rule '$method' in '$rawValue' has no inputs."
            }
            CalculationRule(inputs = inputs, calculationMethod = method)
        }
    }
}
