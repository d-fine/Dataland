package org.dataland.frameworktoolbox.template.components

import org.dataland.frameworktoolbox.intermediate.components.ComponentBase
import org.dataland.frameworktoolbox.intermediate.components.PercentageComponent
import org.dataland.frameworktoolbox.intermediate.group.ComponentGroupApi
import org.dataland.frameworktoolbox.intermediate.group.create
import org.dataland.frameworktoolbox.template.TemplateDiagnostic
import org.dataland.frameworktoolbox.template.model.TemplateRow
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 * Generates PercentageComponents from rows with the component "Percentage"
 */
@Component
class PercentageComponentFactory(
    @Autowired val templateDiagnostic: TemplateDiagnostic,
) : TemplateComponentFactory {
    companion object {
        const val NO_UPLOAD_OPTION = "NoUpload"

        /**
         * Parses the options list and checks whether the NoUpload option is set. Returns true if it finds the option.
         */
        fun hasNoUploadOption(input: String): Boolean = input.contains(NO_UPLOAD_OPTION)
    }

    override fun canGenerateComponent(row: TemplateRow): Boolean = row.component == "Percentage"

    override fun generateComponent(
        row: TemplateRow,
        utils: ComponentGenerationUtils,
        componentGroup: ComponentGroupApi,
    ): ComponentBase {
        templateDiagnostic.unitNotUsed(row)

        return componentGroup.create<PercentageComponent>(
            utils.generateFieldIdentifierFromRow(row),
        ) {
            utils.setCommonProperties(row, this)
            this.hasNoUpload = hasNoUploadOption(row.options)
        }
    }

    override fun updateDependency(
        row: TemplateRow,
        utils: ComponentGenerationUtils,
        componentIdentifierMap: Map<String, ComponentBase>,
    ) {
        utils.defaultDependencyConfiguration(row, componentIdentifierMap, templateDiagnostic)
    }
}
