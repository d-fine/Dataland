package org.dataland.frameworktoolbox.utils.diagnostic

import org.slf4j.event.Level

/**
 * A DiagnosticType describes how a diagnostic message should be handled by the manager
 */
enum class DiagnosticType(
    val key: String,
    val logLevel: Level,
    val errorImmediatelyAfter: Boolean,
    val errorAtEnd: Boolean,
    val canBeSuppressed: Boolean,
) {
    INFO("INFO", Level.INFO, false, false, true),
    WARNING("WARNING", Level.WARN, false, true, true),
    CRITICAL("CRITICAL", Level.ERROR, true, true, false),
}
