package org.dataland.frameworktoolbox
import org.dataland.frameworktoolbox.frameworks.PavedRoadFramework
import org.dataland.frameworktoolbox.utils.DatalandRepository
import org.springframework.beans.factory.getBeansOfType
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import java.nio.file.Path


fun main(args: Array<String>) {
    val context = AnnotationConfigApplicationContext(SpringConfig::class.java)
    val allPavedRoadFrameworks = context.getBeansOfType<PavedRoadFramework>().values.toList()
    val datalandProject = DatalandRepository(Path.of("./"))

    require(args.size == 1) {
        "Please specify one argument: The name of the framework to convert, or 'all' for converting all frameworks"
    }

    if (args[0] == "all") {
        allPavedRoadFrameworks.forEach {
            it.compileFramework(datalandProject)
        }
    } else {
        val foundFramework = allPavedRoadFrameworks.find { it.identifier == args[0] }
        requireNotNull(foundFramework) {
            "Could not find framework with identifier ${args[0]}"
        }
        foundFramework.compileFramework(datalandProject)
    }
}
