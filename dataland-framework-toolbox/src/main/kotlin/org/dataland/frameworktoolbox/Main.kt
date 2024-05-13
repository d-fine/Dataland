package org.dataland.frameworktoolbox
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.dataland.frameworktoolbox.frameworks.PavedRoadFramework
import org.dataland.frameworktoolbox.utils.DatalandRepository
import org.springframework.beans.factory.getBeansOfType
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import java.io.File
import java.nio.file.Path

/**
 * Performs the framework-specific compilation
 */
fun main(args: Array<String>) {
    val context = AnnotationConfigApplicationContext(SpringConfig::class.java)
    val allPavedRoadFrameworks = context.getBeansOfType<PavedRoadFramework>().values.toList()
    val datalandProject = DatalandRepository(Path.of("./"))

    require(args.isNotEmpty()) {
        "Please specify one argument: The name of the framework to convert, 'all' for converting all frameworks, " +
            "or 'list' for listing all available frameworks"
    }
    val command = args[0]

    when (command) {
        "all" -> {
            require(args.size == 1) { "Command 'all' does not support more than one argument" }
            allPavedRoadFrameworks.forEach {
                it.compileFramework(datalandProject)
            }
        }
        "list" -> {
            require(args.size <= 2) { "Command 'list' does not support more than two arguments" }
            val om = jacksonObjectMapper()
            val allFrameworkIdentifiers = allPavedRoadFrameworks.map { it.identifier }
            println(om.writeValueAsString(allFrameworkIdentifiers))
            if (args.size >= 2) {
                om.writeValue(File(args[1]), allFrameworkIdentifiers)
            }
        }
        else -> {
            require(args.size == 1) { "Command 'build' does not support more than one argument" }
            val foundFramework = allPavedRoadFrameworks.find { it.identifier == args[0] }
            requireNotNull(foundFramework) {
                "Could not find framework with identifier ${args[0]}"
            }
            foundFramework.compileFramework(datalandProject)
        }
    }
}
