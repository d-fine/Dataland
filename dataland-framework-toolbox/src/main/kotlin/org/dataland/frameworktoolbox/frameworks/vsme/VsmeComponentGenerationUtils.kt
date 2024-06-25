package org.dataland.frameworktoolbox.frameworks.vsme

import org.dataland.frameworktoolbox.template.components.ComponentGenerationUtils
import org.dataland.frameworktoolbox.template.model.TemplateRow

/**
 * Component generation utils for the VSME framework
 */
class VsmeComponentGenerationUtils : ComponentGenerationUtils() {

    private fun appendUnitSuffixToFieldName(unit: String): String {
        return when (unit) {
            in setOf("Tonnes", "MWh", "Percent", "Cubic Meters", "Days", "Hectare", "Euro") ->
                "In${unit.replace(" ", "")}"
            "tCO2eq" -> "InTonnesOfCO2Equivalents"
            "Euro/h" -> "InEuroPerHour"
            else -> ""
        }
    }

    override fun generateFieldIdentifierFromRow(row: TemplateRow): String {
        val classicalName = super.generateFieldIdentifierFromRow(row)

        return classicalName + appendUnitSuffixToFieldName(row.unit)
    }
}
