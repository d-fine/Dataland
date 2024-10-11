package org.dataland.frameworktoolbox.utils.diagnostic

import org.dataland.frameworktoolbox.utils.LoggerDelegate
import org.springframework.stereotype.Component
import java.lang.StringBuilder

/**
 * The DiagnosticManager handles DiagnosticMessages. A DiagnosticMessage is information that is of interest
 * to the developer and may contain code-smells. It is designed to be used for suppressible error messages that
 * developers should take into account, but can also ignore if they deem appropriate.
 */
@Component
class DiagnosticManager {
    private val logger by LoggerDelegate()
    private val messageLog = mutableListOf<DiagnosticMessage>()

    private val loggedMessageIds = mutableSetOf<String>()
    private val messageIdsThatHaveBeenSuppressed = mutableSetOf<String>()
    private val messageIdsThatShouldBeSuppressed = mutableSetOf<String>()

    private fun logMessageToSlf4j(message: DiagnosticMessage) {
        logger
            .makeLoggingEventBuilder(message.type.logLevel)
            .log(message.toString())
    }

    private fun assertMessageIdHasNotBeenSeenBefore(message: DiagnosticMessage) {
        val messageIdHasBeenSeenBefore = loggedMessageIds.contains(message.id)

        if (messageIdHasBeenSeenBefore) {
            logMessageToSlf4j(message)
            throw IllegalStateException("Diagnostic id ${message.id} not unique! (Has been seen before)")
        }
    }

    private fun assertThatThereAreNoUnnecessarilySuppressedMessages() {
        val messageIdsThatWereUselesslySuppressed = messageIdsThatShouldBeSuppressed - messageIdsThatHaveBeenSuppressed

        if (messageIdsThatWereUselesslySuppressed.isNotEmpty()) {
            logger.error(
                "⚠\uFE0F There are ${messageIdsThatWereUselesslySuppressed.size} " +
                    "messages that were suppressed but they were never logged: $messageIdsThatShouldBeSuppressed",
            )
            throw IllegalStateException(
                "There are ${messageIdsThatWereUselesslySuppressed.size} that were unnecessarily suppressed",
            )
        }
    }

    private fun buildFailingDiagnosticSummary(failingDiagnostics: List<DiagnosticMessage>): String {
        val failingDiagnosticMessageSummary = StringBuilder()
        failingDiagnosticMessageSummary.appendLine(
            "⚠\uFE0F There are ${failingDiagnostics.size} failing diagnostic entries:",
        )

        failingDiagnostics.forEachIndexed { i, it ->
            failingDiagnosticMessageSummary.appendLine("${i + 1}:  $it")
            if (it.type.canBeSuppressed) {
                failingDiagnosticMessageSummary.appendLine(
                    "\t You may suppress this diagnostic (with a good justification) by calling " +
                        "`Diagnostic.suppress(\"${it.id}\")` BEFORE the diagnostic is logged",
                )
            }
        }
        return failingDiagnosticMessageSummary.toString()
    }

    /**
     * Marks a message-id as "suppressed". Suppressed messages do not appear in the log
     * and do not cause exceptions (unless they were marked as un-suppressible)
     */
    fun suppress(id: String) {
        messageIdsThatShouldBeSuppressed.add(id)
    }

    private fun register(message: DiagnosticMessage) {
        assertMessageIdHasNotBeenSeenBefore(message)
        loggedMessageIds.add(message.id)

        if (messageIdsThatShouldBeSuppressed.contains(message.id)) {
            messageIdsThatHaveBeenSuppressed.add(message.id)
            require(message.type.canBeSuppressed) { "Diagnostic cannot be suppressed: $message" }
            return
        }

        logMessageToSlf4j(message)

        messageLog.add(message)

        check(!message.type.errorImmediatelyAfter) { "Critical Diagnostic Error: $message" }
    }

    private fun reset() {
        messageLog.clear()
        loggedMessageIds.clear()
        messageIdsThatHaveBeenSuppressed.clear()
        messageIdsThatShouldBeSuppressed.clear()
    }

    /**
     * Logs a info diagnostic that just appears in the log and otherwise does nothing.
     * @param id A unique identifier for the diagnostic. Must be unique. Should be as stable as possible.
     * @param summary A short summary of the diagnostic.
     */
    fun info(
        id: String,
        summary: String,
    ) = register(DiagnosticMessage(DiagnosticType.INFO, id, summary))

    /**
     * Logs a warning diagnostic that appears in the log, and must be suppressed or causes an exception.
     * @param id A unique identifier for the diagnostic. Must be unique. Should be as stable as possible.
     * @param summary A short summary of the diagnostic.
     */
    fun warning(
        id: String,
        summary: String,
    ) = register(DiagnosticMessage(DiagnosticType.WARNING, id, summary))

    /**
     * Logs a warning diagnostic that appears in the log if the condition is true,
     * and must be suppressed or causes an exception.
     * @param id A unique identifier for the diagnostic. Must be unique. Should be as stable as possible.
     * @param summary A short summary of the diagnostic.
     */
    fun warnIf(
        condition: Boolean,
        id: String,
        summary: String,
    ) {
        if (condition) {
            warning(id, summary)
        }
    }

    /**
     * Logs a critical diagnostic that appears immediately stops execution and cannot be suppressed
     * @param id A unique identifier for the diagnostic. Must be unique. Should be as stable as possible.
     * @param summary A short summary of the diagnostic.
     */
    fun critical(
        id: String,
        summary: String,
    ) = register(DiagnosticMessage(DiagnosticType.CRITICAL, id, summary))

    /**
     * Finalizes the logging manager, resetting its state and printing a summary statistic.
     */
    fun finalizeDiagnosticStream() {
        val failingDiagnostics = messageLog.filter { it.type.errorAtEnd }

        if (failingDiagnostics.isNotEmpty()) {
            logger.error(buildFailingDiagnosticSummary(failingDiagnostics))
            throw AssertionError("There are ${failingDiagnostics.size} failing diagnostic entries")
        } else {
            logger.info("\uD83E\uDD73 Great job! There are no failing diagnostic entries \uD83C\uDF89")
        }

        assertThatThereAreNoUnnecessarilySuppressedMessages()
        reset()
    }
}
