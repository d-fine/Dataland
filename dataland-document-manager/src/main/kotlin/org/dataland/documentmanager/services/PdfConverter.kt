package org.dataland.documentmanager.services

import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Image
import com.itextpdf.layout.element.Paragraph
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.FileReader
import org.apache.pdfbox.Loader
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import java.io.IOException
import java.io.InputStreamReader
import org.apache.tika.Tika
import org.springframework.core.io.InputStreamResource

/**
 * A service for converting various file types into PDFs
 */
@Component
class PdfConverter {
    private val logger = LoggerFactory.getLogger(javaClass)

    fun convertImage(image: MultipartFile, correlationId: String): InputStreamResource {
        logger.info("Converting ${image.name} with correlation Id $correlationId to PDF.")
        val outputStream = ByteArrayOutputStream()

        val imageData = ImageDataFactory.create(image.bytes)
        val pdfDocument = PdfDocument(PdfWriter(outputStream))
        val document = Document(pdfDocument)

        val pdfImage = Image(imageData)
        document.add(pdfImage)

        document.close()
        pdfDocument.close()

        // TODO there is probably a better way of doing this
        return InputStreamResource(ByteArrayInputStream(outputStream.toByteArray()))
    }

    fun convertLineByLine(file: MultipartFile, correlationId: String): InputStreamResource {
        val outputStream = ByteArrayOutputStream()
        val pdfDocument = PdfDocument(PdfWriter(outputStream))
        val document = Document(pdfDocument)

        BufferedReader(InputStreamReader(file.inputStream)).use { reader ->
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                document.add(Paragraph(line))
            }
        }

        document.close()
        pdfDocument.close()

        return InputStreamResource(ByteArrayInputStream(outputStream.toByteArray()))
    }

    fun convertWordDocument(file: MultipartFile, correlationId: String) {
        TODO("Word conversion not implemented")
    }
    fun convertPowerpoint(file: MultipartFile, correlationId: String) {
        TODO("Powerpoint conversion not implemented")
    }
}
