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

@Component
class DocxToPdfConverter : FileConverter() {
    override val logger: Logger = LoggerFactory.getLogger(javaClass)
    var pathToLibre = "C:\\Program Files\\LibreOffice" //todo "/usr/lib/libreoffice"
    private final val docxMimeTypes = setOf(
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        "application/msword",
    )
    override val allowedMimeTypesPerFileExtension: Map<String, Set<String>> = mapOf(
        "docx" to docxMimeTypes,
        "doc" to docxMimeTypes,

    )

    override fun convert(file: MultipartFile, correlationId: String): ByteArray {
        val outputStream = ByteArrayOutputStream()

        val officeManager: OfficeManager = LocalOfficeManager.builder()
            .officeHome(pathToLibre)
            .build()
        officeManager.start()

        val converter = LocalConverter.builder()
            .officeManager(officeManager)
            .build()

        converter.convert(file.inputStream)
            .`as`(DefaultDocumentFormatRegistry.DOCX)
            .to(outputStream)
            .`as`(DefaultDocumentFormatRegistry.PDF)
            .execute()

        officeManager.stop()
        return outputStream.toByteArray()
    }
}
