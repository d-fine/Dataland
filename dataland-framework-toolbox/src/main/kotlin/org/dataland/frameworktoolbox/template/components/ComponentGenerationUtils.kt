package org.dataland.frameworktoolbox.template.components

import org.dataland.frameworktoolbox.intermediate.components.ComponentBase
import org.dataland.frameworktoolbox.intermediate.components.support.SelectionOption
import org.dataland.frameworktoolbox.intermediate.datapoints.DocumentSupport
import org.dataland.frameworktoolbox.intermediate.logic.DependsOnComponentValue
import org.dataland.frameworktoolbox.template.TemplateDiagnostic
import org.dataland.frameworktoolbox.template.model.TemplateRow
import org.dataland.frameworktoolbox.template.model.TemplateYesNo
import org.dataland.frameworktoolbox.utils.Naming
import org.dataland.frameworktoolbox.utils.capitalizeEn
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
    open fun generateSectionIdentifierFromRow(row: TemplateRow): String = Naming.getNameFromLabel(row.category)

    /**
     * Generate a camelCase identifier for a subSection from a template row
     */
    open fun generateSubSectionIdentifierFromRow(row: TemplateRow): String = Naming.getNameFromLabel(row.subCategory)

    /**
     * Generate a camelCase identifier for a component from a template row
     */
    open fun generateFieldIdentifierFromRow(row: TemplateRow): String = Naming.getNameFromLabel(row.fieldName)

    /**
     * Loads properties shared across components from the row into the component
     */
    open fun setCommonProperties(
        row: TemplateRow,
        component: ComponentBase,
    ) {
        component.label = row.fieldName
        component.uploadPageExplanation =
            if (row.combinedTooltip?.isNotBlank() == true) {
                row.combinedTooltip
            } else if (row.uploadPageTooltip?.isNotBlank() == true) {
                row.uploadPageTooltip
            } else {
                null
            }

        component.viewPageExplanation =
            if (row.viewPageTooltip?.isNotBlank() == true) {
                row.viewPageTooltip
            } else {
                null
            }

        component.isNullable = row.mandatoryField == TemplateYesNo.No
        component.documentSupport = DocumentSupport.fromTemplate(row.documentSupport)
    }

    /**
     * Loads the options column required for some components (e.g. drop-downs)
     */
    open fun getSelectionOptionsFromOptionColumn(row: TemplateRow): Set<SelectionOption> {
        val stringOptions =
            row.options
                .split("|")
                .map { it.trim() }

        val mappedOptions =
            stringOptions
                .map {
                    SelectionOption(
                        identifier = Naming.getNameFromLabel(it).capitalizeEn(),
                        label = it,
                    )
                }.toSet()

        require(mappedOptions.isNotEmpty()) {
            "Field ${row.fieldIdentifier} does not specify required options for component ${row.component}."
        }
        return mappedOptions
    }

    /**
     * Inserts showWhenValueIs Inter-Field dependencies into the datamodel
     */
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
