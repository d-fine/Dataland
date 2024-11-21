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
class FileProcessor(
    @Autowired val toPdfConverters: List<FileConverter>,
) {
    private val logger = LoggerFactory.getLogger(javaClass)

    init {
        validateNoOverlapInConverterFileExtensions()
    }

    private fun validateNoOverlapInConverterFileExtensions() {
        val aggregatedConverterFileExtensions = toPdfConverters.flatMap { it.responsibleFileExtensions }
        require(aggregatedConverterFileExtensions.size == aggregatedConverterFileExtensions.distinct().size) {
            "There are multiple file converters which target the same file extensions."
        }
    }

    /**
     * Tries to convert a file to a pdf document
     * @file the file to convert
     * @returns the pdf content as bytes
     */
    fun processFile(
        file: MultipartFile,
        correlationId: String,
    ): ByteArray {
        logger.info("Trying to process uploaded file ${file.originalFilename}. (correlation ID: $correlationId)")
        val fileExtension = file.lowercaseExtension()
        val matchingConverter =
            toPdfConverters.find { fileExtension in it.responsibleFileExtensions }
                ?: throw InvalidInputApiException(
                    "File extension $fileExtension could not be recognized",
                    "File extension $fileExtension could not be recognized",
                )
        matchingConverter.validateFile(file, correlationId)
        return matchingConverter.convertFile(file, correlationId)
    }
}
