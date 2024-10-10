package org.dataland.documentmanager.services.conversion

import org.apache.poi.hslf.usermodel.HSLFSlideShow
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.jodconverter.core.document.DefaultDocumentFormatRegistry
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile

/**
 * Converts a ppt file to a pdf file
 */
@Component
class PptToPdfConverter(
    @Value("\${dataland.libreoffice.path}")
    val pathToLibre: String,
) : MsOfficeToPdfConverterBase(
        converterSourceType = DefaultDocumentFormatRegistry.PPT,
        pathToLibre = pathToLibre,
        allowedMimeTypesPerFileExtension =
            mapOf(
                "ppt" to
                    setOf(
                        "application/vnd.ms-powerpoint",
                        "application/x-tika-msoffice",
                    ),
            ),
    ) {
    override val logger: Logger = LoggerFactory.getLogger(javaClass)

    override fun validateFileContent(
        file: MultipartFile,
        correlationId: String,
    ) {
        file.inputStream.use { inputStream ->
            HSLFSlideShow(inputStream).use { slideShow ->
                if (slideShow.slides.isEmpty()) {
                    throw InvalidInputApiException(
                        fileIsEmptySummary,
                        fileIsEmptyMessage,
                    )
                }
            }
        }
    }
}
