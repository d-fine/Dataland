package org.dataland.documentmanager.services.conversion

import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Image
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.core.io.InputStreamResource
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

@Component
class ImageToPdfConverter : FileConverter() {
    override val logger: Logger = LoggerFactory.getLogger(javaClass)
    private final val imageMimeTypes = setOf("image/png", "image/jpeg", "image/tiff", "image/heif", "image/heic")
    override val allowedMimeTypesPerFileExtension: Map<String, Set<String>> = mapOf(
        "png" to imageMimeTypes,
        "jpg" to imageMimeTypes,
        "jpeg" to imageMimeTypes,
        "jpe" to imageMimeTypes,
        "jxr" to imageMimeTypes,
        "tif" to imageMimeTypes,
        "tiff" to imageMimeTypes,
        "heic" to imageMimeTypes,
        "heif" to imageMimeTypes
    )

    override fun convertToPdf(file: MultipartFile): ByteArray {
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