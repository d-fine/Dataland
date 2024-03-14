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
class PowerPointToPdfConverter : FileConverter() {
    override val logger: Logger = LoggerFactory.getLogger(javaClass)
    private final val powerPointMimeTypes = setOf(
        "application/vnd.ms-powerpoint",
        "application/vnd.openxmlformats-officedocument.presentationml.presentation"
    )
    override val allowedMimeTypesPerFileExtension: Map<String, Set<String>> = mapOf(
        "ppt" to powerPointMimeTypes,
        "pptx" to powerPointMimeTypes,

    )
    override fun convertToPdf(file: MultipartFile): ByteArray {
        val outputStream = ByteArrayOutputStream()

        val ppt = XMLSlideShow(file.inputStream)
        val pdfDocument = PdfDocument(PdfWriter(outputStream))
        val document = Document(pdfDocument)

        for (slide in ppt.slides) {
            val pptxSlide = slide as XSLFSlide
            val content = pptxSlide.shapes

            for (shape in content) {
                if (shape is XSLFTextShape) {
                    val text = shape.text
                    document.add(Paragraph(text))
                } else if (shape is XSLFPictureShape) {
                    val pictureData = shape.pictureData.data
                    val picture = ImageDataFactory.create(pictureData)
                    document.add(Image(picture))
                }
            }
            document.add(Paragraph("\n\n"))
        }

        document.close()
        pdfDocument.close()

        return outputStream.toByteArray()
    }

}