package org.dataland.datalandbackendutils.model

import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.http.MediaType
import org.springframework.util.MimeType

/**
 * Enum used for defining file types allowed for exporting datasets
 */
@Schema(
    enumAsRef = true,
)
enum class ExportFileType(
    val fileExtension: String,
    val mediaType: MediaType = MediaType.APPLICATION_OCTET_STREAM,
) {
    CSV("csv", MediaType.asMediaType(MimeType.valueOf("text/csv"))),
    JSON("json", MediaType.APPLICATION_JSON),
}
