package org.dataland.frameworktoolbox.frameworks

import org.dataland.frameworktoolbox.SpringConfig
import org.dataland.frameworktoolbox.intermediate.Framework
import org.dataland.frameworktoolbox.specific.datamodel.FrameworkDataModelBuilder
import org.dataland.frameworktoolbox.specific.fixturegenerator.FrameworkFixtureGeneratorBuilder
import org.dataland.frameworktoolbox.specific.frameworkregistryimports.FrameworkRegistryImportsUpdater
import org.dataland.frameworktoolbox.specific.viewconfig.FrameworkViewConfigBuilder
import org.dataland.frameworktoolbox.template.ExcelTemplate
import org.dataland.frameworktoolbox.template.TemplateComponentBuilder
import org.dataland.frameworktoolbox.template.components.ComponentGenerationUtils
import org.dataland.frameworktoolbox.template.components.TemplateComponentFactory
import org.dataland.frameworktoolbox.utils.DatalandRepository
import org.dataland.frameworktoolbox.utils.diagnostic.DiagnosticManager
import org.springframework.beans.factory.getBean
import org.springframework.beans.factory.getBeansOfType
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import java.io.File

abstract class PavedRoadFramework(
    val identifier: String,
    val label: String,
    val explanation: String,
    val frameworkTemplateCsvFile: File,
) {
    val framework = Framework(
        identifier = identifier,
        label = label,
        explanation = explanation
    )

    open fun configureDiagnostics(diagnosticManager: DiagnosticManager) {

    }

    open fun customizeExcelTemplate(excelTemplate: ExcelTemplate) {}

    open fun getComponentGenerationUtils(): ComponentGenerationUtils {
        return ComponentGenerationUtils()
    }

    open fun getComponentFactoriesForIntermediateRepresentation(
        context: ApplicationContext
    ): List<TemplateComponentFactory> {
        return context.getBeansOfType<TemplateComponentFactory>().values.toList()
    }

    open fun convertExcelTemplateToToHighLevelComponentRepresentation(
        context: ApplicationContext,
        template: ExcelTemplate
    ): Framework {
        val generationUtils = getComponentGenerationUtils()
        val componentFactories = getComponentFactoriesForIntermediateRepresentation(context)

        val intermediateBuilder = TemplateComponentBuilder(
            template = template,
            componentFactories = componentFactories,
            generationUtils = generationUtils,
        )
        intermediateBuilder.build(into = framework.root)
        return framework
    }

    open fun customizeHighLevelIntermediateRepresentation(framework: Framework) {}

    open fun generateDataModel(framework: Framework): FrameworkDataModelBuilder {
        return framework.generateDataModel()
    }

    open fun customizeDataModel(dataModel: FrameworkDataModelBuilder) {}

    open fun generateViewModel(framework: Framework): FrameworkViewConfigBuilder {
        return framework.generateViewModel()
    }

    open fun customizeViewModel(viewModel: FrameworkViewConfigBuilder) {}

    open fun generateFakeFixtureGenerator(framework: Framework): FrameworkFixtureGeneratorBuilder {
        return framework.generateFixtureGenerator()
    }

    open fun customizeFixtureGenerator(fixtureGenerator: FrameworkFixtureGeneratorBuilder) {}

    fun compileFramework(datalandProject: DatalandRepository) {
        val context = AnnotationConfigApplicationContext(SpringConfig::class.java)
        val diagnostics = context.getBean<DiagnosticManager>()

        configureDiagnostics(diagnostics)
        val excelTemplate = ExcelTemplate.fromCsv(frameworkTemplateCsvFile)
        customizeExcelTemplate(excelTemplate)

        val frameworkIntermediateRepresentation = convertExcelTemplateToToHighLevelComponentRepresentation(
            template = excelTemplate,
            context = context
        )
        diagnostics.finalizeDiagnosticStream()

        customizeHighLevelIntermediateRepresentation(frameworkIntermediateRepresentation)

        val dataModel = generateDataModel(framework)
        customizeDataModel(dataModel)
        dataModel.build(into = datalandProject)

        val viewConfig = generateViewModel(framework)
        customizeViewModel(viewConfig)
        viewConfig.build(into = datalandProject)

        val fixtureGenerator = generateFakeFixtureGenerator(framework)
        customizeFixtureGenerator(fixtureGenerator)
        fixtureGenerator.build(into = datalandProject)

        FrameworkRegistryImportsUpdater().update(datalandProject)
        diagnostics.finalizeDiagnosticStream()
    }
}