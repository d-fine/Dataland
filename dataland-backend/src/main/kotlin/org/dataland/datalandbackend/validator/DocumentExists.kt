package org.dataland.datalandbackend.validator

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import org.dataland.datalandbackend.interfaces.documents.BaseDocumentReference
import org.dataland.datalanddocumentmanager.openApiClient.api.DocumentControllerApi
import kotlin.reflect.KClass

/**
 * Annotation for the validation of an ExtendedDataPoint<*> holding a number
 */
@Target(AnnotationTarget.FIELD)
@Constraint(validatedBy = [DocumentExistsValidator::class])
annotation class DocumentExists(
    val message: String = "Input validation failed: A base data point holding a number" +
        " is smaller than the set minimum.",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)

/**
 * Class holding the validation logic for a class that implements BaseDataPoint<*> Interface featuring a number
 */
class DocumentExistsValidator : ConstraintValidator<DocumentExists, BaseDocumentReference> {
    // TODO make path adaptive for container
    val documentControllerApi = DocumentControllerApi(basePath = "http://local-dev.dataland.com/documents")

    override fun isValid(documentReference: BaseDocumentReference?, context: ConstraintValidatorContext?): Boolean {
        return true
    }
}
