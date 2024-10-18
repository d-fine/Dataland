package org.dataland.datalandbackend.validator

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import org.dataland.documentmanager.openApiClient.api.DocumentControllerApi
import org.dataland.documentmanager.openApiClient.infrastructure.ClientException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import kotlin.reflect.KClass

/**
 * Annotation for the validation of Base- and ExtendedDataPoints holding an existing document
 */
@Target(AnnotationTarget.FIELD)
@Constraint(
    validatedBy = [
        DocumentReferenceExistsValidator::class,
    ],
)
annotation class DocumentExists(
    val message: String = "Input validation failed: The document reference doesn't exist.",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)

/**
 * Class holding the validation logic for an BaseDocumentReference. It checks if the referenced document is valid
 */
class DocumentReferenceExistsValidator(
    @Qualifier("getDocumentControllerApi")
    @Autowired
    val documentControllerApi: DocumentControllerApi,
) : ConstraintValidator<DocumentExists, String> {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun isValid(
        documentReference: String?,
        context: ConstraintValidatorContext?,
    ): Boolean {
        if (documentReference == null) return true
        return callDocumentApiAndCheckFileReference(documentReference, documentControllerApi, logger)
    }
}

private fun callDocumentApiAndCheckFileReference(
    fileReference: String,
    documentControllerApi: DocumentControllerApi,
    logger: org.slf4j.Logger,
): Boolean {
    try {
        documentControllerApi.checkDocument(fileReference)
    } catch (exception: ClientException) {
        logger.info("The referenced document does not exist.")
        logger.info(
            "Message: ${exception.message} " +
                "Status code: ${exception.statusCode} " +
                "Response: ${exception.response}",
        )
        return false
    }
    return true
}
