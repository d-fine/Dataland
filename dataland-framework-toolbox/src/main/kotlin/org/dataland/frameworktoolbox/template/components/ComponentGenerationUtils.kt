package org.dataland.frameworktoolbox.template.components

import org.dataland.frameworktoolbox.intermediate.components.ComponentBase
import org.dataland.frameworktoolbox.intermediate.datapoints.DocumentSupport
import org.dataland.frameworktoolbox.intermediate.logic.DependsOnComponentValue
import org.dataland.frameworktoolbox.template.TemplateDiagnostic
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
     * Generate a camelCase identifier for a section from a template row
     */
    open fun generateSectionIdentifierFromRow(row: TemplateRow): String {
        return Naming.getNameFromLabel(row.category)
    }

    /**
     * Generate a camelCase identifier for a subSection from a template row
     */
    open fun generateSubSectionIdentifierFromRow(row: TemplateRow): String {
        return Naming.getNameFromLabel(row.subCategory)
    }

    /**
     * Generate a camelCase identifier for a component from a template row
     */
    open fun generateFieldIdentifierFromRow(row: TemplateRow): String {
        return Naming.getNameFromLabel(row.fieldName)
    }

    /**
     * Loads properties shared across components from the row into the component
     */
    open fun setCommonProperties(row: TemplateRow, component: ComponentBase) {
        component.label = row.fieldName
        component.explanation = if (row.tooltip.isNotBlank()) row.tooltip else null
        component.isNullable = row.mandatoryField == TemplateYesNo.No
        component.documentSupport = DocumentSupport.fromTemplate(row.documentSupport)
    }

    open fun defaultDependencyConfiguration(
        row: TemplateRow,
        identifierMap: Map<String, ComponentBase>,
        diagnostic: TemplateDiagnostic,
    ) {
        if (row.dependency.isBlank()) {
            diagnostic.showWhenValueIsNotUsed(row)
            return
        }

        val dependencyField = identifierMap[row.dependency.trim()]
        requireNotNull(dependencyField) {
            "Field ${row.fieldIdentifier} depends on non-existent field ${row.dependency}"
        }

        val myField = identifierMap[row.fieldIdentifier]
        requireNotNull(myField)

        myField.availableIf = DependsOnComponentValue(dependencyField, row.showWhenValueIs)
    }
}
