package org.dataland.documentmanager.services.conversion

import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile

/**
 * A service for converting various file types into PDFs
 */
@Component
class PdfConverter(
    @Autowired val toPdfConverters: List<FileConverter>,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    // todo this should be the only public method in the end
    /**
     * Tries to convert a file to a pdf document
     * @file the file to convert
     * @returns the pdf content as bytes
     */
    fun convertToPdf(file: MultipartFile): ByteArray {
        val fileExtension = file.lowerCaseExtension()
        val matchingConverter = toPdfConverters.find { fileExtension in it.responsibleFileExtensions }
            ?: throw InvalidInputApiException(
                "File extension $fileExtension could not be recognized",
                "File extension $fileExtension could not be recognized",
            )
        matchingConverter.validateFile(file)
        return matchingConverter.convertToPdf(file)
    }

    /** todo
     * to be removed
     */
    fun convertWordDocument(file: MultipartFile, correlationId: String) {
        TODO("Word conversion not implemented")
    }

    /** todo
     * to be removed
     */
    fun convertPowerpoint(file: MultipartFile, correlationId: String) {
        TODO("Powerpoint conversion not implemented")
    }
}
