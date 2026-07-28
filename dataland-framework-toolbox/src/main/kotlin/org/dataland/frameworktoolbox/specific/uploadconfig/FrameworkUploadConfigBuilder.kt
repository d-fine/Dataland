package org.dataland.frameworktoolbox.specific.uploadconfig

import org.dataland.frameworktoolbox.intermediate.Framework
import org.dataland.frameworktoolbox.specific.uploadconfig.elements.UploadCategoryBuilder
import org.dataland.frameworktoolbox.specific.uploadconfig.elements.UploadCellConfigBuilder
import org.dataland.frameworktoolbox.specific.viewconfig.functional.FrameworkBooleanLambda
import org.dataland.frameworktoolbox.utils.DatalandRepository
import org.dataland.frameworktoolbox.utils.LoggerDelegate
import org.dataland.frameworktoolbox.utils.Naming
import org.dataland.frameworktoolbox.utils.capitalizeEn
import org.dataland.frameworktoolbox.utils.freemarker.FreeMarker
import org.dataland.frameworktoolbox.utils.typescript.EsLintPrettierRunner
import org.dataland.frameworktoolbox.utils.typescript.TypeScriptImport
import java.io.FileWriter
import java.nio.file.Path
import kotlin.io.path.div

/**
 * A FrameworkUploadConfigBuilder converts an Intermediate-Representation framework to a TypeScript Upload-Configuration
 * and integrates the generated code into a Dataland Repository.
 * @param framework the framework DataModel to convert
 */
class FrameworkUploadConfigBuilder(
    private val framework: Framework,
) {
    private val logger by LoggerDelegate()

    val rootSectionConfigBuilder =
        UploadCategoryBuilder(
            parentSection = null,
            label = "root-section",
            name = "root-section-name",
            shouldDisplay = FrameworkBooleanLambda.TRUE,
        )

    private fun buildUploadConfig(uploadConfigTsPath: Path) {
        var anyLambdaFunctionUsesDataset = false
        rootSectionConfigBuilder.traverse {
            anyLambdaFunctionUsesDataset =
                when (it) {
                    is UploadCategoryBuilder -> anyLambdaFunctionUsesDataset || it.shouldDisplay.usesDataset
                    is UploadCellConfigBuilder -> anyLambdaFunctionUsesDataset || it.shouldDisplay.usesDataset
                }
        }

        val freeMarkerContext =
            mapOf(
                "uploadConfig" to rootSectionConfigBuilder.children,
                "frameworkDataType" to "${framework.identifier.capitalizeEn()}Data",
                "frameworkBaseNameInCamelCase" to Naming.getNameFromLabel(framework.identifier),
                "frameworkIdentifier" to framework.identifier,
                "imports" to TypeScriptImport.mergeImports(rootSectionConfigBuilder.imports),
                "anyLambdaFunctionUsesDataset" to anyLambdaFunctionUsesDataset,
            )

        val freemarkerTemplate =
            FreeMarker.configuration
                .getTemplate("/specific/uploadconfig/UploadConfig.ts.ftl")

        val writer = FileWriter(uploadConfigTsPath.toFile())
        freemarkerTemplate.process(freeMarkerContext, writer)
        writer.close()
    }

    /**
     * Generate the code for the UploadConfig and integrates it into the Dataland Repository
     */
    fun build(into: DatalandRepository) {
        logger.info("Starting to build the upload-config into the dataland-repository at ${into.path}")
        rootSectionConfigBuilder.assertComplianceWithLegacyUploadPage()

        val frameworkConfigDir = into.frontendSrc / "frameworks" / framework.identifier
        with(frameworkConfigDir.toFile()) {
            mkdirs()
        }

        val uploadConfigTsPath = frameworkConfigDir / "UploadConfig.ts"

        buildUploadConfig(uploadConfigTsPath)

        into.gradleInterface.executeGradleTasks(listOf(":dataland-frontend:npmInstall"))

        EsLintPrettierRunner(into, listOf(uploadConfigTsPath)).run()

        // WORKAROUND to display different labels without breaking the API. Remove once API is updated!!!
        val uploadConfigFile = java.io.File("$frameworkConfigDir/UploadConfig.ts")
        var uploadConfigContent = uploadConfigFile.readText()

        val replacements =
            listOf(
                listOf("'Water Consumption'", "'Water Withdrawal'"),
                listOf("'Relative Water Usage'", "'Water Withdrawal Intensity'"),
                listOf(
                    "'Violation Of Tax Rules And Regulation'",
                    "'Violation of UNGC principles and OECD Guidelines for Multinational Enterprises'",
                ),
                listOf("'Reported Child Labour Incidents'", "'Risk of Child Labour Incidents'"),
                listOf("'Reported Forced Or Compulsory Labour Incidents'", "'Risk of Forced Or Compulsory Labour Incidents'"),
                listOf("'Reported Convictions Of Bribery and Corruption'", "'Number of Reported Convictions Of Bribery and Corruption'"),
            )

        for (replacement in replacements) {
            uploadConfigContent = uploadConfigContent.replace(replacement[0], replacement[1])
        }
        uploadConfigFile.writeText(uploadConfigContent)
    }
}
