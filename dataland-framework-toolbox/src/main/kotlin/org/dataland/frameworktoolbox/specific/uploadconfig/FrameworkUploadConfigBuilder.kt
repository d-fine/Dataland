package org.dataland.frameworktoolbox.specific.uploadconfig

import org.dataland.frameworktoolbox.intermediate.Framework
import org.dataland.frameworktoolbox.specific.uploadconfig.elements.SectionUploadConfigBuilder
import org.dataland.frameworktoolbox.specific.viewconfig.functional.FrameworkBooleanLambda
import org.dataland.frameworktoolbox.utils.DatalandRepository
import org.dataland.frameworktoolbox.utils.LoggerDelegate
import org.dataland.frameworktoolbox.utils.capitalizeEn
import org.dataland.frameworktoolbox.utils.freemarker.FreeMarker
import java.io.FileWriter
import java.nio.file.Path
import kotlin.io.path.div

/**
 * A FrameworkUploadConfigBuilder converts an Intermediate-Representation framework to a TypeScript Input-Configuration
 * and integrates the generated code into a Dataland Repository.
 * @param framework the framework DataModel to convert
 */
class FrameworkUploadConfigBuilder(
    private val framework: Framework,
) {
    private val logger by LoggerDelegate()

    val rootSectionConfigBuilder = SectionUploadConfigBuilder(
        parentSection = null,
        label = "root-section",
        shouldDisplay = FrameworkBooleanLambda.TRUE,
        name = "???",
        subcategory = false,
    )

    private fun buildUploadConfig(uploadConfigTsPath: Path) {
        val freeMarkerContext = mapOf(
            "uploadConfig" to rootSectionConfigBuilder.children,
            "frameworkDataType" to "${framework.identifier.capitalizeEn()}Data",
            "frameworkIdentifier" to framework.identifier,
        )

        val freemarkerTemplate = FreeMarker.configuration
            .getTemplate("/specific/uploadconfig/UploadConfig.ts.ftl")

        val writer = FileWriter(uploadConfigTsPath.toFile())
        freemarkerTemplate.process(freeMarkerContext, writer)
        writer.close()
    }

    /**
     * Generate the code for the UploadConfig and integrates it into the Dataland Repository
     */
    fun build(into: DatalandRepository) {
        logger.info("Starting to build to backend data-model into the dataland-repository at ${into.path}")

        val frameworkConfigDir = into.frontendSrc / "frameworks" / framework.identifier
        with(frameworkConfigDir.toFile()) {
            mkdirs()
        }

        val uploadConfigTsPath = frameworkConfigDir / "UploadConfig.ts"

        buildUploadConfig(uploadConfigTsPath)

        into.gradleInterface.executeGradleTasks(listOf(":dataland-frontend:npm_run_checkfrontendcompilation"))

        // TODO Marc: Pls include EsLintRunner here too
    }
}
