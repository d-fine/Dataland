package org.dataland.frameworktoolbox

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.dataland.frameworktoolbox.frameworks.PavedRoadFramework
import org.dataland.frameworktoolbox.utils.DatalandRepository
import org.springframework.beans.factory.getBeansOfType
import org.springframework.context.annotation.AnnotationConfigApplicationContext
import java.io.File
import java.nio.file.Path

/**
 * An Entrypoint for the Framework Toolbox Command-Line-Interface
 */
class FrameworkToolboxCli {
    private val context = AnnotationConfigApplicationContext(SpringConfig::class.java)
    private val allPavedRoadFrameworks = context.getBeansOfType<PavedRoadFramework>().values.toList()

    private val datalandProject = DatalandRepository(Path.of("./"))

    /**
     * Invoke the CLI
     * @param args the arguments passed as inputs
     */
    fun invoke(args: Array<String>) {
        require(args.isNotEmpty()) {
            "Please specify one argument: The name of the framework to convert, 'all' for converting all frameworks, " +
                "or 'list' for listing all available frameworks"
        }
        val command = args[0]

        when (command) {
            "all" -> buildAllFrameworks(args)
            "list" -> listAllFrameworks(args)
            else -> buildSingleFramework(args)
        }
    }

    private fun buildAllFrameworks(args: Array<String>) {
        require(args.size == 1) { "Command 'all' does not support more than one argument" }
        allPavedRoadFrameworks.forEach {
            it.compileFramework(datalandProject)
        }
    }

    private fun buildSingleFramework(args: Array<String>) {
        require(args.size == 1) { "Command 'build' does not support more than one argument" }
        val foundFramework = allPavedRoadFrameworks.find { it.identifier == args[0] }
        requireNotNull(foundFramework) {
            "Could not find framework with identifier ${args[0]}"
        }
        foundFramework.compileFramework(datalandProject)
    }

    private fun listAllFrameworks(args: Array<String>) {
        require(args.size <= 2) { "Command 'list' does not support more than two arguments" }
        val om = jacksonObjectMapper()
        val allFrameworkIdentifiers = allPavedRoadFrameworks.map { it.identifier }
        println(om.writeValueAsString(allFrameworkIdentifiers))
        if (args.size >= 2) {
            om.writeValue(File(args[1]), allFrameworkIdentifiers)
        }
    }
}
