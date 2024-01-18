package org.dataland.frameworktoolbox.specific.viewconfig

import org.dataland.frameworktoolbox.intermediate.Framework
import org.dataland.frameworktoolbox.specific.viewconfig.elements.SectionConfigBuilder
import org.dataland.frameworktoolbox.specific.viewconfig.functional.FrameworkBooleanLambda
import org.dataland.frameworktoolbox.utils.DatalandRepository
import org.dataland.frameworktoolbox.utils.LoggerDelegate
import org.dataland.frameworktoolbox.utils.Naming.getNameFromLabel
import org.dataland.frameworktoolbox.utils.capitalizeEn
import org.dataland.frameworktoolbox.utils.freemarker.FreeMarker
import org.dataland.frameworktoolbox.utils.typescript.EsLintRunner
import java.io.FileWriter
import java.nio.file.Path
import kotlin.io.path.div

/**
 * A FrameworkViewConfigBuilder converts an Intermediate-Representation framework to a TypeScript View-Configuration
 * and integrates the generated code into a Dataland Repository.
 * @param framework the framework DataModel to convert
 */
class FrameworkViewConfigBuilder(
    private val framework: Framework,
) {

    private val logger by LoggerDelegate()
    private val generatedTsFiles = mutableListOf<Path>()

    val rootSectionConfigBuilder = SectionConfigBuilder(
        parentSection = null,
        label = "root-section",
        expandOnPageLoad = false,
        shouldDisplay = FrameworkBooleanLambda.TRUE,
    )

    private fun buildViewConfig(viewConfigTsPath: Path) {
        val freeMarkerContext = mapOf(
            "viewConfig" to rootSectionConfigBuilder.children,
            "frameworkDataType" to "${getNameFromLabel(framework.identifier).capitalizeEn()}Data",
            "viewConfigConstName" to getNameFromLabel(framework.identifier),
            "imports" to rootSectionConfigBuilder.imports,
        )

        val freemarkerTemplate = FreeMarker.configuration
            .getTemplate("/specific/viewconfig/ViewConfig.ts.ftl")

        val writer = FileWriter(viewConfigTsPath.toFile())
        generatedTsFiles.add(viewConfigTsPath)
        freemarkerTemplate.process(freeMarkerContext, writer)
        writer.close()
    }

    private fun buildApiClient(apiClientTsPath: Path) {
        val freeMarkerContext = mapOf(
            "frameworkBaseName" to getNameFromLabel(framework.identifier).capitalizeEn(),
        )

        val freemarkerTemplate = FreeMarker.configuration
            .getTemplate("/specific/viewconfig/ApiClient.ts.ftl")

        val writer = FileWriter(apiClientTsPath.toFile())
        generatedTsFiles.add(apiClientTsPath)
        freemarkerTemplate.process(freeMarkerContext, writer)
        writer.close()
    }

    private fun buildFrameworkDefinitionTs(baseDirectoryPath: Path) {
        val freeMarkerContext = mapOf(
            "frameworkIdentifier" to framework.identifier,
            "frameworkRootName" to getNameFromLabel(framework.identifier).capitalizeEn(), // TODO naming?
            "frameworkViewConfigConstName" to getNameFromLabel(framework.identifier),
            "frameworkLabel" to framework.label,
            "frameworkExplanation" to framework.explanation,
        )

        val outputJobs = listOf(
            Pair(
                "/specific/viewconfig/BaseFrameworkDefinition.ts.ftl",
                baseDirectoryPath / "BaseFrameworkDefinition.ts",
            ),
            Pair(
                "/specific/viewconfig/FrontendFrameworkDefinition.ts.ftl",
                baseDirectoryPath / "FrontendFrameworkDefinition.ts",
            ),
        )

        for ((template, outputPath) in outputJobs) {
            generatedTsFiles.add(outputPath)
            val freemarkerTemplate = FreeMarker.configuration
                .getTemplate(template)

            val writer = FileWriter(outputPath.toFile())
            freemarkerTemplate.process(freeMarkerContext, writer)
            writer.close()
        }
    }

    /**
     * Generate the code for the ViewConfig and integrates it into the Dataland Repository
     */
    fun build(into: DatalandRepository) {
        logger.info("Starting to build to backend data-model into the dataland-repository at ${into.path}")

        val frameworkConfigDir = into.frontendSrc / "frameworks" / framework.identifier
        with(frameworkConfigDir.toFile()) {
            deleteRecursively()
            mkdirs()
        }

        buildViewConfig(frameworkConfigDir / "ViewConfig.ts")
        buildApiClient(frameworkConfigDir / "ApiClient.ts")
        buildFrameworkDefinitionTs(frameworkConfigDir)

        into.gradleInterface.executeGradleTasks(listOf(":dataland-frontend:npm_run_checkfrontendcompilation"))

        EsLintRunner(into, generatedTsFiles).run()
    }
}
