package org.dataland.frameworktoolbox.specific.inputconfig

import org.dataland.frameworktoolbox.intermediate.Framework
import org.dataland.frameworktoolbox.specific.inputconfig.elements.SectionInputConfigBuilder
import org.dataland.frameworktoolbox.specific.inputconfig.functional.FrameworkBooleanLambda
import org.dataland.frameworktoolbox.utils.DatalandRepository
import org.dataland.frameworktoolbox.utils.LoggerDelegate
import org.dataland.frameworktoolbox.utils.capitalizeEn
import org.dataland.frameworktoolbox.utils.freemarker.FreeMarker
import java.io.FileWriter
import java.nio.file.Path
import java.util.concurrent.TimeUnit
import kotlin.io.path.div

/**
 * A FrameworkInputConfigBuilder converts an Intermediate-Representation framework to a TypeScript Input-Configuration
 * and integrates the generated code into a Dataland Repository.
 * @param framework the framework DataModel to convert
 */
class FrameworkInputConfigBuilder(
    private val framework: Framework,
) {
    companion object {
        private const val ESLINT_TIMEOUT = 60L
    }

    private val logger by LoggerDelegate()

    val rootSectionConfigBuilder = SectionInputConfigBuilder(
        parentSection = null,
        label = "root-section",
        expandOnPageLoad = false,
        shouldDisplay = FrameworkBooleanLambda.TRUE,
    )

    private fun buildInputConfig(inputConfigTsPath: Path) {
        val freeMarkerContext = mapOf(
            "inputConfig" to rootSectionConfigBuilder.children,
            "frameworkDataType" to "${framework.identifier.capitalizeEn()}Data",
            "frameworkIdentifier" to framework.identifier,
            "imports" to rootSectionConfigBuilder.imports,
        )

        val freemarkerTemplate = FreeMarker.configuration
            .getTemplate("/specific/inputconfig/InputConfig.ts.ftl")

        val writer = FileWriter(inputConfigTsPath.toFile())
        freemarkerTemplate.process(freeMarkerContext, writer)
        writer.close()
    }

    private fun buildApiClient(apiClientTsPath: Path) {
        val freeMarkerContext = mapOf(
            "frameworkIdentifier" to framework.identifier,
        )

        val freemarkerTemplate = FreeMarker.configuration
            .getTemplate("/specific/inputconfig/ApiClient.ts.ftl")

        val writer = FileWriter(apiClientTsPath.toFile())
        freemarkerTemplate.process(freeMarkerContext, writer)
        writer.close()
    }

    private fun buildIndexTs(indexTsPath: Path) {
        val freeMarkerContext = mapOf(
            "frameworkIdentifier" to framework.identifier,
            "frameworkLabel" to framework.label,
            "frameworkExplanation" to framework.explanation,
        )

        val freemarkerTemplate = FreeMarker.configuration
            .getTemplate("/specific/inputconfig/index.ts.ftl")

        val writer = FileWriter(indexTsPath.toFile())
        freemarkerTemplate.process(freeMarkerContext, writer)
        writer.close()
    }

    /**
     * Generate the code for the InputConfig and integrates it into the Dataland Repository
     */
    fun build(into: DatalandRepository) {
        logger.info("Starting to build to backend data-model into the dataland-repository at ${into.path}")

        val frameworkConfigDir = into.frontendSrc / "frameworks" / framework.identifier
        with(frameworkConfigDir.toFile()) {
            deleteRecursively()
            mkdirs()
        }

        val inputConfigTsPath = frameworkConfigDir / "InputConfig.ts"

        buildInputConfig(inputConfigTsPath)
        buildApiClient(frameworkConfigDir / "ApiClient.ts")
        buildIndexTs(frameworkConfigDir / "index.ts")

        into.gradleInterface.executeGradleTasks(listOf(":dataland-frontend:npm_run_checkfrontendcompilation"))

        ProcessBuilder("npx", "eslint", "--fix", inputConfigTsPath.toAbsolutePath().toString())
            .directory((into.path / "dataland-frontend").toFile())
            .redirectOutput(ProcessBuilder.Redirect.INHERIT)
            .redirectError(ProcessBuilder.Redirect.INHERIT)
            .start()
            .waitFor(ESLINT_TIMEOUT, TimeUnit.SECONDS)
    }
}
