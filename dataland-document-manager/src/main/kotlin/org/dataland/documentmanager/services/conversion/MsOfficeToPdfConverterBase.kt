package org.dataland.documentmanager.services.conversion

import org.jodconverter.core.document.DefaultDocumentFormatRegistry
import org.jodconverter.core.document.DocumentFormat
import org.jodconverter.core.office.OfficeManager
import org.jodconverter.local.LocalConverter
import org.jodconverter.local.office.LocalOfficeManager
import org.springframework.web.multipart.MultipartFile
import java.io.ByteArrayOutputStream

/**
 * Base for converters converting a word file to a pdf file
 */
abstract class MsOfficeToPdfConverterBase(
    private val converterSourceType: DocumentFormat,
    private val pathToLibre: String,
    allowedMimeTypesPerFileExtension: Map<String, Set<String>>,
) : FileConverter(
        allowedMimeTypesPerFileExtension = allowedMimeTypesPerFileExtension,
    ) {
    override fun convert(
        file: MultipartFile,
        correlationId: String,
    ): ByteArray {
        logger.info("Converting ${file.lowercaseExtension()} to a pdf document. (correlation ID: $correlationId)")
        val outputStream = ByteArrayOutputStream()

        val officeManager: OfficeManager =
            LocalOfficeManager
                .builder()
                .officeHome(pathToLibre)
                .build()
        officeManager.start()

        val converter =
            LocalConverter
                .builder()
                .officeManager(officeManager)
                .build()

        file.inputStream.use { inputStream ->
            converter
                .convert(inputStream)
                .`as`(converterSourceType)
                .to(outputStream)
                .`as`(DefaultDocumentFormatRegistry.PDF)
                .execute()
        }

        officeManager.stop()
        return outputStream.toByteArray()
    }
}
