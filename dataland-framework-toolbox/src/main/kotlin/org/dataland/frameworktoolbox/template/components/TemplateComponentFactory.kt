package org.dataland.frameworktoolbox.template.components

import org.dataland.frameworktoolbox.intermediate.group.ComponentGroupApi
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
     */
    fun generateComponent(row: TemplateRow, utils: ComponentGenerationUtils, componentGroup: ComponentGroupApi)
}
