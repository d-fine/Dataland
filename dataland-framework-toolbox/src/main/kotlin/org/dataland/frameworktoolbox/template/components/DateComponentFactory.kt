package org.dataland.frameworktoolbox.template.components

import org.dataland.frameworktoolbox.intermediate.components.DateComponent
import org.dataland.frameworktoolbox.intermediate.group.ComponentGroupApi
import org.dataland.frameworktoolbox.intermediate.group.create
import org.dataland.frameworktoolbox.template.TemplateDiagnostic
import org.dataland.frameworktoolbox.template.model.TemplateRow
import org.dataland.frameworktoolbox.template.model.TemplateYesNo
import org.springframework.stereotype.Component

/**
 * Generates DateComponents from rows with the component "Date"
 */
@Component
class DateComponentFactory : TemplateComponentFactory {
    override fun canGenerateComponent(row: TemplateRow): Boolean = row.component == "Date"

    override fun generateComponent(
        row: TemplateRow,
        utils: ComponentGenerationUtils,
        componentGroup: ComponentGroupApi,
    ) {
        TemplateDiagnostic.optionsNotUsed(row)
        TemplateDiagnostic.unitNotUsed(row)
        TemplateDiagnostic.documentSupportNotUsed(row)

        componentGroup.create<DateComponent>(
            utils.generateFieldIdentifierFromRow(row),
        ) {
            isNullable = row.mandatoryField == TemplateYesNo.No
        }
    }
}
