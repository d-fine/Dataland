package org.dataland.documentmanager.services.conversion

import org.apache.poi.xslf.usermodel.XMLSlideShow
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.jodconverter.core.document.DefaultDocumentFormatRegistry
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile

/**
 * Converts a pptx file to a pdf file
 */
@Component
class PptxToPdfConverter(
    @Value("\${dataland.libreoffice.path}")
    val pathToLibre: String,
) : MsOfficeToPdfConverterBase(
        converterSourceType = DefaultDocumentFormatRegistry.PPTX,
        pathToLibre = pathToLibre,
        allowedMimeTypesPerFileExtension =
            mapOf(
                "pptx" to
                    setOf(
                        "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                        "application/x-tika-ooxml",
                    ),
            ),
    ) {
    override val logger: Logger = LoggerFactory.getLogger(javaClass)

    override fun validateFileContent(
        file: MultipartFile,
        correlationId: String,
    ) {
        file.inputStream.use { inputStream ->
            XMLSlideShow(inputStream).use { slideShow ->
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
