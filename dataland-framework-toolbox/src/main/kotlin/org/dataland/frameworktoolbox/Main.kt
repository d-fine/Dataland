package org.dataland.frameworktoolbox

/**
 * Performs the framework-specific compilation. Docker must be available.
 */
fun main(args: Array<String>) {
    FrameworkToolboxCli().invoke(args)
}
