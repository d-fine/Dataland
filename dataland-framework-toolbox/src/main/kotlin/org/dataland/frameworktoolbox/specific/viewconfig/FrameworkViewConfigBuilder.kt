package org.dataland.frameworktoolbox.specific.viewconfig

import org.dataland.frameworktoolbox.intermediate.Framework
import org.dataland.frameworktoolbox.specific.viewconfig.elements.SectionConfigBuilder
import org.dataland.frameworktoolbox.specific.viewconfig.functional.FrameworkBooleanLambda
import org.dataland.frameworktoolbox.utils.DatalandRepository
import org.dataland.frameworktoolbox.utils.LoggerDelegate
import org.dataland.frameworktoolbox.utils.capitalizeEn
import org.dataland.frameworktoolbox.utils.freemarker.FreeMarker
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

    val rootSectionConfigBuilder = SectionConfigBuilder(
        parentSection = null,
        label = "root-section",
        expandOnPageLoad = false,
        shouldDisplay = FrameworkBooleanLambda.TRUE,
    )

    private fun buildViewConfig(viewConfigTsPath: Path) {
        val freeMarkerContext = mapOf(
            "viewConfig" to rootSectionConfigBuilder.children,
            "frameworkDataType" to "${framework.identifier.capitalizeEn()}Data",
            "frameworkIdentifier" to framework.identifier,
            "imports" to rootSectionConfigBuilder.imports,
        )

        val freemarkerTemplate = FreeMarker.configuration
            .getTemplate("/specific/viewconfig/ViewConfig.ts.ftl")

        val writer = FileWriter(viewConfigTsPath.toFile())
        freemarkerTemplate.process(freeMarkerContext, writer)
        writer.close()
    }

    private fun buildApiClient(apiClientTsPath: Path) {
        val freeMarkerContext = mapOf(
            "frameworkIdentifier" to framework.identifier,
        )

        val freemarkerTemplate = FreeMarker.configuration
            .getTemplate("/specific/viewconfig/ApiClient.ts.ftl")

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
            .getTemplate("/specific/viewconfig/index.ts.ftl")

        val writer = FileWriter(indexTsPath.toFile())
        freemarkerTemplate.process(freeMarkerContext, writer)
        writer.close()
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
        buildIndexTs(frameworkConfigDir / "index.ts")
    }
}
