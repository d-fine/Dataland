package org.dataland.frameworktoolbox.template.components

import org.dataland.frameworktoolbox.intermediate.components.StringComponent
import org.dataland.frameworktoolbox.intermediate.group.ComponentGroupApi
import org.dataland.frameworktoolbox.intermediate.group.create
import org.dataland.frameworktoolbox.template.TemplateDiagnostic
import org.dataland.frameworktoolbox.template.model.TemplateRow
import org.dataland.frameworktoolbox.template.model.TemplateYesNo
import org.springframework.stereotype.Component

/**
 * Generates StringComponents from rows with the component "String"
 */
@Component
class StringComponentFactory : TemplateComponentFactory {
    override fun canGenerateComponent(row: TemplateRow): Boolean = row.component == "String"

    override fun generateComponent(
        row: TemplateRow,
        utils: ComponentGenerationUtils,
        componentGroup: ComponentGroupApi,
    ) {
        TemplateDiagnostic.optionsNotUsed(row)
        TemplateDiagnostic.unitNotUsed(row)
        TemplateDiagnostic.documentSupportNotUsed(row)

        componentGroup.create<StringComponent>(
            utils.generateFieldIdentifierFromRow(row),
        ) {
            isNullable = row.mandatoryField == TemplateYesNo.No
        }
    }
}
