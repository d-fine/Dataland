package org.dataland.documentmanager.model

import io.swagger.v3.oas.annotations.media.Schema

/**
 * An enum with the possible types (extensions) of a document stored in the database
 */
@Schema(
    enumAsRef = true,
)
enum class DocumentType {
    Pdf, Xls, Xlsx, Ods
}
