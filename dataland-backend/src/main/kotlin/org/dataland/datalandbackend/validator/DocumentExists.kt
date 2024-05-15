package org.dataland.datalandbackend.validator

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import org.dataland.datalandbackend.interfaces.documents.BaseDocumentReference
import org.dataland.documentmanager.openApiClient.api.DocumentControllerApi
import org.springframework.beans.factory.annotation.Value
import kotlin.reflect.KClass

/**
 * Annotation for the validation of an ExtendedDataPoint<*> holding a number
 */
@Target(AnnotationTarget.FIELD)
@Constraint(validatedBy = [DocumentExistsValidator::class])
annotation class DocumentExists(
    val message: String = "Input validation failed: The document reference doesn't exist",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)

/**
 * Class holding the validation logic for a class that implements BaseDataPoint<*> Interface featuring a number
 */
class DocumentExistsValidator(
    @Value("\${dataland.documentmanager.base-url:https://local-dev.dataland.com/documents}")
    private val documentManagerBaseUrl: String,
) : ConstraintValidator<DocumentExists, BaseDocumentReference> {
    // TODO make path adaptive for container
    private val documentControllerApi = DocumentControllerApi(basePath = documentManagerBaseUrl)

    override fun isValid(documentReference: BaseDocumentReference?, context: ConstraintValidatorContext?): Boolean {
        if (documentReference != null) {
            documentControllerApi.checkDocument(documentReference.fileReference!!)
        }
        return true
    }
}
