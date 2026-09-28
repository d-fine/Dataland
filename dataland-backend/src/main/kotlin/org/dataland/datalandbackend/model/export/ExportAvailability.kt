package org.dataland.datalandbackend.model.export

import com.fasterxml.jackson.annotation.JsonValue

/**
 * Describes whether a data dimension (company, dataType, reportingPeriod) that is part of an export
 * has an available dataset or is confirmed as non-sourceable.
 *
 * This is unrelated to the request-level DataSourcingState (NonSourceable/NonSourceableVerification), which
 * describes the outcome of one specific request. This enum instead describes a fact about the data dimension
 * itself, independent of whether any request exists for it.
 */
enum class ExportAvailability(
    @JsonValue val value: String,
) {
    /** A dataset was found for the data dimension. */
    AVAILABLE("available"),

    /** No dataset was found for the data dimension, but it is confirmed as non-sourceable. */
    NON_SOURCEABLE("non-sourceable"),
}
