package org.dataland.datalandbackend.validator

import com.fasterxml.jackson.module.kotlin.readValue
import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import org.dataland.datalandbackendutils.utils.JsonUtils.defaultObjectMapper
import org.springframework.core.io.ClassPathResource
import kotlin.reflect.KClass

/**
 * Annotation for validating that a string is an ISO 4217 currency code known to Dataland.
 * Null values are considered valid.
 */
@Target(AnnotationTarget.FIELD)
@Constraint(validatedBy = [ValidCurrencyCodeValidator::class])
annotation class ValidCurrencyCode(
    val message: String = "The currency must be a valid ISO 4217 currency code (e.g. 'EUR').",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)

/**
 * Validator class checking that a currency code is contained in the shared currencyCodes.json resource
 * (which is also used by the frontend).
 */
class ValidCurrencyCodeValidator : ConstraintValidator<ValidCurrencyCode, String?> {
    private data class CurrencyCodeEntry(
        val code: String,
        val name: String,
    )

    companion object {
        private const val RESOURCE_PATH = "currencyCodes.json"

        private val validCodes: Set<String> by lazy {
            ClassPathResource(RESOURCE_PATH).inputStream.use { stream ->
                defaultObjectMapper.readValue<List<CurrencyCodeEntry>>(stream).map { it.code }.toSet()
            }
        }
    }

    override fun isValid(
        value: String?,
        context: ConstraintValidatorContext?,
    ): Boolean = value == null || value in validCodes
}
