package org.dataland.datalandbackendutils.validator

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import okhttp3.OkHttpClient
import org.dataland.datalandbackendutils.utils.ValidationUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import kotlin.reflect.KClass

/**
 * Annotation to validate that a company exists on Dataland
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Constraint(
    validatedBy = [
        CompanyExistsValidator::class,
    ],
)
annotation class CompanyExistsIfUuidIsValid(
    val message: String = "Input validation failed: The company does not exist on Dataland.",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)

/**
 * Validator to check that a company exists on Dataland
 */
class CompanyExistsValidator(
    @Value("\${dataland.backend.base-url}") private val backendBaseUrl: String,
    @Qualifier("AuthenticatedOkHttpClient") val authenticatedOkHttpClient: OkHttpClient,
) : ConstraintValidator<CompanyExistsIfUuidIsValid, String> {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun isValid(
        companyId: String?,
        context: ConstraintValidatorContext?,
    ): Boolean {
        if (companyId == null || !ValidationUtils.isUuid(companyId)) return true
        return callCompanyDataApiAndCheckCompanyId(
            backendBaseUrl,
            authenticatedOkHttpClient,
            companyId,
            logger,
        )
    }
}
