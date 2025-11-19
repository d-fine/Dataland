package org.dataland.datalandbackendutils.validator

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import okhttp3.OkHttpClient
import okhttp3.Request
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import java.io.IOException
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
annotation class CompanyExists(
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
) : ConstraintValidator<CompanyExists, String> {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun isValid(
        companyId: String?,
        context: ConstraintValidatorContext?,
    ): Boolean {
        if (companyId == null) return true
        return callCompanyDataApiAndCheckCompanyId(companyId)
    }

    private fun callCompanyDataApiAndCheckCompanyId(companyId: String): Boolean {
        val request =
            Request
                .Builder()
                .url("$backendBaseUrl/companies/$companyId")
                .head()
                .build()
        return try {
            authenticatedOkHttpClient.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    true
                } else {
                    logger.info(
                        "Company with id $companyId not found: Status code ${response.code}",
                    )
                    false
                }
            }
        } catch (exception: IOException) {
            logger.warn("Error validating company existence: ${exception.message}")
            false
        }
    }
}
