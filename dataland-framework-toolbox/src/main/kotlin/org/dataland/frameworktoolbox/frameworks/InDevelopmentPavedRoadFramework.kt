package org.dataland.frameworktoolbox.frameworks

import org.dataland.frameworktoolbox.SpringConfig
import org.dataland.frameworktoolbox.specific.frameworkregistryimports.FrameworkRegistryImportsUpdater
import org.dataland.frameworktoolbox.template.ExcelTemplate
import org.dataland.frameworktoolbox.utils.DatalandRepository
import org.dataland.frameworktoolbox.utils.diagnostic.DiagnosticManager
import org.springframework.beans.factory.getBean
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import java.io.File

/**
 * You may choose to use the InDevelopmentPavedRoadFramework as a basis
 * for any frameworks that are currently in development. It turns a large amount of errors into warnings
 * that make the development experience more pleasant
 */
@Suppress("TooManyFunctions", "LongParameterList")
abstract class InDevelopmentPavedRoadFramework(
    identifier: String,
    label: String,
    explanation: String,
    frameworkTemplateCsvFile: File,
    order: Int,
    enabledFeatures: Set<FrameworkGenerationFeatures> = FrameworkGenerationFeatures.ENTRY_SET,
    isPrivateFramework: Boolean = false,
) : PavedRoadFramework(
        identifier, label, explanation, frameworkTemplateCsvFile, order, enabledFeatures,
        isPrivateFramework,
    ) {
    private fun ignoreErrors(
        compilationStep: String,
        lambda: () -> Unit,
    ) {
        @Suppress("TooGenericExceptionCaught")
        try {
            lambda()
        } catch (e: Exception) {
            logger.warn("Ignoring error during $compilationStep Compilation: ${e.message}")
        }
    }

    override fun compileFramework(datalandProject: DatalandRepository) {
        val context = AnnotationConfigApplicationContext(SpringConfig::class.java)
        val diagnostics = context.getBean<DiagnosticManager>()

        configureDiagnostics(diagnostics)
        val excelTemplate = ExcelTemplate.fromFile(frameworkTemplateCsvFile)
        customizeExcelTemplate(excelTemplate)

        val frameworkIntermediateRepresentation =
            convertExcelTemplateToToHighLevelComponentRepresentation(
                template = excelTemplate,
                context = context,
            )

        customizeHighLevelIntermediateRepresentation(frameworkIntermediateRepresentation)

        ignoreErrors("Data Model") { compileDataModel(datalandProject) }
        ignoreErrors("QA Model") { compileQaModel(datalandProject) }
        ignoreErrors("View Model") { compileViewModel(datalandProject) }
        ignoreErrors("Upload Model") { compileUploadModel(datalandProject) }
        ignoreErrors("Fake Fixture") { compileFixtureGenerator(datalandProject) }
        ignoreErrors("Specification") { compileSpecifications(datalandProject) }

        FrameworkRegistryImportsUpdater().update(datalandProject)
        datalandProject.gradleInterface.executeGradleTasks(listOf(":dataland-frontend:npm_run_typecheck"))
        logger.info("✔ Framework toolbox finished for framework $identifier ✨")
    }
}
