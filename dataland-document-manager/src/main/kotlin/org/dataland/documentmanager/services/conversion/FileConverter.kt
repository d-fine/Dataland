package org.dataland.documentmanager.services.conversion

import org.apache.tika.Tika
import org.dataland.datalandbackendutils.exceptions.InvalidInputApiException
import org.slf4j.Logger
import org.springframework.web.multipart.MultipartFile
import java.io.File

/**
 * A base class for a converter to convert between different file types
 */
abstract class FileConverter(
    private val allowedMimeTypesPerFileExtension: Map<String, Set<String>>,
) {
    protected abstract val logger: Logger
    private val fileExtensionAndMimeTypeMismatchMessage =
        "Only upload of documents with matching file extensions and MIME types is supported."
    private val fileNameHasForbiddenCharactersMessage =
        "Please ensure that your selected file name follows the naming convention for Windows: Avoid using " +
            "special characters like < > : \" / \\ | ? * and ensure the name does not end or begin with a space, " +
            "or end with a full stop character."
    protected val fileIsEmptyMessage = "The file you uploaded seems to be empty."
    protected val fileIsEmptySummary = "Provided file is empty."
    val responsibleFileExtensions = allowedMimeTypesPerFileExtension.keys

    init {
        require(allowedMimeTypesPerFileExtension.isNotEmpty()) { "No file extension for conversion is provided." }
        require(responsibleFileExtensions.all { it == it.lowercase() }) { "Some file extensions are not lowercase." }
    }

    /**
     * Validates that a file is what it claims to be, e.g. by mime type and content validation
     * @param file the file to validate
     */
    fun validateFile(
        file: MultipartFile,
        correlationId: String,
    ) {
        logger.info("Validating uploaded file. (correlation ID: $correlationId)")
        validateFileNameWithinNamingConvention(file.originalFilename!!, correlationId)
        validateMimeType(file)
        validateFileContent(file, correlationId)
    }

    protected open fun validateFileContent(
        file: MultipartFile,
        correlationId: String,
    ) {
        // Empty, just for customization
    }

    /** Converts a file to a different format
     * @param file the file to convert
     * @returns the converted file as bytes
     */
    fun convertFile(
        file: MultipartFile,
        correlationId: String,
    ): ByteArray {
        logger.info("Converting uploaded file. (correlation ID: $correlationId)")
        return convert(file, correlationId)
    }

    protected abstract fun convert(
        file: MultipartFile,
        correlationId: String,
    ): ByteArray

    private fun validateMimeType(file: MultipartFile) {
        val fileExtension = file.lowercaseExtension()
        require(fileExtension in allowedMimeTypesPerFileExtension)
        val detectedMimeType = Tika().detect(file.bytes)
        val expectedMimeTypes = allowedMimeTypesPerFileExtension.getValue(fileExtension)
        if (detectedMimeType !in expectedMimeTypes) {
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

    private fun validateFileNameWithinNamingConvention(
        name: String,
        correlationId: String,
    ) {
        if (!allowedFilenameRegex.matches(name)) {
            logger.info(
                "Document uploaded with correlation ID: $correlationId violates the naming convention" +
                    "of Dataland, aborting.",
            )
            throw InvalidInputApiException(
                "You seem to have uploaded a file that has an invalid name",
                fileNameHasForbiddenCharactersMessage,
            )
        }
    }
}

/**
 * @returns the lowercase file extension of a file
 */
fun MultipartFile.lowercaseExtension() = this.originalFilename!!.let { File(it).extension }.lowercase()
