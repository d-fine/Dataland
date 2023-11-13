package org.dataland.frameworktoolbox.template.components

import org.dataland.frameworktoolbox.intermediate.components.ComponentBase
import org.dataland.frameworktoolbox.intermediate.group.ComponentGroupApi
import org.dataland.frameworktoolbox.intermediate.logic.DependsOnComponentValue
import org.dataland.frameworktoolbox.template.model.TemplateRow

/**
 * TemplateComponentFactories are used to generate the high-level intermediate representation from
 * the Excel rows.
 */
interface TemplateComponentFactory {
    /**
     * Return true iff this factory can handle generating the component(s) for this row
     */
    fun canGenerateComponent(row: TemplateRow): Boolean

    /**
     * Generates the high-level intermediate components from the provided row of the Excel.
     * Only called iff canGenerateComponent(row) returns true.
     * @return the component used to compare against if another field declares a dependency on this field
     */
    fun generateComponent(row: TemplateRow, utils: ComponentGenerationUtils, componentGroup: ComponentGroupApi): ComponentBase?

    /**
     * Updates the conditional properties of the component identified by the row,
     */
    fun updateDependency(row: TemplateRow, componentIdentifierMap: Map<String, ComponentBase>) {
        if (row.dependency.isBlank())
            return

        val dependencyField = componentIdentifierMap[row.dependency.trim()]
        requireNotNull(dependencyField) {
            "Field ${row.fieldIdentifier} depends on non-existent field ${row.dependency}"
        }

        val myField = componentIdentifierMap[row.fieldIdentifier]
        requireNotNull(myField)

        myField.availableIf = DependsOnComponentValue(dependencyField, row.showWhenValueIs)
    }
}
