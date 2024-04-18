package org.dataland.frameworktoolbox.specific.frameworkregistryimports

import org.dataland.frameworktoolbox.utils.DatalandRepository
import org.dataland.frameworktoolbox.utils.Naming.getNameFromLabel
import org.dataland.frameworktoolbox.utils.freemarker.FreeMarker
import org.dataland.frameworktoolbox.utils.typescript.EsLintRunner
import java.io.FileWriter
import java.nio.file.Path
import kotlin.io.path.div

/**
 * The FrameworkRegistryImportsUpdater generates the FrameworkRegistryImports.ts file in the dataland repository
 */
class FrameworkRegistryImportsUpdater {
    /**
     * Generate the FrameworkRegistryImports.ts file in the dataland frontend
     */
    fun update(repository: DatalandRepository) {
        val pathToFrameworkDirectory = repository.frontendSrc / "frameworks"

        val allRegisteredFrameworks = pathToFrameworkDirectory.toFile().listFiles {
                file ->
            file.isDirectory && file.listFiles()?.any { it.name == "BaseFrameworkDefinition.ts" } ?: false
        }!!

        val freeMarkerContext = mapOf(
            "frameworks" to allRegisteredFrameworks
                .sortedBy { it.name }
                .map {
                    mapOf(
                        "identifier" to it.name,
                        "baseNameInCamelCase" to getNameFromLabel(it.name),
                    )
                },
        )
        writeIntoRegistryTsFiles(repository, freeMarkerContext)
    }

    private fun writeIntoRegistryTsFiles(repository: DatalandRepository, freeMarkerContext: Any) {
        val pathToFrameworkDirectory = repository.frontendSrc / "frameworks"
        val jobs = listOf(
            Pair(
                "/specific/frameworkregistryimports/BaseFrameworkRegistryImports.ts.ftl",
                pathToFrameworkDirectory / "BaseFrameworkRegistryImports.ts",
            ),
            Pair(
                "/specific/frameworkregistryimports/FrontendFrameworkRegistryImports.ts.ftl",
                pathToFrameworkDirectory / "FrontendFrameworkRegistryImports.ts",
            ),
        )

        val generatedTsFiles = mutableListOf<Path>()
        for ((templateName, outputPath) in jobs) {
            generatedTsFiles.add(outputPath.toAbsolutePath())
            val template = FreeMarker.configuration
                .getTemplate(templateName)
            val writer = FileWriter(outputPath.toFile())
            template.process(freeMarkerContext, writer)
            writer.close()
        }
        EsLintRunner(repository, generatedTsFiles).run()
    }
}
