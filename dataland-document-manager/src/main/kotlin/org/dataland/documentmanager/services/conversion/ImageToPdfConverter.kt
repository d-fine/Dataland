package org.dataland.documentmanager.services.conversion

import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Image
import org.springframework.core.io.InputStreamResource
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

@Component
class ImageToPdfConverter : FileConverter() {
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

    override fun convertToPdf(file: MultipartFile): InputStreamResource {
        val outputStream = ByteArrayOutputStream()

        val imageData = ImageDataFactory.create(file.bytes)
        val pdfDocument = PdfDocument(PdfWriter(outputStream))
        val document = Document(pdfDocument)

        val pdfImage = Image(imageData)
        document.add(pdfImage)

        document.close()
        pdfDocument.close()

        // TODO there is probably a better way of doing this
        return InputStreamResource(ByteArrayInputStream(outputStream.toByteArray()))
    }

}