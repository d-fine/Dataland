package org.dataland.frameworktoolbox.frameworks.nuclearandgas.custom

import org.dataland.frameworktoolbox.frameworks.eutaxonomynonfinancials.custom.EuTaxonomyNonAlignedActivitiesComponent
import org.dataland.frameworktoolbox.intermediate.components.ComponentBase
import org.dataland.frameworktoolbox.intermediate.group.ComponentGroupApi
import org.dataland.frameworktoolbox.intermediate.group.create
import org.dataland.frameworktoolbox.template.TemplateDiagnostic
import org.dataland.frameworktoolbox.template.components.ComponentGenerationUtils
import org.dataland.frameworktoolbox.template.components.TemplateComponentFactory
import org.dataland.frameworktoolbox.template.model.TemplateRow
import org.springframework.beans.factory.annotation.Autowired

class CustomComponentFactory (@Autowired val templateDiagnostic: TemplateDiagnostic) :
    TemplateComponentFactory {
    override fun canGenerateComponent(row: TemplateRow): Boolean =
        row.component.trim() == "Custom EuTaxonomyNonAlignedActivitiesComponent"

    override fun generateComponent(
        row: TemplateRow,
        utils: ComponentGenerationUtils,
        componentGroup: ComponentGroupApi,
    ): ComponentBase {
        templateDiagnostic.optionsNotUsed(row)
        templateDiagnostic.unitNotUsed(row)

        return componentGroup.create<EuTaxonomyNonAlignedActivitiesComponent>(
            utils.generateFieldIdentifierFromRow(row),
        ) {
            utils.setCommonProperties(row, this)
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