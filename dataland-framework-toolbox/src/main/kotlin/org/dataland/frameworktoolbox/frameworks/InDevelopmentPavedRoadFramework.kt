package org.dataland.frameworktoolbox.frameworks

import org.dataland.frameworktoolbox.SpringConfig
import org.dataland.frameworktoolbox.intermediate.Framework
import org.dataland.frameworktoolbox.intermediate.components.ComponentBase
import org.dataland.frameworktoolbox.intermediate.group.ComponentGroupApi
import org.dataland.frameworktoolbox.specific.frameworkregistryimports.FrameworkRegistryImportsUpdater
import org.dataland.frameworktoolbox.template.ExcelTemplate
import org.dataland.frameworktoolbox.template.TemplateComponentBuilder
import org.dataland.frameworktoolbox.template.components.ComponentGenerationUtils
import org.dataland.frameworktoolbox.template.components.TemplateComponentFactory
import org.dataland.frameworktoolbox.template.model.TemplateRow
import org.dataland.frameworktoolbox.utils.DatalandRepository
import org.dataland.frameworktoolbox.utils.LoggerDelegate
import org.dataland.frameworktoolbox.utils.diagnostic.DiagnosticManager
import org.springframework.beans.factory.getBean
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import java.io.File

/**
 * You may choose to use the InDevelopmentPavedRoadFramework as a basis
 * for any frameworks that are currently in development. It turns a large amount of errors into warnings
 * that make the development experience more pleasant
 */
abstract class InDevelopmentPavedRoadFramework(
    identifier: String,
    label: String,
    explanation: String,
    frameworkTemplateCsvFile: File,
) :
    PavedRoadFramework(identifier, label, explanation, frameworkTemplateCsvFile) {

    override fun convertExcelTemplateToToHighLevelComponentRepresentation(
        context: ApplicationContext,
        template: ExcelTemplate,
    ): Framework {
        val generationUtils = getComponentGenerationUtils()
        val componentFactories = getComponentFactoriesForIntermediateRepresentation(context)

        // Register custom converter that in this case just ignores all unknown fields.
        val noopComponentFactory = object : TemplateComponentFactory {
            override fun canGenerateComponent(row: TemplateRow): Boolean = true

            override fun generateComponent(
                row: TemplateRow,
                utils: ComponentGenerationUtils,
                componentGroup: ComponentGroupApi,
            ): ComponentBase? {
                logger.warn("No-one wants to generate components for ${row.component} (Row $row)")
                return null
            }

            override fun updateDependency(
                row: TemplateRow,
                utils: ComponentGenerationUtils,
                componentIdentifierMap: Map<String, ComponentBase>,
            ) {
                // NOOP
            }
        }

        val intermediateBuilder = TemplateComponentBuilder(
            template = template,
            componentFactories = componentFactories + noopComponentFactory,
            generationUtils = generationUtils,
        )
        intermediateBuilder.build(into = framework.root)
        return framework
    }

    private fun compileDataModel(datalandProject: DatalandRepository) {
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
        val excelTemplate = ExcelTemplate.fromCsv(frameworkTemplateCsvFile)
        customizeExcelTemplate(excelTemplate)

        val frameworkIntermediateRepresentation = convertExcelTemplateToToHighLevelComponentRepresentation(
            template = excelTemplate,
            context = context,
        )

        customizeHighLevelIntermediateRepresentation(frameworkIntermediateRepresentation)

        compileDataModel(datalandProject)
        compileViewModel(datalandProject)
        compileUploadModel(datalandProject)
        compileFixtureGenerator(datalandProject)

        FrameworkRegistryImportsUpdater().update(datalandProject)
        logger.info("✔ Framework toolbox finished for framework $identifier ✨")
    }
}
