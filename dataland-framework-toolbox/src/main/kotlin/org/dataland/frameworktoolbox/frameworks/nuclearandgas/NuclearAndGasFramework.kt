package org.dataland.frameworktoolbox.frameworks.nuclearandgas
import org.dataland.frameworktoolbox.SpringConfig
import org.dataland.frameworktoolbox.frameworks.FrameworkGenerationFeatures
import org.dataland.frameworktoolbox.frameworks.InDevelopmentPavedRoadFramework
import org.dataland.frameworktoolbox.specific.frameworkregistryimports.FrameworkRegistryImportsUpdater
import org.dataland.frameworktoolbox.template.ExcelTemplate
import org.dataland.frameworktoolbox.utils.DatalandRepository
import org.dataland.frameworktoolbox.utils.diagnostic.DiagnosticManager
import org.springframework.beans.factory.getBean
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import org.springframework.stereotype.Component
import java.io.File

/**
 * The EU Taxonomy Nuclear And Gas Framework
 */
@Component
class NuclearAndGasFramework : InDevelopmentPavedRoadFramework(
    identifier = "nuclear-and-gas",
    label = "EU Taxonomy Nuclear and Gas Framework",
    explanation = "EU Taxonomy Nuclear and Gas Framework according to the Commission Delegated Regulation (EU)" +
        " 2021/2178, Annex XII ",
    File("./dataland-framework-toolbox/inputs/nuclear-and-gas/nuclear-and-gas.xlsx"),
    order = 3,
    enabledFeatures = FrameworkGenerationFeatures.ENTRY_SET,
) {
    override fun compileFramework(datalandProject: DatalandRepository) {
        val context = AnnotationConfigApplicationContext(SpringConfig::class.java)
        val diagnostics = context.getBean<DiagnosticManager>()

        configureDiagnostics(diagnostics)
        val excelTemplate = ExcelTemplate.fromFile(frameworkTemplateCsvFile, "Framework Data Model")
        customizeExcelTemplate(excelTemplate)

        val frameworkIntermediateRepresentation = convertExcelTemplateToToHighLevelComponentRepresentation(
            template = excelTemplate,
            context = context,
        )
        diagnostics.finalizeDiagnosticStream()

        customizeHighLevelIntermediateRepresentation(frameworkIntermediateRepresentation)

        compileDataModel(datalandProject)
        compileQaModel(datalandProject)
        compileViewModel(datalandProject)
        compileUploadModel(datalandProject)
        compileFixtureGenerator(datalandProject)

        FrameworkRegistryImportsUpdater().update(datalandProject)
        datalandProject.gradleInterface.executeGradleTasks(listOf(":dataland-frontend:npm_run_typecheck"))
        diagnostics.finalizeDiagnosticStream()
        logger.info("✔ Framework toolbox finished for framework $identifier ✨")
    }
}
