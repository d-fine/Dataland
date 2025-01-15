package org.dataland.frameworktoolbox.frameworks.sfdr

import org.dataland.frameworktoolbox.template.components.ComponentGenerationUtils
import org.dataland.frameworktoolbox.template.model.TemplateRow

/**
 * Component generation utils for the SFDR framework to align technical names with the old implementation
 */
class SfdrComponentGenerationUtils : ComponentGenerationUtils() {
    private fun appendUnitSuffixToFieldNameToMatchLegacyGeneration(unit: String): String =
        when (unit) {
            in setOf("Tonnes", "GWh", "Percent", "Cubic Meters", "Days") -> "In${unit.replace(" ", "")}"
            "Tonnes / M Revenue" -> "InTonnesPerMillionRevenue"
            "Tonnes / €M Revenue" -> "InTonnesPerMillionEURRevenue"
            "Cubic Meters / €M Revenue" -> "InCubicMetersPerMillionEURRevenue"
            "Cubic Meters / M Revenue" -> "InCubicMetersPerMillionRevenue"
            else -> ""
        }

    override fun generateFieldIdentifierFromRow(row: TemplateRow): String {
        val classicalName = super.generateFieldIdentifierFromRow(row)

        return classicalName + appendUnitSuffixToFieldNameToMatchLegacyGeneration(row.unit)
    }
}
