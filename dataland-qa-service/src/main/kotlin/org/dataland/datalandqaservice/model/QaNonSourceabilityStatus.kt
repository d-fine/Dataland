package org.dataland.datalandqaservice.org.dataland.datalandqaservice.model

import io.swagger.v3.oas.annotations.media.Schema

/**
 * An enum representing the QA status for non-sourceability requests.
 * Pending: Request created, awaits QA review
 * Accepted: QA reviewer accepted the non-sourceability claim
 * Rejected: QA reviewer rejected the non-sourceability claim
 */
@Schema(
    enumAsRef = true,
)
enum class QaNonSourceabilityStatus { Pending, Accepted, Rejected }
