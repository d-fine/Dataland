package org.dataland.frameworktoolbox.template.components

import org.dataland.frameworktoolbox.intermediate.components.ComponentBase
import org.dataland.frameworktoolbox.template.model.TemplateRow
import org.dataland.frameworktoolbox.template.model.TemplateYesNo
import org.dataland.frameworktoolbox.utils.Naming
import org.springframework.stereotype.Component

/**
 * A utility class used during Template conversion. Can be overridden / replaced
 * to cater to framework-specific needs.
 */
@Component
open class ComponentGenerationUtils {
    /**
     * Generate a camelCase identifier for a component from a template row
     */
    fun generateFieldIdentifierFromRow(row: TemplateRow): String {
        return Naming.getNameFromLabel(row.fieldName)
    }

    /**
     * Loads properties shared across components from the row into the component
     */
    fun setCommonProperties(row: TemplateRow, component: ComponentBase) {
        component.label = row.fieldName
        component.explanation = if (row.tooltip.isNotBlank()) row.tooltip else null
        component.isNullable = row.mandatoryField == TemplateYesNo.No
    }
}
