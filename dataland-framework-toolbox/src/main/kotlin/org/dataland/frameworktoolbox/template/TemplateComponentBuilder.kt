package org.dataland.frameworktoolbox.template

import org.dataland.frameworktoolbox.intermediate.group.ComponentGroup
import org.dataland.frameworktoolbox.intermediate.group.ComponentGroupApi
import org.dataland.frameworktoolbox.intermediate.group.create
import org.dataland.frameworktoolbox.intermediate.group.getOrNull
import org.dataland.frameworktoolbox.template.components.ComponentGenerationUtils
import org.dataland.frameworktoolbox.template.components.TemplateComponentFactory
import org.dataland.frameworktoolbox.template.model.TemplateRow
import org.dataland.frameworktoolbox.utils.Naming

/**
 * Builds a High-Level Intermediate Representation of a Framework using an ExcelTemplate.
 * @param template the ExcelTemplate to convert
 * @param componentFactories a list of ComponentFactories for the conversion
 * @param generationUtils an implementation of the ComponentGenerationUtils
 */
class TemplateComponentBuilder(
    private val template: ExcelTemplate,
    private val componentFactories: List<TemplateComponentFactory>,
    private val generationUtils: ComponentGenerationUtils,
) {

    private fun getSubsectionForRow(base: ComponentGroupApi, row: TemplateRow): ComponentGroupApi {
        return if (row.category.isBlank()) {
            require(row.subCategory.isBlank()) {
                "Row ${row.fieldIdentifier} defines a subcategory but no category"
            }
            base
        } else {
            val sectionName = Naming.getNameFromLabel(row.category)
            val section = base.getOrNull<ComponentGroup>(sectionName)
                ?: base.create(sectionName) {
                    label = row.category
                }

            if (row.subCategory.isBlank()) {
                section
            } else {
                val subsectionName = Naming.getNameFromLabel(row.subCategory)
                val subsection = section.getOrNull<ComponentGroup>(subsectionName)
                    ?: section.create(subsectionName) {
                        label = row.subCategory
                    }

                subsection
            }
        }
    }

    private fun getFactoryForRow(row: TemplateRow): TemplateComponentFactory {
        return componentFactories.firstOrNull { it.canGenerateComponent(row) }
            ?: throw IllegalStateException(
                "Cannot find a suitable converter for the row $row. Maybe it has an invalid component name?",
            )
    }

    /**
     * Use the Excel template to build a high-level intermediate representation of a framework
     * into the provided ComponentGroup
     */
    fun build(into: ComponentGroupApi) {
        for (row in template.rows) {
            val group = getSubsectionForRow(into, row)
            val factory = getFactoryForRow(row)
            factory.generateComponent(row, generationUtils, group)
        }
    }
}
