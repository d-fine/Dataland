package org.dataland.documentmanager.services

import java.io.File
import org.apache.pdfbox.Loader
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import java.io.IOException
import org.apache.tika.Tika

/**
 * A service for performing basic sanity checks on files uploaded by users
 */
@Component
class VerificationService {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val extensionToMimetype = mapOf(
        "ppt" to "application/vnd.ms-powerpoint",
        "pptx" to "application/vnd.openxmlformats-officedocument.presentationml.presentation",
        "doc" to "application/msword",
        "docx" to "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        "txt" to "text/plain",
        "png" to "image/png",
        "jpg" to "image/jpeg",
        "jpeg" to "image/jpeg",
        "jpe" to "image/jpeg",
        "jxr" to "image/vnd.ms-photo",
        "tif" to "image/tiff",
        "tiff" to "image/tiff",
        "heif" to "image/heif",
        "heic" to "image/heic",
    )

    val pdfParsingErrorMessage = "The file you uploaded was not able to be parsed as PDF file."
    val pdfHasZeroPagesErrorMessage = "The PDF you uploaded seems to have 0 pages."
    val fileNameHasForbiddenCharactersMessage =
        "Please ensure that your selected file name follows the naming convention for Windows: Avoid using " +
            "special characters like < > : \" / \\ | ? * and ensure the name does not end or begin with a space, " +
            "or end with a full stop character."
    val typeNotSupportedMessage = "Only supported file types can be uploaded."
    val fileExtensionAndMimeTypeMismatchMessage = "Only upload of documents with matching file extensions and MIME types is supported."

    /**
     * A function that performs surface-level checks to ensure that an uploaded file is indeed a PDF with at least one
     * page and a file name that follows a specific naming convention.
     * This function does not enforce anything else.
     * In particular a file passing this function is in no way guaranteed to be safe to open.
     */
    fun assertThatFileLooksLikeAValidPdfWithAValidName(file: MultipartFile, correlationId: String) {
        try {
            checkIfPotentialPdfFileIsEmpty(file.bytes, correlationId)
            checkThatFileNameIsWithinNamingConvention(file.originalFilename!!, correlationId)
        } catch (ex: IOException) {
            logger.info("Document uploaded with correlation ID: $correlationId cannot be parsed as a PDF, aborting.")
            throw InvalidInputApiException(
                "Could not parse file as PDF document",
                pdfParsingErrorMessage,
                ex,
            )
        }
    }

    private fun checkIfPotentialPdfFileIsEmpty(blob: ByteArray, correlationId: String) {
        Loader.loadPDF(blob).use {
            if (it.numberOfPages <= 0) {
                logger.info(
                    "PDF document uploaded with correlation ID: $correlationId seems to have 0 pages, aborting.",
                )
                throw InvalidInputApiException(
                    "You seem to have uploaded an empty PDF",
                    pdfHasZeroPagesErrorMessage,
                )
            }
        }
    }

    fun checkMimeTypeAndFileExtensionForConsistency(file: MultipartFile, correlationId: String) {
        val detectedMimeType = Tika().detect(file.bytes)
        val fileExtension = file.originalFilename?.let { File(it).extension }
        if (! extensionToMimetype.containsKey(fileExtension)) {
            throw InvalidInputApiException(
                "The provided file extension $fileExtension of ${file.originalFilename} is not recognized.",
                typeNotSupportedMessage,
            )
        }
        val expectedMimeType = extensionToMimetype[fileExtension]
        if (detectedMimeType != expectedMimeType) {
            throw InvalidInputApiException(
                "The provided file extension $fileExtension does not match the detected MIME type $detectedMimeType.",
                fileExtensionAndMimeTypeMismatchMessage,
            )
        }
    }

    /**
     * We allow file names that follow the naming convention of Windows systems.
     */
    private val allowedFilenameRegex =
        Regex("^[^<>:\"|?/*\\\\\\s][^<>:\"|?/*\\\\]{0,252}[^<>:\"|?/*\\\\.\\s]\$")

    private fun checkThatFileNameIsWithinNamingConvention(name: String, correlationId: String) {
        if (!allowedFilenameRegex.matches(name)) {
            logger.info(
                "PDF document uploaded with correlation ID: $correlationId violates the naming convention" +
                    "of Dataland, aborting.",
            )
            throw InvalidInputApiException(
                "You seem to have uploaded a file that has an invalid name",
                fileNameHasForbiddenCharactersMessage,
            )
        }
    }
}
