import org.dataland.documentmanager.services.conversion.FileConverter
import org.jodconverter.core.document.DefaultDocumentFormatRegistry
import org.jodconverter.core.office.OfficeManager
import org.jodconverter.local.LocalConverter
import org.jodconverter.local.office.LocalOfficeManager
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import java.io.ByteArrayOutputStream
import java.io.FileOutputStream



@Component
class PowerPointToPdfConverter : FileConverter() {
    override val logger: Logger = LoggerFactory.getLogger(javaClass)
    var pathToLibre = "C:\\Program Files\\LibreOffice" //todo
    private final val powerPointMimeTypes = setOf(
        "application/vnd.ms-powerpoint",
        "application/vnd.openxmlformats-officedocument.presentationml.presentation"
    )
    override val allowedMimeTypesPerFileExtension: Map<String, Set<String>> = mapOf(
        "ppt" to powerPointMimeTypes,
        "pptx" to powerPointMimeTypes,

    )

    override fun convert(file: MultipartFile, correlationId: String): ByteArray {
        val officeManager: OfficeManager = LocalOfficeManager.builder()
            .officeHome(pathToLibre)
            .build()
        officeManager.start()
        val outputStream = ByteArrayOutputStream()

        val converter = LocalConverter.builder()
            .officeManager(officeManager)
            .build()

            converter.convert(file.inputStream)
            .`as`(DefaultDocumentFormatRegistry.PPTX)
            .to(outputStream)
            .`as`(DefaultDocumentFormatRegistry.PDF)
            .execute()
        //todo remove saving
        val outputFile = "MeineTestPowerPointToPdf.pdf"
        try {
            val fileOutputStream = FileOutputStream(outputFile)
            fileOutputStream.write(outputStream.toByteArray())
            fileOutputStream.close()
            println("PDF-Datei erfolgreich gespeichert: $outputFile")
        } catch (e: Exception) {
            println("Fehler beim Speichern der PDF-Datei: ${e.message}")
        }
        officeManager.stop()
        return outputStream.toByteArray()

    }
}