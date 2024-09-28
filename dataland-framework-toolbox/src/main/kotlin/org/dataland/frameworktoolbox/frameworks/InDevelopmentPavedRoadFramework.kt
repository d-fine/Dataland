package org.dataland.frameworktoolbox.frameworks

import org.dataland.frameworktoolbox.SpringConfig
import org.dataland.frameworktoolbox.intermediate.components.ReportPreuploadComponent
import org.dataland.frameworktoolbox.specific.datamodel.Annotation
import org.dataland.frameworktoolbox.specific.datamodel.FrameworkDataModelBuilder
import org.dataland.frameworktoolbox.specific.datamodel.elements.ReferencedReportValidatorBuilder
import org.dataland.frameworktoolbox.specific.frameworkregistryimports.FrameworkRegistryImportsUpdater
import org.dataland.frameworktoolbox.specific.viewconfig.elements.getKotlinFieldAccessor
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
    private fun compileDataModel(datalandProject: DatalandRepository) {
        if (!enabledFeatures.contains(FrameworkGenerationFeatures.BackendDataModel)) {
            return
        }
        val dataModel = generateDataModel(framework)
        customizeDataModel(dataModel)

        insertReferencedReportValidatorIfNeeded(dataModel)

        @Suppress("TooGenericExceptionCaught")
        try {
            dataModel.build(
                into = datalandProject,
                buildApiController = enabledFeatures.contains(FrameworkGenerationFeatures.BackendApiController),
                privateFrameworkBoolean = isPrivateFramework,
            )
        } catch (ex: Exception) {
            logger.error("Could not build framework data-model!", ex)
        }
    }

    private fun compileQaModel(datalandProject: DatalandRepository) {
        if (!enabledFeatures.contains(FrameworkGenerationFeatures.QaModel)) {
            return
        }
        val qaModelBuilder = framework.generateQaModel()
        customizeQaModel(qaModelBuilder)

        @Suppress("TooGenericExceptionCaught")
        try {
            qaModelBuilder.build(
                into = datalandProject,
            )
        } catch (ex: Exception) {
            logger.error("Could not build framework QA data-model!", ex)
        }
    }

    private fun insertReferencedReportValidatorIfNeeded(dataModel: FrameworkDataModelBuilder) {
        logger.info(
            "Searching for report preupload component to determine " +
                "if a referenced report validator is needed.",
        )
        val referencedReports = framework.root.nestedChildren.find { it is ReportPreuploadComponent }
        if (referencedReports != null) {
            val referencedReportsPath = referencedReports.getKotlinFieldAccessor()
            val extendedDocumentFileReferences =
                framework.root.nestedChildren
                    .flatMap { it.getExtendedDocumentReference() }
                    .toList()
            logger.info(
                "The validator will check for ${extendedDocumentFileReferences.size} " +
                    "extended document file references.",
            )
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

    private fun compileViewModel(datalandProject: DatalandRepository) {
        if (!enabledFeatures.contains(FrameworkGenerationFeatures.ViewPage)) {
            return
        }
        val viewConfig = generateViewModel(framework)
        customizeViewModel(viewConfig)

        @Suppress("TooGenericExceptionCaught")
        try {
            viewConfig.build(into = datalandProject, isPrivateFramework)
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

        val frameworkIntermediateRepresentation =
            convertExcelTemplateToToHighLevelComponentRepresentation(
                template = excelTemplate,
                context = context,
            )

        customizeHighLevelIntermediateRepresentation(frameworkIntermediateRepresentation)

        compileDataModel(datalandProject)
        compileQaModel(datalandProject)
        compileViewModel(datalandProject)
        compileUploadModel(datalandProject)
        compileFixtureGenerator(datalandProject)

        FrameworkRegistryImportsUpdater().update(datalandProject)
        datalandProject.gradleInterface.executeGradleTasks(listOf(":dataland-frontend:npm_run_typecheck"))
        logger.info("✔ Framework toolbox finished for framework $identifier ✨")
    }
}
