package org.dataland.frameworktoolbox.frameworks.eutaxonomynonfinancials.custom

import org.dataland.frameworktoolbox.intermediate.components.ComponentBase
import org.dataland.frameworktoolbox.intermediate.group.ComponentGroupApi
import org.dataland.frameworktoolbox.intermediate.group.create
import org.dataland.frameworktoolbox.template.TemplateDiagnostic
import org.dataland.frameworktoolbox.template.components.ComponentGenerationUtils
import org.dataland.frameworktoolbox.template.components.TemplateComponentFactory
import org.dataland.frameworktoolbox.template.model.TemplateRow
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

/**
 * Generates EuTaxonomyAmountWithCurrency from rows with the component "Custom EuTaxonomyAmountWithCurrencyComponent"
 */
@Component
class EuTaxonomyAmountWithCurrencyComponentFactory(@Autowired val templateDiagnostic: TemplateDiagnostic) :
    TemplateComponentFactory {
    override fun canGenerateComponent(row: TemplateRow): Boolean =
        row.component.trim() == "Custom EuTaxonomyAmountWithCurrencyComponent"

    override fun generateComponent(
        row: TemplateRow,
        utils: ComponentGenerationUtils,
        componentGroup: ComponentGroupApi,
    ): ComponentBase {
        templateDiagnostic.optionsNotUsed(row)
        templateDiagnostic.unitNotUsed(row)

        return componentGroup.create<EuTaxonomyAmountWithCurrencyComponent>(
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
