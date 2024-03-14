
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import org.apache.poi.xwpf.usermodel.XWPFDocument
import org.apache.poi.xwpf.usermodel.XWPFParagraph
import org.apache.poi.xwpf.usermodel.XWPFRun
import org.dataland.documentmanager.services.conversion.FileConverter
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import java.io.ByteArrayOutputStream

@Component
class DocxToPdfConverter: FileConverter() {
    override val logger: Logger = LoggerFactory.getLogger(javaClass)
    private final val imageMimeTypes = setOf(
        "application/msword",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
         )
    override val allowedMimeTypesPerFileExtension: Map<String, Set<String>> = mapOf(
        "doc" to imageMimeTypes,
        "docx" to imageMimeTypes,

        )
    override fun convertToPdf(file: MultipartFile): ByteArray {
        val outputStream = ByteArrayOutputStream()

        val doc = XWPFDocument(file.inputStream)
        val pdfDocument = PdfDocument(PdfWriter(outputStream))
        val document = Document(pdfDocument)

        for (paragraph: XWPFParagraph in doc.paragraphs) {
            val text = StringBuilder()
            for (run: XWPFRun in paragraph.runs) {
                text.append(run.text())
            }
            document.add(Paragraph(text.toString()))
        }
        document.close()
        pdfDocument.close()

        return outputStream.toByteArray()
    }

}