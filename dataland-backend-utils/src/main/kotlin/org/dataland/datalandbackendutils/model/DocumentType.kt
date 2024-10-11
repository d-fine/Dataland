package org.dataland.datalandbackendutils.model

import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.http.MediaType
import org.springframework.util.MimeType

/**
 * An enum with the possible types (extensions) of a document stored in the database
 */
@Schema(
    enumAsRef = true,
)
enum class DocumentType(
    val fileExtension: String,
    val mediaType: MediaType = MediaType.APPLICATION_OCTET_STREAM,
) {
    Pdf("pdf", MediaType.APPLICATION_PDF),
    Xls("xls", MediaType.asMediaType(MimeType.valueOf("application/vnd.ms-excel"))),
    Xlsx(
        "xlsx",
        MediaType.asMediaType(
            MimeType.valueOf(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            ),
        ),
    ),
    Ods("ods", MediaType.asMediaType(MimeType.valueOf("application/vnd.oasis.opendocument.spreadsheet"))),
}
