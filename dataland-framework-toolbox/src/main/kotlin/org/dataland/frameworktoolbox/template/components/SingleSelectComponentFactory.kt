package org.dataland.frameworktoolbox.template.components

import org.dataland.frameworktoolbox.intermediate.components.ComponentBase
import org.dataland.frameworktoolbox.intermediate.components.SingleSelectComponent
import org.dataland.frameworktoolbox.intermediate.group.ComponentGroupApi
import org.dataland.frameworktoolbox.intermediate.group.create
import org.dataland.frameworktoolbox.template.TemplateDiagnostic
import org.dataland.frameworktoolbox.template.model.TemplateRow
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 * Generates SingleSelectComponents from rows with the component "Single-Select Dropdown"
 */
@Component
class SingleSelectComponentFactory(
    @Autowired val templateDiagnostic: TemplateDiagnostic,
) : TemplateComponentFactory {
    private val nameMap =
        mapOf(
            "Single-Select Dropdown" to SingleSelectComponent.UploadMode.Dropdown,
            "Single-Select Radio Button" to SingleSelectComponent.UploadMode.RadioButtons,
        )

    override fun canGenerateComponent(row: TemplateRow): Boolean = row.component in nameMap.keys

    override fun generateComponent(
        row: TemplateRow,
        utils: ComponentGenerationUtils,
        componentGroup: ComponentGroupApi,
    ): ComponentBase {
        templateDiagnostic.unitNotUsed(row)

        return componentGroup.create<SingleSelectComponent>(
            utils.generateFieldIdentifierFromRow(row),
        ) {
            utils.setCommonProperties(row, this)
            this.options = utils.getSelectionOptionsFromOptionColumn(row)
            val mappedMode = nameMap[row.component]
            requireNotNull(mappedMode) { "Unknown upload mode ${row.component}" }
            this.uploadMode = mappedMode
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
