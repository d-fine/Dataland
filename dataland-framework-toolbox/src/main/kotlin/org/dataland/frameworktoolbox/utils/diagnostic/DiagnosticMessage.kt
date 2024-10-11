package org.dataland.frameworktoolbox.utils.diagnostic

/**
 * A single diagnostic message that can be uniquely identified.
 * The system is designed based on the contract that message IDs must not change between executions.
 */
data class DiagnosticMessage(
    val type: DiagnosticType,
    val id: String,
    val summary: String,
) {
    override fun toString(): String = "[${type.key}] [$id] - $summary"
}
