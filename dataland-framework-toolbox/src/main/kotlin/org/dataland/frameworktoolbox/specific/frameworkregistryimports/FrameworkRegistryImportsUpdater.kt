package org.dataland.frameworktoolbox.specific.frameworkregistryimports

import org.dataland.frameworktoolbox.utils.DatalandRepository
import org.dataland.frameworktoolbox.utils.freemarker.FreeMarker
import java.io.FileWriter
import kotlin.io.path.div

class FrameworkRegistryImportsUpdater {
    fun update(repository: DatalandRepository) {
        val pathToFrameworkDirectory = repository.frontendSrc / "frameworks"

        val allRegisteredFrameworks = pathToFrameworkDirectory.toFile().listFiles {
            file -> file.isDirectory
        }!!

        val freeMarkerContext = mapOf(
            "frameworks" to allRegisteredFrameworks.map { it.name },
        )

        val freemarkerTemplate = FreeMarker.configuration
            .getTemplate("/specific/frameworkregistryimports/FrameworkRegistryImports.ts.ftl")

        val writer = FileWriter((pathToFrameworkDirectory / "FrameworkRegistryImports.ts").toFile())
        freemarkerTemplate.process(freeMarkerContext, writer)
        writer.close()
    }
}