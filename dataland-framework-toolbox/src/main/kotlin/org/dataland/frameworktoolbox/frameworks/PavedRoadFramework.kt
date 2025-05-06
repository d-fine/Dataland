package org.dataland.frameworktoolbox.frameworks

import org.dataland.frameworktoolbox.SpringConfig
import org.dataland.frameworktoolbox.intermediate.Framework
import org.dataland.frameworktoolbox.intermediate.components.ReportPreuploadComponent
import org.dataland.frameworktoolbox.specific.datamodel.Annotation
import org.dataland.frameworktoolbox.specific.datamodel.FrameworkDataModelBuilder
import org.dataland.frameworktoolbox.specific.datamodel.elements.ReferencedReportValidatorBuilder
import org.dataland.frameworktoolbox.specific.fixturegenerator.FrameworkFixtureGeneratorBuilder
import org.dataland.frameworktoolbox.specific.frameworkregistryimports.FrameworkRegistryImportsUpdater
import org.dataland.frameworktoolbox.specific.qamodel.FrameworkQaModelBuilder
import org.dataland.frameworktoolbox.specific.specification.FrameworkSpecificationBuilder
import org.dataland.frameworktoolbox.specific.uploadconfig.FrameworkUploadConfigBuilder
import org.dataland.frameworktoolbox.specific.viewconfig.FrameworkViewConfigBuilder
import org.dataland.frameworktoolbox.specific.viewconfig.elements.getKotlinFieldAccessor
import org.dataland.frameworktoolbox.template.ExcelTemplate
import org.dataland.frameworktoolbox.template.TemplateComponentBuilder
import org.dataland.frameworktoolbox.template.components.ComponentFactoryContainer
import org.dataland.frameworktoolbox.template.components.ComponentGenerationUtils
import org.dataland.frameworktoolbox.template.components.TemplateComponentFactory
import org.dataland.frameworktoolbox.utils.DatalandRepository
import org.dataland.frameworktoolbox.utils.LoggerDelegate
import org.dataland.frameworktoolbox.utils.diagnostic.DiagnosticManager
import org.springframework.beans.factory.getBean
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import java.io.File

/**
 * A PavedRoadFramework is the simplest way to integrate a new framework into Dataland or
 * update an existing one! It provides a template for implementing frameworks.
 */
@Suppress("TooManyFunctions", "LongParameterList")
abstract class PavedRoadFramework(
    val identifier: String,
    val label: String,
    val explanation: String,
    val frameworkTemplateCsvFile: File,
    val order: Int,
    val enabledFeatures: Set<FrameworkGenerationFeatures> = FrameworkGenerationFeatures.ENTRY_SET,
    val isPrivateFramework: Boolean = false,
) {
    val framework =
        Framework(
            identifier = identifier,
            label = label,
            explanation = explanation,
            order = order,
        )

    val logger by LoggerDelegate()

    /**
     * Can be overwritten to configure the diagnosticManager (to e.g., suppress issues)
     */
    open fun configureDiagnostics(diagnosticManager: DiagnosticManager) {
        // Empty as it's just a customization endpoint
    }

    /**
     * Can be overwritten to programmatically customize the excelTemplate (to e.g, add a prefix to all fields)
     */
    open fun customizeExcelTemplate(excelTemplate: ExcelTemplate) {
        // Empty as it's just a customization endpoint
    }

    /**
     * Retrieve the ComponentGenerationUtils instance for this framework.
     * Can be overwritten to supply a custom, framework-specific instance (to e.g., customize the field-name generation)
     */
    open fun getComponentGenerationUtils(): ComponentGenerationUtils = ComponentGenerationUtils()

    /**
     * Retrieve a list of TemplateComponentFactories that are responsible for converting template rows.
     * Can be overwritten to e.g., insert factories for framework-specific components
     */
    open fun getComponentFactoriesForIntermediateRepresentation(context: ApplicationContext): List<TemplateComponentFactory> {
        val containerBean = context.getBean<ComponentFactoryContainer>()
        return containerBean.factories.reversed()
    }

    /**
     * Convert the excel-template to a high-level component representation.
     */
    open fun convertExcelTemplateToToHighLevelComponentRepresentation(
        context: ApplicationContext,
        template: ExcelTemplate,
    ): Framework {
        val generationUtils = getComponentGenerationUtils()
        val componentFactories = getComponentFactoriesForIntermediateRepresentation(context)

        val intermediateBuilder =
            TemplateComponentBuilder(
                template = template,
                componentFactories = componentFactories,
                generationUtils = generationUtils,
            )
        intermediateBuilder.build(into = framework.root)
        return framework
    }

    /**
     * Can be overwritten to programmatically customize the framework representation
     * (to e.g, add new fields)
     */
    open fun customizeHighLevelIntermediateRepresentation(framework: Framework) {
        // Empty as it's just a customization endpoint
    }

    /**
     * Generate the data-model for the framework
     */
    open fun generateDataModel(framework: Framework): FrameworkDataModelBuilder = framework.generateDataModel()

    /**
     * Can be overwritten to programmatically customize the dataModel
     * (to e.g, change the JVM type of certain fields)
     */
    open fun customizeDataModel(dataModel: FrameworkDataModelBuilder) {
        // Empty as it's just a customization endpoint
    }

    /**
     * Generate the QA-model for the framework
     */
    open fun generateQaModel(framework: Framework): FrameworkQaModelBuilder = framework.generateQaModel()

    /**
     * Can be overwritten to programmatically customize the QA dataModel
     * (to e.g, change the JVM type of certain fields)
     */
    open fun customizeQaModel(dataModel: FrameworkQaModelBuilder) {
        // Empty as it's just a customization endpoint
    }

    /**
     * Generate the view-model for the framework
     */
    open fun generateViewModel(framework: Framework): FrameworkViewConfigBuilder = framework.generateViewModel()

    /**
     * Generate the upload-model for the framework
     */
    open fun generateUploadModel(framework: Framework): FrameworkUploadConfigBuilder = framework.generateUploadModel()

    /**
     * Can be overwritten to programmatically customize the viewModel
     * (to e.g, change the way certain fields are displayed in the frontend)
     */
    open fun customizeViewModel(viewModel: FrameworkViewConfigBuilder) {
        // Empty as it's just a customization endpoint
    }

    /**
     * Can be overwritten to programmatically customize the uploadModel
     * (to e.g, change the way certain fields are displayed in the frontend)
     */
    open fun customizeUploadModel(uploadModel: FrameworkUploadConfigBuilder) {
        // Empty as it's just a customization endpoint
    }

    /**
     * Generate the fixture-generator for the framework
     */
    open fun generateFakeFixtureGenerator(framework: Framework): FrameworkFixtureGeneratorBuilder = framework.generateFixtureGenerator()

    /**
     * Can be overwritten to programmatically customize the fixtureGenerator
     * (to e.g, change the probabilities for certain outcomes of the fixture generation)
     */
    open fun customizeFixtureGenerator(fixtureGenerator: FrameworkFixtureGeneratorBuilder) {
        // Empty as it's just a customization endpoint
    }

    /**
     * Can be overwritten to programmatically customize the specifications
     */
    open fun customizeSpecifications(specifications: FrameworkSpecificationBuilder) {
        // Empty as it's just a customization endpoint
    }

    protected fun compileDataModel(datalandProject: DatalandRepository) {
        if (!enabledFeatures.contains(FrameworkGenerationFeatures.BackendDataModel)) {
            return
        }
        val dataModel = generateDataModel(framework)
        customizeDataModel(dataModel)

        insertReferencedReportValidatorIfNeeded(dataModel)

        dataModel.build(
            into = datalandProject,
            buildApiController = enabledFeatures.contains(FrameworkGenerationFeatures.BackendApiController),
            privateFrameworkBoolean = isPrivateFramework,
            assembledDataset = enabledFeatures.contains(FrameworkGenerationFeatures.DataPointSpecifications),
        )
    }

    protected fun compileQaModel(datalandProject: DatalandRepository) {
        if (!enabledFeatures.contains(FrameworkGenerationFeatures.QaModel)) {
            return
        }
        val qaModelBuilder = generateQaModel(framework)
        customizeQaModel(qaModelBuilder)

        qaModelBuilder.build(
            into = datalandProject,
            assembledDataset = enabledFeatures.contains(FrameworkGenerationFeatures.DataPointSpecifications),
        )
    }

    private fun insertReferencedReportValidatorIfNeeded(dataModel: FrameworkDataModelBuilder) {
        val referencedReports = framework.root.nestedChildren.find { it is ReportPreuploadComponent }
        if (referencedReports != null) {
            val referencedReportsPath = referencedReports.getKotlinFieldAccessor()
            val extendedDocumentFileReferences =
                framework.root.nestedChildren
                    .flatMap { it.getExtendedDocumentReference() }
                    .toList()

            val validatorPackage = dataModel.rootPackageBuilder.addPackage("validator")
            val referencedReportValidatorBuilder =
                ReferencedReportValidatorBuilder(
                    validatorPackage,
                    dataModel.rootDataModelClass,
                    framework.identifier,
                    referencedReportsPath,
                    extendedDocumentFileReferences,
                )
            validatorPackage.childElements.add(referencedReportValidatorBuilder)

            dataModel.rootDataModelClass.annotations.add(
                Annotation(referencedReportValidatorBuilder.fullyQualifiedName),
            )
        }
    }

    protected fun compileViewModel(datalandProject: DatalandRepository) {
        if (!enabledFeatures.contains(FrameworkGenerationFeatures.ViewPage)) {
            return
        }
        val viewConfig = generateViewModel(framework)
        customizeViewModel(viewConfig)
        viewConfig.build(into = datalandProject, isPrivateFramework)
    }

    protected fun compileFixtureGenerator(datalandProject: DatalandRepository) {
        if (!enabledFeatures.contains(FrameworkGenerationFeatures.FakeFixtures)) {
            return
        }
        val fixtureGenerator = generateFakeFixtureGenerator(framework)
        customizeFixtureGenerator(fixtureGenerator)
        fixtureGenerator.build(into = datalandProject)
    }

    protected fun compileUploadModel(datalandProject: DatalandRepository) {
        if (!enabledFeatures.contains(FrameworkGenerationFeatures.UploadPage)) {
            return
        }
        val uploadConfig = generateUploadModel(framework)
        customizeUploadModel(uploadConfig)
        uploadConfig.build(into = datalandProject)
    }

    protected fun compileSpecifications(datalandProject: DatalandRepository) {
        if (!enabledFeatures.contains(FrameworkGenerationFeatures.DataPointSpecifications)) {
            return
        }
        framework.root.nestedChildren.forEach {
            require(!it.isRequired) {
                "All components must be optional for the data point migration to work." +
                    "The component ${it.identifier} is marked as required."
            }
        }
        val specifications = framework.generateSpecifications(datalandProject)
        specifications.build()
    }

    /**
     * Compiles a framework following the template and integrates it into the dataland repository
     */
    open fun compileFramework(datalandProject: DatalandRepository) {
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
        diagnostics.finalizeDiagnosticStream()

        customizeHighLevelIntermediateRepresentation(frameworkIntermediateRepresentation)

        compileDataModel(datalandProject)
        compileQaModel(datalandProject)
        compileViewModel(datalandProject)
        compileUploadModel(datalandProject)
        compileFixtureGenerator(datalandProject)
        compileSpecifications(datalandProject)

        FrameworkRegistryImportsUpdater().update(datalandProject)
        datalandProject.gradleInterface.executeGradleTasks(listOf(":dataland-frontend:npm_run_typecheck"))
        diagnostics.finalizeDiagnosticStream()
        logger.info("✔ Framework toolbox finished for framework $identifier ✨")
    }
}
