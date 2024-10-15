package org.dataland.frameworktoolbox.frameworks.esgdatenkatalog.custom

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
 * Generates EsgDatenkatalogYearlyDecimalTimeseriesDataComponentFactory
 */
@Component
class EsgDatenkatalogYearlyDecimalTimeseriesDataComponentFactory(
    @Autowired val templateDiagnostic: TemplateDiagnostic,
) : TemplateComponentFactory {
    override fun canGenerateComponent(row: TemplateRow): Boolean =
        row.component.trim() in setOf("Custom - Rolling Window", "Custom - Rolling Window (past-only)")

    override fun generateComponent(
        row: TemplateRow,
        utils: ComponentGenerationUtils,
        componentGroup: ComponentGroupApi,
    ): ComponentBase {
        templateDiagnostic.optionsNotUsed(row)
        templateDiagnostic.unitNotUsed(row)

        return componentGroup.create<EsgDatenkatalogYearlyDecimalTimeseriesDataComponent>(
            utils.generateFieldIdentifierFromRow(row),
        ) {
            utils.setCommonProperties(row, this)
            uploadBehaviour =
                if (row.component.contains("past-only")) {
                    EsgDatenkatalogYearlyDecimalTimeseriesDataComponent.UploadBehaviour.ThreeYearPast
                } else {
                    EsgDatenkatalogYearlyDecimalTimeseriesDataComponent.UploadBehaviour.ThreeYearDelta
                }
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
