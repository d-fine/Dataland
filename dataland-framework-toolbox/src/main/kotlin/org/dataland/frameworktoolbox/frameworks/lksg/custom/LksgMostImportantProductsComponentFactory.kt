package org.dataland.frameworktoolbox.frameworks.lksg.custom

import org.dataland.frameworktoolbox.frameworks.sfdr.custom.SfdrHighImpactClimateSectors
import org.dataland.frameworktoolbox.intermediate.components.ComponentBase
import org.dataland.frameworktoolbox.intermediate.group.ComponentGroupApi
import org.dataland.frameworktoolbox.intermediate.group.create
import org.dataland.frameworktoolbox.template.TemplateDiagnostic
import org.dataland.frameworktoolbox.template.components.ComponentGenerationUtils
import org.dataland.frameworktoolbox.template.components.TemplateComponentFactory
import org.dataland.frameworktoolbox.template.model.TemplateRow
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class LksgMostImportantProductsComponentFactory(@Autowired val templateDiagnostic: TemplateDiagnostic) :
        TemplateComponentFactory {
        override fun canGenerateComponent(row: TemplateRow): Boolean =
            row.component == "Custom LkSG Most-Important-Products"

        override fun generateComponent(
            row: TemplateRow,
            utils: ComponentGenerationUtils,
            componentGroup: ComponentGroupApi,
        ): ComponentBase {
            templateDiagnostic.optionsNotUsed(row)
            templateDiagnostic.unitNotUsed(row)

            return componentGroup.create<SfdrHighImpactClimateSectors>(
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