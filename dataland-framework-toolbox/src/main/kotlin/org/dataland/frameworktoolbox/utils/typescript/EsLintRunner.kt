package org.dataland.frameworktoolbox.utils.typescript

import org.dataland.frameworktoolbox.utils.DatalandRepository
import java.nio.file.Path
import java.util.concurrent.TimeUnit
import kotlin.io.path.div

/**
 * An interface class for running EsLint on a pre-determined set of files
 */
class EsLintRunner(val repository: DatalandRepository, val files: List<Path>) {
    companion object {
        private const val ESLINT_TIMEOUT = 60L
    }

    /**
     * Run EsLint on the configured files
     */
    fun run() {
        require(files.isNotEmpty()) {
            "You must call EsLint with at least one file to run it on."
        }

        val npxCommand = if (System.getProperty("os.name").contains("windows", true)) {
            "npx.cmd"
        } else {
            "npx"
        }

        val argumentList = mutableListOf(npxCommand, "eslint", "--fix")

        argumentList.addAll(files.map { it.toAbsolutePath().toString() })

        ProcessBuilder(*argumentList.toTypedArray())
            .directory((repository.path / "dataland-frontend").toFile())
            .redirectOutput(ProcessBuilder.Redirect.INHERIT)
            .redirectError(ProcessBuilder.Redirect.INHERIT)
            .start()
            .waitFor(ESLINT_TIMEOUT, TimeUnit.SECONDS)
    }
}
