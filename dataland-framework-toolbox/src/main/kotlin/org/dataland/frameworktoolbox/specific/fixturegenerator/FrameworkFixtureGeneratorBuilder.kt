package org.dataland.frameworktoolbox.specific.fixturegenerator

import org.dataland.frameworktoolbox.intermediate.Framework
import org.dataland.frameworktoolbox.specific.fixturegenerator.elements.FixtureSectionBuilder
import org.dataland.frameworktoolbox.utils.DatalandRepository
import org.dataland.frameworktoolbox.utils.LoggerDelegate
import org.dataland.frameworktoolbox.utils.capitalizeEn
import org.dataland.frameworktoolbox.utils.freemarker.FreeMarker
import java.io.FileWriter
import java.nio.file.Path
import kotlin.io.path.div

class FrameworkFixtureGeneratorBuilder(
    private val framework: Framework,
) {
    private val logger by LoggerDelegate()

    val rootSectionBuilder = FixtureSectionBuilder(
        parentSection = null,
        identifier = "root",
        elements = mutableListOf(),
    )

    private fun buildIndexTs(indexTsPath: Path) {
        val freeMarkerContext = mapOf(
            "frameworkIdentifier" to framework.identifier,
        )

        val freemarkerTemplate = FreeMarker.configuration
            .getTemplate("/specific/fixturegenerator/index.ts.ftl")

        val writer = FileWriter(indexTsPath.toFile())
        freemarkerTemplate.process(freeMarkerContext, writer)
        writer.close()
    }

    private fun buildDataFixtures(dataFixturesTsPath: Path) {
        val freeMarkerContext = mapOf(
            "frameworkIdentifier" to framework.identifier,
            "imports" to rootSectionBuilder.imports,
            "rootSection" to rootSectionBuilder,
        )

        val freemarkerTemplate = FreeMarker.configuration
            .getTemplate("/specific/fixturegenerator/DataFixtures.ts.ftl")

        val writer = FileWriter(dataFixturesTsPath.toFile())
        freemarkerTemplate.process(freeMarkerContext, writer)
        writer.close()
    }

    /**
     * Generate the code for the ViewConfig and integrates it into the Dataland Repository
     */
    fun build(into: DatalandRepository) {
        logger.info("Starting to build the fixture generator into the dataland-repository at ${into.path}")

        val frameworkConfigDir = into.path / "dataland-frontend" / "tests" /
            "e2e" / "fixtures" / "frameworks" / framework.identifier

        frameworkConfigDir.toFile().mkdirs()

        buildIndexTs(frameworkConfigDir / "index.ts")
        buildDataFixtures(frameworkConfigDir / "${framework.identifier.capitalizeEn()}DataFixtures.ts")
    }
}
