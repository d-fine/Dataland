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
@Suppress("LongParameterList")
abstract class InDevelopmentPavedRoadFramework(
    identifier: String,
    label: String,
    explanation: String,
    frameworkTemplateCsvFile: File,
    order: Int,
    customUploadConfig: Boolean,
    enabledFeatures: Set<FrameworkGenerationFeatures> = FrameworkGenerationFeatures.entries.toSet(),
) :
    PavedRoadFramework(identifier, label, explanation, frameworkTemplateCsvFile, order, customUploadConfig) {

    private fun compileDataModel(datalandProject: DatalandRepository) {
        if (!enabledFeatures.contains(FrameworkGenerationFeatures.DataModel)) {
            return
        }
        val dataModel = generateDataModel(framework)
        customizeDataModel(dataModel)

        @Suppress("TooGenericExceptionCaught")
        try {
            dataModel.build(into = datalandProject)
        } catch (ex: Exception) {
            logger.error("Could not build framework data-model!", ex)
        }
    }

    private fun compileViewModel(datalandProject: DatalandRepository) {
        if (!enabledFeatures.contains(FrameworkGenerationFeatures.ViewPage)) {
            return
        }
        val viewConfig = generateViewModel(framework)
        customizeViewModel(viewConfig)

        @Suppress("TooGenericExceptionCaught")
        try {
            viewConfig.build(into = datalandProject)
        } catch (ex: Exception) {
            logger.error("Could not build framework view configuration!", ex)
        }
    }

    private fun compileFixtureGenerator(datalandProject: DatalandRepository) {
        if (!enabledFeatures.contains(FrameworkGenerationFeatures.FakeFixtures)) {
            return
        }
        val fixtureGenerator = generateFakeFixtureGenerator(framework)
        customizeFixtureGenerator(fixtureGenerator)

        @Suppress("TooGenericExceptionCaught")
        try {
            fixtureGenerator.build(into = datalandProject)
        } catch (ex: Exception) {
            logger.error("Could not build framework fixture generator", ex)
        }
    }

    private fun compileUploadModel(datalandProject: DatalandRepository) {
        if (!enabledFeatures.contains(FrameworkGenerationFeatures.UploadPage)) {
            return
        }
        val uploadConfig = generateUploadModel(framework)

        customizeUploadModel(uploadConfig)

        @Suppress("TooGenericExceptionCaught")
        try {
            uploadConfig.build(into = datalandProject)
        } catch (ex: Exception) {
            logger.error("Could not build framework upload configuration!", ex)
        }
    }

    override fun compileFramework(datalandProject: DatalandRepository) {
        val context = AnnotationConfigApplicationContext(SpringConfig::class.java)
        val diagnostics = context.getBean<DiagnosticManager>()

        configureDiagnostics(diagnostics)
        val excelTemplate = ExcelTemplate.fromFile(frameworkTemplateCsvFile)
        customizeExcelTemplate(excelTemplate)

        val frameworkIntermediateRepresentation = convertExcelTemplateToToHighLevelComponentRepresentation(
            template = excelTemplate,
            context = context,
        )

        customizeHighLevelIntermediateRepresentation(frameworkIntermediateRepresentation)

        compileDataModel(datalandProject)
        compileViewModel(datalandProject)
        if (customUploadConfig) {
            compileUploadModel(datalandProject)
        }
        compileFixtureGenerator(datalandProject)

        FrameworkRegistryImportsUpdater().update(datalandProject)
        logger.info("✔ Framework toolbox finished for framework $identifier ✨")
    }
}
