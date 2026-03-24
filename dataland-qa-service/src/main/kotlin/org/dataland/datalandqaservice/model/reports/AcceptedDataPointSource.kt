package org.dataland.datalandqaservice.model.reports

import io.swagger.v3.oas.annotations.media.Schema

/**
 * Enum representing the source from which an accepted data point was derived.
 */
@Schema(
    enumAsRef = true,
)
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
