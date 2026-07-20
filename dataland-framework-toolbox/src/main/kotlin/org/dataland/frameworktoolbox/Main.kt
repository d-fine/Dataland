package org.dataland.frameworktoolbox

/**
 * Performs the framework-specific compilation. Docker must be available.
 *
 * WARNING: WORKAROUND. SFDR labels get replaced at the end of FrameworkViewConfigBuilder.build() and FrameworkUploadConfigBuilder.build()
 * to avoid breaking the API. Once the API is updated, remove this workaround!!!
 */
fun main(args: Array<String>) {
    FrameworkToolboxCli().invoke(args)
}
