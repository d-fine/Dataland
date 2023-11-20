package org.dataland.frameworktoolbox.frameworks

import org.dataland.frameworktoolbox.intermediate.Framework
import org.dataland.frameworktoolbox.intermediate.components.ComponentBase
import org.dataland.frameworktoolbox.intermediate.group.ComponentGroupApi
import org.dataland.frameworktoolbox.template.ExcelTemplate
import org.dataland.frameworktoolbox.template.TemplateComponentBuilder
import org.dataland.frameworktoolbox.template.components.ComponentGenerationUtils
import org.dataland.frameworktoolbox.template.components.TemplateComponentFactory
import org.dataland.frameworktoolbox.template.model.TemplateRow
import org.springframework.context.ApplicationContext
import java.io.File

/**
 * You may choose to use the InDevelopmentPavedRoadFramework as a basis
 * for any frameworks that are currently in development. It turns a large amount of errors into warnings
 * that make the development experience more pleasant
 */
abstract class InDevelopmentPavedRoadFramework(
    identifier: String,
    label: String,
    explanation: String,
    frameworkTemplateCsvFile: File,
) :
    PavedRoadFramework(identifier, label, explanation, frameworkTemplateCsvFile, false) {

    override fun convertExcelTemplateToToHighLevelComponentRepresentation(
        context: ApplicationContext,
        template: ExcelTemplate,
    ): Framework {
        val generationUtils = getComponentGenerationUtils()
        val componentFactories = getComponentFactoriesForIntermediateRepresentation(context)

        // Register custom converter that in this case just ignores all unknown fields.
        val noopComponentFactory = object : TemplateComponentFactory {
            override fun canGenerateComponent(row: TemplateRow): Boolean = true

            override fun generateComponent(
                row: TemplateRow,
                utils: ComponentGenerationUtils,
                componentGroup: ComponentGroupApi,
            ): ComponentBase? {
                println("No-one wants to generate components for ${row.component}")
                return null
            }

            override fun updateDependency(
                row: TemplateRow,
                utils: ComponentGenerationUtils,
                componentIdentifierMap: Map<String, ComponentBase>,
            ) {
                // NOOP
            }
        }

        val intermediateBuilder = TemplateComponentBuilder(
            template = template,
            componentFactories = componentFactories + noopComponentFactory,
            generationUtils = generationUtils,
        )
        intermediateBuilder.build(into = framework.root)
        return framework
    }
}
