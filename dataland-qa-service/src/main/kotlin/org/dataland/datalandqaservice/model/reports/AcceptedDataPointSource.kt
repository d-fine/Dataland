package org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.reports

/**
 * Enum representing the source from which an accepted data point was derived.
 */
enum class AcceptedDataPointSource {
    /**
     * The original data point was accepted.
     */
    Original,

    /**
     * The data point suggested by the QA report was accepted.
     */
    Qa,

    /**
     * A custom data point was accepted.
     */
    Custom,
}
