package org.dataland.frameworktoolbox.utils

import org.gradle.tooling.GradleConnector
import org.gradle.tooling.ProjectConnection
import org.gradle.tooling.events.ProgressEvent

/**
 * The DatalandRepositoryGradleInterface manages the interaction with the
 * gradle project of a Dataland repository
 */
class DatalandRepositoryGradleInterface(
    val repository: DatalandRepository,
) {
    private val logger by LoggerDelegate()

    private fun withGradleConnection(usingFunction: (ProjectConnection) -> Unit) {
        val gradleConnector = GradleConnector.newConnector()
        val projectConnection =
            gradleConnector
                .forProjectDirectory(repository.path.toFile())

        projectConnection.connect().use(usingFunction)
    }

    /**
     * Execute a list of gradle tasks
     * @param force if true, force re-execution even if the task is up-to-date
     */
    @Suppress("SpreadOperator")
    fun executeGradleTasks(
        tasks: List<String>,
        force: Boolean = false,
    ) {
        logger.debug("Executing gradle tasks {}", tasks)
        withGradleConnection {
            val buildBuilder =
                it
                    .newBuild()
                    .forTasks(*tasks.toTypedArray())
                    .addJvmArguments("-Xmx8g")
                    .addProgressListener { update: ProgressEvent ->
                        logger.trace("GRADLE UPDATE: ${update.displayName}")
                    }.setStandardOutput(System.out)
                    .setStandardError(System.err)

            if (force) buildBuilder.addArguments("--rerun-tasks")

            buildBuilder.run()
        }
    }
}
