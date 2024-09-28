package org.dataland.documentmanager.services.conversion

import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.InputStreamReader

/**
 * A converter for txt files to the pdf format
 */
@Component
class TextToPdfConverter :
    FileConverter(
        allowedMimeTypesPerFileExtension =
            mapOf(
                "txt" to setOf("text/plain"),
            ),
    ) {
    override val logger: Logger = LoggerFactory.getLogger(javaClass)

    override fun validateFileContent(
        file: MultipartFile,
        correlationId: String,
    ) {
        if (file.bytes.decodeToString().isBlank()) {
            throw InvalidInputApiException(
                fileIsEmptySummary,
                fileIsEmptyMessage,
            )
        }
    }

    override fun convert(
        file: MultipartFile,
        correlationId: String,
    ): ByteArray {
        logger.info("Converting plain text file to pdf document.(correlation ID: $correlationId)")
        val outputStream = ByteArrayOutputStream()
        val pdfDocument = PdfDocument(PdfWriter(outputStream))
        val document = Document(pdfDocument)

        file.inputStream.use { inputStream ->
            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    document.add(Paragraph(line))
                }
            }
        }

        document.close()
        pdfDocument.close()

        return outputStream.toByteArray()
    }
}
