package org.dataland.documentmanager.model

import io.swagger.v3.oas.annotations.media.Schema

/**
 * An enum with the different stati of review a document has
 */
@Schema(
    enumAsRef = true,
)
enum class DocumentQAStatus {
    Pending,
    Accepted,
}