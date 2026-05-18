package org.dataland.datalandbackendutils.model

/**
 * A calculation rule defines how the value of a data point can be computed from a set of input data points.
 * @param inputs the list of input data point type identifiers used by the calculation.
 * @param calculationMethod the calculation method that combines the inputs (e.g. "Sum", "Division").
 */
data class CalculationRule(
    val inputs: List<String>,
    val calculationMethod: String,
)
