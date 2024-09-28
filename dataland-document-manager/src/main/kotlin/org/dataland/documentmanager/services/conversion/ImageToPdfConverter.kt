package org.dataland.documentmanager.services.conversion

import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Image
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import java.io.ByteArrayOutputStream

/**
 * A converter for multiple image types to the pdf format
 */
@Component
class ImageToPdfConverter :
    FileConverter(
        allowedMimeTypesPerFileExtension =
            mapOf(
                "png" to imageMimeTypes,
                "jpg" to imageMimeTypes,
                "jpeg" to imageMimeTypes,
                "jpe" to imageMimeTypes,
                "jxr" to imageMimeTypes,
                "tif" to imageMimeTypes,
                "tiff" to imageMimeTypes,
                "heic" to imageMimeTypes,
                "heif" to imageMimeTypes,
            ),
    ) {
    override val logger: Logger = LoggerFactory.getLogger(javaClass)

    override fun convert(
        file: MultipartFile,
        correlationId: String,
    ): ByteArray {
        logger.info("Converting image to a pdf document. (correlation ID: $correlationId)")
        val outputStream = ByteArrayOutputStream()

        val imageData = ImageDataFactory.create(file.bytes)
        val pdfDocument = PdfDocument(PdfWriter(outputStream))
        val document = Document(pdfDocument)

        val pdfImage = Image(imageData)
        document.add(pdfImage)

        document.close()
        pdfDocument.close()

        return outputStream.toByteArray()
    }
}

private val imageMimeTypes =
    setOf(
        "image/png",
        "image/jpeg", "image/vnd.ms-photo", "image/jxr",
        "image/tiff",
        "image/tiff-fx",
        "image/heif",
        "image/heif-sequence",
        "image/heic",
        "image/heic-sequence",
        "image/avif",
    )
