
import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.kernel.colors.Color
import com.itextpdf.kernel.colors.DeviceRgb
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Image
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.element.Text
import org.apache.commons.lang3.StringUtils.substring
import org.apache.poi.xwpf.usermodel.UnderlinePatterns
import org.apache.poi.xwpf.usermodel.XWPFDocument
import org.apache.poi.xwpf.usermodel.XWPFParagraph
import org.apache.poi.xwpf.usermodel.XWPFPicture
import org.apache.poi.xwpf.usermodel.XWPFRun
import org.apache.poi.xwpf.usermodel.XWPFTable
import org.dataland.documentmanager.services.conversion.FileConverter
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import java.io.ByteArrayOutputStream
import java.io.FileOutputStream

@Component
class DocxToPdfConverter: FileConverter() {
    override val logger: Logger = LoggerFactory.getLogger(javaClass)
    private final val docxMimeTypes = setOf(
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
         )
    override val allowedMimeTypesPerFileExtension: Map<String, Set<String>> = mapOf(
        "docx" to docxMimeTypes,
        )
    override fun convertToPdf(file: MultipartFile): ByteArray {
        val outputStream = ByteArrayOutputStream()

        val docx = XWPFDocument(file.inputStream)
        val pdfDocument = PdfDocument(PdfWriter(outputStream))
        val document = Document(pdfDocument)
        for(element in docx.bodyElementsIterator){
            if(element is XWPFParagraph){
                val pdfParagraph = createPdfParagraph(element)
                document.add(pdfParagraph)
            }
            if(element is XWPFTable){
                val pdfTable = createPdfTable(element)
                document.add(pdfTable)
            }
            if(element is XWPFPicture){
                val pictureData = element.pictureData.data
                val picture = Image(ImageDataFactory.create(pictureData))
                picture.setWidth(element.width.toFloat())
                document.add(picture)
            }
            //todo error message?
        }
        document.close()
        pdfDocument.close()

        //todo remove saving
        val outputFile = "MeineTestDocxToPdf.pdf"
        try {
            val fileOutputStream = FileOutputStream(outputFile)
            fileOutputStream.write(outputStream.toByteArray())
            fileOutputStream.close()
            println("PDF-Datei erfolgreich gespeichert: $outputFile")
        } catch (e: Exception) {
            println("Fehler beim Speichern der PDF-Datei: ${e.message}")
        }

        return outputStream.toByteArray()
    }

    private fun createPdfParagraph(xwpfParagraph: XWPFParagraph):Paragraph {
        val pdfParagraph = Paragraph()
        for (run: XWPFRun in xwpfParagraph.runs) {
            if (!run.embeddedPictures.isEmpty()) {
                val pictures = run.embeddedPictures
                pictures.forEach{
                    val pictureData = it.pictureData.data
                    val picture = Image(ImageDataFactory.create(pictureData))
                    picture.setWidth(it.width.toFloat())
                    pdfParagraph.add(picture)
                }
            } else {


            val txt = Text(run.text())

            if (run.color != null) {
                val r: Int = substring(run.color, 0, 2).toInt(16) // 16 for hex
                val g: Int = substring(run.color, 2, 4).toInt(16) // 16 for hex
                val b: Int = substring(run.color, 4, 6).toInt(16) // 16 for hex
                val fontColor: Color = DeviceRgb(r, g, b)
                txt.setFontColor(fontColor)
            }
            if (run.isBold) {
                txt.setBold()
            }

            if (run.isItalic) {
                txt.setItalic()
            }
            if (run.underline == UnderlinePatterns.SINGLE) {
                txt.setUnderline()
            }
            pdfParagraph.add(txt)
        }
    }
        return pdfParagraph
    }
    fun createPdfTable(table: XWPFTable): Table {
        val pdfTable = Table(table.rows[0].tableCells.size)
        for (row in table.rows) {
            for (cell in row.tableCells) {
                val pdfCell = com.itextpdf.layout.element.Cell()
                pdfCell.setWidth(cell.widthDecimal.toFloat())
                for(paragraph in cell.paragraphs){
                    pdfCell.add(createPdfParagraph(paragraph))
                }
                pdfTable.addCell(pdfCell)
            }
        }
        return pdfTable
    }

}