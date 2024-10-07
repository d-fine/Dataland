package org.dataland.frameworktoolbox.specific.fixturegenerator

import org.dataland.frameworktoolbox.intermediate.Framework
import org.dataland.frameworktoolbox.specific.fixturegenerator.elements.FixtureSectionBuilder
import org.dataland.frameworktoolbox.utils.DatalandRepository
import org.dataland.frameworktoolbox.utils.LoggerDelegate
import org.dataland.frameworktoolbox.utils.Naming.getNameFromLabel
import org.dataland.frameworktoolbox.utils.capitalizeEn
import org.dataland.frameworktoolbox.utils.freemarker.FreeMarker
import org.dataland.frameworktoolbox.utils.typescript.EsLintPrettierRunner
import org.dataland.frameworktoolbox.utils.typescript.TypeScriptImport
import java.io.FileWriter
import java.nio.file.Path
import kotlin.io.path.div
import kotlin.io.path.notExists

/**
 * A FrameworkFixtureGeneratorBuilder converts an Intermediate-Representation framework to a fake-fixture generation
 * script and integrates the generated code into a Dataland Repository.
 * @param framework the framework DataModel to convert
 */
class FrameworkFixtureGeneratorBuilder(
    private val framework: Framework,
) {
    private val logger by LoggerDelegate()
    private val generatedTsFiles = mutableListOf<Path>()

    val rootSectionBuilder = FixtureSectionBuilder(
        parentSection = null,
        identifier = "root",
        elements = mutableListOf(),
    )

    /**
     * This data class defines a custom generator that can be used to define additional custom fake fixture
     * generators during the initial run of the framework toolbox.
     * These custom generators consist of a [className] of the data-model for which the custom generator generates the
     * data. Additionally, we can specify a [prefix] for the function that defines the custom generator. The [prefix]
     * is usually 'random' or 'guaranteed'.
     * The [rootSection] defines the structure of the fake fixture generator.
     */
    data class CustomGenerator(val className: String, val prefix: String, val rootSection: FixtureSectionBuilder)

    private val customGenerators = mutableListOf<CustomGenerator>()

    /**
     * This function adds a new custom generator that is generated during the first run of the toolbox.
     * The [className], [prefix] and [rootSection] work as described for the [CustomGenerator] class.
     */
    fun addCustomGenerator(className: String, prefix: String, rootSection: FixtureSectionBuilder) {
        customGenerators.add(CustomGenerator(className, prefix, rootSection))
    }

    private fun buildIndexTs(indexTsPath: Path) {
        val freeMarkerContext = mapOf(
            "frameworkIdentifier" to framework.identifier,
            "frameworkBaseName" to getNameFromLabel(framework.identifier).capitalizeEn(),
        )

        val freemarkerTemplate = FreeMarker.configuration
            .getTemplate("/specific/fixturegenerator/index.ts.ftl")

        val writer = FileWriter(indexTsPath.toFile())
        generatedTsFiles.add(indexTsPath)
        freemarkerTemplate.process(freeMarkerContext, writer)
        writer.close()
    }

    private fun buildPreparedFixturesTs(preparedFixturesTsPath: Path) {
        val freeMarkerContext = mapOf(
            "frameworkIdentifier" to framework.identifier,
            "frameworkBaseName" to getNameFromLabel(framework.identifier).capitalizeEn(),
        )

        val freemarkerTemplate = FreeMarker.configuration
            .getTemplate("/specific/fixturegenerator/PreparedFixtures.ts.ftl")

        if (preparedFixturesTsPath.notExists()) {
            val writer = FileWriter(preparedFixturesTsPath.toFile())
            generatedTsFiles.add(preparedFixturesTsPath)
            freemarkerTemplate.process(freeMarkerContext, writer)
            writer.close()
        }
    }

    private fun buildFrameworkGeneratorsTs(frameworkGeneratorTsPath: Path) {
        val freeMarkerContext = mapOf(
            "frameworkBaseName" to getNameFromLabel(framework.identifier).capitalizeEn(),
            "generators" to customGenerators,
        )

        val freemarkerTemplate = FreeMarker.configuration
            .getTemplate("/specific/fixturegenerator/FrameworkGenerator.ts.ftl")

        // if (frameworkGeneratorTsPath.notExists()) {
        val writer = FileWriter(frameworkGeneratorTsPath.toFile())
        generatedTsFiles.add(frameworkGeneratorTsPath)
        freemarkerTemplate.process(freeMarkerContext, writer)
        writer.close()
        // }
    }

    private fun buildDataFixtures(dataFixturesTsPath: Path) {
        val imports = rootSectionBuilder.imports +
            TypeScriptImport("generateFixtureDataset", "@e2e/fixtures/FixtureUtils") +
            TypeScriptImport(
                "type ${getNameFromLabel(framework.identifier).capitalizeEn()}" +
                    "Data",
                "@clients/backend",
            )

        val freeMarkerContext = mapOf(
            "frameworkIdentifier" to framework.identifier,
            "frameworkBaseName" to getNameFromLabel(framework.identifier).capitalizeEn(),
            "imports" to TypeScriptImport.mergeImports(imports),
            "rootSection" to rootSectionBuilder,
        )

        val freemarkerTemplate = FreeMarker.configuration
            .getTemplate("/specific/fixturegenerator/DataFixtures.ts.ftl")

        val writer = FileWriter(dataFixturesTsPath.toFile())
        generatedTsFiles.add(dataFixturesTsPath)
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
        buildDataFixtures(
            frameworkConfigDir / "${getNameFromLabel(
                framework.identifier,
            ).capitalizeEn()}DataFixtures.ts",
        )
        buildPreparedFixturesTs(
            frameworkConfigDir /
                "${getNameFromLabel(framework.identifier).capitalizeEn()}PreparedFixtures.ts",
        )
        buildFrameworkGeneratorsTs(
            frameworkConfigDir / "${getNameFromLabel(framework.identifier).capitalizeEn()}Generator.ts",
        )

        into.gradleInterface.executeGradleTasks(
            listOf(
                "dataland-frontend:npm_run_checkfakefixturecompilation",
                "dataland-frontend:npm_run_fakefixtures",
            ),
        )

        EsLintPrettierRunner(into, generatedTsFiles).run()
    }
}
