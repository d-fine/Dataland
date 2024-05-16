package org.dataland.datalandbackend.validator

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import org.dataland.datalandbackend.interfaces.documents.BaseDocumentReference
import org.dataland.datalandbackend.interfaces.documents.ExtendedDocumentReference

import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.documentmanager.openApiClient.api.DocumentControllerApi
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import kotlin.reflect.KClass

/**
 * Annotation for the validation of an ExtendedDataPoint<*> holding a number todo documentation
 */
@Target(AnnotationTarget.FIELD)
@Constraint(validatedBy = [BaseDocumentReferenceExistsValidator::class,
    ExtendedDocumentReferenceExistsValidator::class])
annotation class DocumentExists(
    val message: String = "Input validation failed: The document reference doesn't exist",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)

/**
 * Class holding the validation logic for a class that implements BaseDataPoint<*> Interface featuring a number
 * todo documentation
 */
class BaseDocumentReferenceExistsValidator(
    @Qualifier("getDocumentControllerApi")
    @Autowired
    val documentControllerApi: DocumentControllerApi,
) : ConstraintValidator<DocumentExists, BaseDocumentReference> {
    override fun isValid(documentReference: BaseDocumentReference?, context: ConstraintValidatorContext?): Boolean {
        if (documentReference == null) return true
        var valid: Boolean
        try {
            documentControllerApi.checkDocument(documentReference.fileReference!!)
            valid = true
        } catch (exception: ResourceNotFoundApiException) {
            valid = false
        }
        return valid
    }
}

class ExtendedDocumentReferenceExistsValidator(
    @Qualifier("getDocumentControllerApi")
    @Autowired
    val documentControllerApi: DocumentControllerApi,
) : ConstraintValidator<DocumentExists, ExtendedDocumentReference> {
    override fun isValid(documentReference: ExtendedDocumentReference?, context: ConstraintValidatorContext?): Boolean {
        if (documentReference == null) return true
        try {
            documentControllerApi.checkDocument(documentReference.fileReference!!)
            return true
        } catch (exception: ResourceNotFoundApiException) {
            return false
        }
    }
}
