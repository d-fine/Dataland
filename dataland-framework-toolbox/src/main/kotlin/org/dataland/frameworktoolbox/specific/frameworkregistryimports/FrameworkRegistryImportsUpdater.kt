package org.dataland.frameworktoolbox.specific.frameworkregistryimports

import org.dataland.frameworktoolbox.utils.DatalandRepository
import org.dataland.frameworktoolbox.utils.Naming.getNameFromLabel
import org.dataland.frameworktoolbox.utils.freemarker.FreeMarker
import org.dataland.frameworktoolbox.utils.typescript.EsLintPrettierRunner
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
        val allRegisteredFrameworks =
            pathToFrameworkDirectory.toFile().listFiles { file ->
                file.isDirectory && file.listFiles()?.any { it.name == "BaseFrameworkDefinition.ts" } ?: false
            }!!
        val allRegisteredPrivateFrameworks =
            allRegisteredFrameworks
                .filter {
                    (it.toPath() / "BaseFrameworkDefinition.ts")
                        .toFile()
                        .readText()
                        .contains("implements BasePrivateFrameworkDefinition")
                }.map { it.name }

        val freeMarkerContextForAllFrameworks =
            mapOf(
                "frameworks" to
                    allRegisteredFrameworks
                        .sortedBy { it.name }
                        .map {
                            mapOf(
                                "identifier" to it.name,
                                "baseNameInCamelCase" to getNameFromLabel(it.name),
                            )
                        },
            )
        val publicFrameworkIdentifier =
            allRegisteredFrameworks
                .map { it.name }
                .subtract(allRegisteredPrivateFrameworks.toSet())
                .toList()
        writeAllTestFiles(
            repository, allRegisteredPrivateFrameworks, freeMarkerContextForAllFrameworks,
            publicFrameworkIdentifier,
        )
    }

    private fun writeAllTestFiles(
        repository: DatalandRepository,
        privateFrameworkNames: List<String>,
        freeMarkerContextForAllFrameworks: Map<String, List<Map<String, String>>>,
        publicFrameworkNames: List<String>,
    ) {
        val freemarkerContextForAllPrivateFrameworks =
            createFreeMarkerContextFile(
                privateFrameworkNames, "privateFrameworks",
            )
        val freemarkerContextForPublicFrameworks =
            createFreeMarkerContextFile(
                publicFrameworkNames, "publicFrameworks",
            )
        val frontendFrameworkRegistryImportsTemplateNames =
            Pair(
                "FrontendFrameworkRegistryImports.ts.ftl", "FrontendFrameworkRegistryImports.ts",
            )
        val basePrivateFrameworkregistryImportsTemplateNames =
            Pair(
                "BasePrivateFrameworkRegistryImports.ts.ftl", "BasePrivateFrameworkRegistryImports.ts",
            )
        val basePublicFrameworkregistryImportsTemplateNames =
            Pair(
                "BasePublicFrameworkRegistryImports.ts.ftl", "BasePublicFrameworkRegistryImports.ts",
            )
        writeIntoRegistryTsFiles(
            repository, freeMarkerContextForAllFrameworks, frontendFrameworkRegistryImportsTemplateNames,
        )
        writeIntoRegistryTsFiles(
            repository, freemarkerContextForAllPrivateFrameworks, basePrivateFrameworkregistryImportsTemplateNames,
        )
        writeIntoRegistryTsFiles(
            repository, freemarkerContextForPublicFrameworks, basePublicFrameworkregistryImportsTemplateNames,
        )
    }

    private fun createFreeMarkerContextFile(
        frameworkList: List<String>,
        argumentName: String,
    ): Map<String, List<Map<String, String>>> {
        val freemarkerContextFile =
            mapOf(
                argumentName to
                    frameworkList
                        .sorted()
                        .map {
                            mapOf(
                                "identifier" to it,
                                "baseNameInCamelCase" to getNameFromLabel(it),
                            )
                        },
            )
        return freemarkerContextFile
    }

    private fun writeIntoRegistryTsFiles(
        repository: DatalandRepository,
        freeMarkerContext: Any,
        templateNames: Pair<String, String>,
    ) {
        val pathToFrameworkDirectory = repository.frontendSrc / "frameworks"
        val jobs =
            listOf(
                Pair(
                    "/specific/frameworkregistryimports/" + templateNames.first,
                    pathToFrameworkDirectory / templateNames.second,
                ),
            )

        val generatedTsFiles = mutableListOf<Path>()
        for ((templateName, outputPath) in jobs) {
            generatedTsFiles.add(outputPath.toAbsolutePath())
            val template =
                FreeMarker.configuration
                    .getTemplate(templateName)
            val writer = FileWriter(outputPath.toFile())
            template.process(freeMarkerContext, writer)
            writer.close()
        }
        EsLintPrettierRunner(repository, generatedTsFiles).run()
    }
}
