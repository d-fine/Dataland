package org.dataland.frameworktoolbox.template

import org.dataland.frameworktoolbox.intermediate.components.ComponentBase
import org.dataland.frameworktoolbox.intermediate.group.ComponentGroup
import org.dataland.frameworktoolbox.intermediate.group.ComponentGroupApi
import org.dataland.frameworktoolbox.intermediate.group.create
import org.dataland.frameworktoolbox.intermediate.group.getOrNull
import org.dataland.frameworktoolbox.template.components.ComponentGenerationUtils
import org.dataland.frameworktoolbox.template.components.TemplateComponentFactory
import org.dataland.frameworktoolbox.template.model.TemplateRow

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
    private fun getSubsectionForRow(
        base: ComponentGroupApi,
        row: TemplateRow,
    ): ComponentGroupApi =
        if (row.category.isBlank()) {
            require(row.subCategory.isBlank()) {
                "Row ${row.fieldIdentifier} defines a subcategory but no category"
            }
            base
        } else {
            val sectionName = generationUtils.generateSectionIdentifierFromRow(row)
            val section =
                base.getOrNull<ComponentGroup>(sectionName)
                    ?: base.create(sectionName) {
                        label = row.category
                    }

            if (row.subCategory.isBlank()) {
                section
            } else {
                val subsectionName = generationUtils.generateSubSectionIdentifierFromRow(row)
                val subsection =
                    section.getOrNull<ComponentGroup>(subsectionName)
                        ?: section.create(subsectionName) {
                            label = row.subCategory
                        }

                subsection
            }
        }

    private fun getFactoryForRow(row: TemplateRow): TemplateComponentFactory =
        componentFactories.firstOrNull { it.canGenerateComponent(row) }
            ?: throw IllegalStateException(
                "Cannot find a suitable converter for the row $row. Maybe it has an invalid component name?",
            )

    private fun initialBuildPassForCreatingComponents(into: ComponentGroupApi): Map<String, ComponentBase> {
        val componentMapForDependencies = mutableMapOf<String, ComponentBase>()
        for (row in template.rows) {
            val group = getSubsectionForRow(into, row)
            val factory = getFactoryForRow(row)
            require(!componentMapForDependencies.containsKey(row.fieldIdentifier)) {
                "Duplicate component identifier ${row.fieldIdentifier}"
            }
            val generatedComponent = factory.generateComponent(row, generationUtils, group)
            generatedComponent?.let { componentMapForDependencies[row.fieldIdentifier] = it }
        }
        return componentMapForDependencies
    }

    private fun secondBuildPassForDefiningDependencies(componentIdentifierMap: Map<String, ComponentBase>) {
        for (row in template.rows) {
            val factory = getFactoryForRow(row)
            factory.updateDependency(row, generationUtils, componentIdentifierMap)
        }
    }

    /**
     * Use the Excel template to build a high-level intermediate representation of a framework
     * into the provided ComponentGroup
     */
    fun build(into: ComponentGroupApi) {
        val componentIdentifierMap = initialBuildPassForCreatingComponents(into)
        secondBuildPassForDefiningDependencies(componentIdentifierMap)
    }
}
