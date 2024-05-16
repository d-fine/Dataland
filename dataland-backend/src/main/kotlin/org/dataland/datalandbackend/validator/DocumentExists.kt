package org.dataland.datalandbackend.validator

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import org.dataland.datalandbackend.interfaces.documents.BaseDocumentReference
import org.dataland.datalandbackend.interfaces.documents.ExtendedDocumentReference
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
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
        BaseDocumentReferenceExistsValidator::class,
        ExtendedDocumentReferenceExistsValidator::class,
    ],
)
annotation class DocumentExists(
    val message: String = "Input validation failed: The document reference doesn't exist",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)

/**
 * Class holding the validation logic for an BaseDocumentReference. It checks if the referenced document is valid
 */
class BaseDocumentReferenceExistsValidator(
    @Qualifier("getDocumentControllerApi")
    @Autowired
    val documentControllerApi: DocumentControllerApi,
) : ConstraintValidator<DocumentExists, BaseDocumentReference> {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun isValid(documentReference: BaseDocumentReference?, context: ConstraintValidatorContext?): Boolean {
        if (documentReference == null) return true
        return callDocumentApiAndCheckFileReference(documentReference.fileReference!!, documentControllerApi, logger)
    }
}

/**
 * Class holding the validation logic for an ExtendedDocumentReference. It checks if the referenced document is valid
 */
class ExtendedDocumentReferenceExistsValidator(
    @Qualifier("getDocumentControllerApi")
    @Autowired
    val documentControllerApi: DocumentControllerApi,
) : ConstraintValidator<DocumentExists, ExtendedDocumentReference> {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun isValid(documentReference: ExtendedDocumentReference?, context: ConstraintValidatorContext?): Boolean {
        if (documentReference == null) return true
        return callDocumentApiAndCheckFileReference(documentReference.fileReference!!, documentControllerApi, logger)
    }
}

private fun callDocumentApiAndCheckFileReference(
    fileReference: String,
    documentControllerApi: DocumentControllerApi,
    logger: org.slf4j.Logger,
): Boolean {
    try {
        documentControllerApi.checkDocument(fileReference)
    } catch (exception: ResourceNotFoundApiException) {
        logger.info("The referenced document doesn't have a valid reference or doesn't exist at all.")
        logger.info(exception.message + exception.summary)
        // return false
    } catch (exception: ClientException) {
        logger.info("This is a client exception!")
        logger.info(exception.message + exception.statusCode + exception.response)
        return false
    }
    return true
}
