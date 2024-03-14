import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Image
import com.itextpdf.layout.element.Paragraph
import org.apache.poi.xslf.usermodel.XMLSlideShow
import org.apache.poi.xslf.usermodel.XSLFPictureShape
import org.apache.poi.xslf.usermodel.XSLFSlide
import org.apache.poi.xslf.usermodel.XSLFTextShape
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


        val pdfDocument = PdfDocument(PdfWriter(outputStream))
        val document = Document(pdfDocument)



        document.close()
        pdfDocument.close()

        return outputStream.toByteArray()
    }

}