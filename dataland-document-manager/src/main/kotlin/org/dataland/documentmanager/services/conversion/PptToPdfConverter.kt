import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.AreaBreak
import com.itextpdf.layout.element.Div
import com.itextpdf.layout.element.Image
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.property.AreaBreakType
import org.apache.poi.xslf.usermodel.XMLSlideShow
import org.apache.poi.xslf.usermodel.XSLFGroupShape
import org.apache.poi.xslf.usermodel.XSLFPictureShape
import org.apache.poi.xslf.usermodel.XSLFShape
import org.apache.poi.xslf.usermodel.XSLFSlide
import org.apache.poi.xslf.usermodel.XSLFTextShape
import org.dataland.documentmanager.services.conversion.FileConverter
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import java.awt.Color
import java.awt.geom.Rectangle2D
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.FileOutputStream
import javax.imageio.ImageIO

@Component
class PptToPdfConverter : FileConverter() {
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
            val pgsize = ppt.pageSize

            val img = BufferedImage(pgsize.width, pgsize.height, BufferedImage.TYPE_INT_RGB)
            val graphics = img.createGraphics()
            graphics.background = java.awt.Color.WHITE
            graphics.clearRect(0, 0, pgsize.width, pgsize.height)
            pptxSlide.draw(graphics)

            val byteStream = ByteArrayOutputStream()
            ImageIO.write(img, "png", byteStream)
            val imgBytes = byteStream.toByteArray()

            val pdfImg = Image(ImageDataFactory.create(imgBytes))
            document.add(pdfImg)
            document.add(AreaBreak(AreaBreakType.NEXT_PAGE))
        }

        document.close()
        pdfDocument.close()

        //todo
        val outputFile = "MeineTestPowerPointToPdf.pdf"
        try {
            val fileOutputStream = FileOutputStream(outputFile)
            fileOutputStream.write(outputStream.toByteArray())
            fileOutputStream.close()
            println("PDF-Datei erfolgreich gespeichert: $outputFile")
        } catch (e: Exception) {
            println("Fehler beim Speichern der PDF-Datei: ${e.message}")
        }
        //todo
        return outputStream.toByteArray()
    }

}