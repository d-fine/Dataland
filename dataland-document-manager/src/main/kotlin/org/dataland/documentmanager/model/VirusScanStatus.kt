package org.dataland.documentmanager.model

import io.swagger.v3.oas.annotations.media.Schema

/**
 * The stati of a document in the virus scanning process
 */
@Schema(
    enumAsRef = true,
)
enum class VirusScanStatus {
    Pending, Approved, Rejected, InProgress, Unscanned
}
