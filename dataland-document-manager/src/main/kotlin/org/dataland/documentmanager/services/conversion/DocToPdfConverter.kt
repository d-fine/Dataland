
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import org.apache.tika.metadata.Metadata
import org.apache.tika.parser.AutoDetectParser
import org.apache.tika.sax.BodyContentHandler
import org.apache.tika.sax.XHTMLContentHandler
import org.dataland.documentmanager.services.conversion.FileConverter
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import java.io.ByteArrayOutputStream

@Component
class DocToPdfConverter: FileConverter() {
    override val logger: Logger = LoggerFactory.getLogger(javaClass)
    private final val docMimeTypes = setOf(
        "application/msword"
    )
    override val allowedMimeTypesPerFileExtension: Map<String, Set<String>> = mapOf(
        "doc" to docMimeTypes
        )
    override fun convertToPdf(file: MultipartFile): ByteArray {
        val outputStream = ByteArrayOutputStream()

        //val doc =
        val pdfDocument = PdfDocument(PdfWriter(outputStream))
        val document = Document(pdfDocument)

        val parser = AutoDetectParser()
        val metadata = Metadata()
        val handler = BodyContentHandler()
        val contentHandler = XHTMLContentHandler(handler, metadata)

        val input = file.inputStream

        parser.parse(input, contentHandler, metadata)

        val paragraphs = handler.toString().split("\n\n")
        for (paragraphText in paragraphs) {
            document.add(Paragraph(paragraphText))
        }

        document.close()
        pdfDocument.close()

        return outputStream.toByteArray()
    }

}