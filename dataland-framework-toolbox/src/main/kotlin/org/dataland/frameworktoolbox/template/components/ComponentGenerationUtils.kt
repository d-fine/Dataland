package org.dataland.frameworktoolbox.template.components

import org.dataland.frameworktoolbox.template.model.TemplateRow
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
}
