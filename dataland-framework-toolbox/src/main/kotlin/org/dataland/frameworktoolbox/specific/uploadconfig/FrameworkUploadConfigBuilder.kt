package org.dataland.frameworktoolbox.specific.uploadconfig

import org.dataland.frameworktoolbox.intermediate.Framework
import org.dataland.frameworktoolbox.specific.uploadconfig.elements.UploadCategoryBuilder
import org.dataland.frameworktoolbox.specific.viewconfig.functional.FrameworkBooleanLambda
import org.dataland.frameworktoolbox.utils.DatalandRepository
import org.dataland.frameworktoolbox.utils.LoggerDelegate
import org.dataland.frameworktoolbox.utils.capitalizeEn
import org.dataland.frameworktoolbox.utils.freemarker.FreeMarker
import org.dataland.frameworktoolbox.utils.typescript.EsLintRunner
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

    val rootSectionConfigBuilder = UploadCategoryBuilder(
        parentSection = null,
        label = "root-section",
        name = "root-section-name",
        shouldDisplay = FrameworkBooleanLambda.TRUE,
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
        logger.info("Starting to build the upload-config into the dataland-repository at ${into.path}")
        rootSectionConfigBuilder.assertComplianceWithLegacyUploadPage()

        val frameworkConfigDir = into.frontendSrc / "frameworks" / framework.identifier
        with(frameworkConfigDir.toFile()) {
            mkdirs()
        }

        val uploadConfigTsPath = frameworkConfigDir / "UploadConfig.ts"

        buildUploadConfig(uploadConfigTsPath)

        into.gradleInterface.executeGradleTasks(listOf(":dataland-frontend:npm_run_checkfrontendcompilation"))

        EsLintRunner(into, listOf(uploadConfigTsPath)).run()
    }
}
