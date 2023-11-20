package org.dataland.frameworktoolbox.frameworks.heimathafen

import org.dataland.frameworktoolbox.frameworks.PavedRoadFramework
import org.dataland.frameworktoolbox.intermediate.Framework
import org.dataland.frameworktoolbox.intermediate.group.ComponentGroup
import org.dataland.frameworktoolbox.intermediate.group.edit
import org.dataland.frameworktoolbox.template.ExcelTemplate
import org.dataland.frameworktoolbox.template.components.ComponentGenerationUtils
import org.dataland.frameworktoolbox.utils.diagnostic.DiagnosticManager
import org.springframework.stereotype.Component
import java.io.File

@Component
class HeimathafenFramework: PavedRoadFramework(
    identifier = "heimathafen",
    label = "Heimathafen",
    explanation = "Das Heimathafen Framework",
    File("./dataland-framework-toolbox/inputs/heimathafen/Heimathafen_Data_Model-German version.csv")
) {
    var excelTemplate: ExcelTemplate? = null

    override fun configureDiagnostics(diagnostics: DiagnosticManager) {
        diagnostics.suppress("TemplateConversion-UnusedColumn-showWhenValueIs-Row-205-85a39ab3")
    }

    override fun customizeHighLevelIntermediateRepresentation(framework: Framework) {
        framework.root.edit<ComponentGroup>("general") {
            viewPageExpandOnPageLoad = true
            edit<ComponentGroup>("datenanbieter") {
                viewPageExpandOnPageLoad = true
            }
        }
    }

    override fun customizeExcelTemplate(excelTemplate: ExcelTemplate) {
        this.excelTemplate = excelTemplate
    }

    override fun getComponentGenerationUtils(): ComponentGenerationUtils {
        return HeimathafenComponentGenerationUtils(excelTemplate!!)
    }
}