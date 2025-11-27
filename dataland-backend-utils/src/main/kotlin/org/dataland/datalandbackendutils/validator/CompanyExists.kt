package org.dataland.datalandbackendutils.validator

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import okhttp3.OkHttpClient
import okhttp3.Request
import org.dataland.datalandbackendutils.utils.ValidationUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import java.io.IOException
import kotlin.reflect.KClass

/**
 * Annotation to validate that a company exists on Dataland
 */
@Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.FIELD, AnnotationTarget.PROPERTY_GETTER)
@Constraint(
    validatedBy = [
        CompanyExistsStringValidator::class,
        CompanyExistsUuidValidator::class,
    ],
)
annotation class CompanyExists(
    val message: String =
        "Input validation failed: The company does not exist on Dataland. " +
            "There is no resource with this companyId on Dataland",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)

/**
 * Validator to check that a company exists on Dataland
 */
class CompanyExistsStringValidator(
    @Value("\${dataland.backend.base-url}") private val backendBaseUrl: String,
    @Qualifier("AuthenticatedOkHttpClient") val authenticatedOkHttpClient: OkHttpClient,
) : ConstraintValidator<CompanyExists, String> {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun isValid(
        companyId: String?,
        context: ConstraintValidatorContext?,
    ): Boolean {
        if (companyId == null || !ValidationUtils.isUuid(companyId)) return false

        return callCompanyDataApiAndCheckCompanyId(
            backendBaseUrl,
            authenticatedOkHttpClient,
            companyId,
            logger,
        )
    }

    /**
     * Makes a HEAD request to the company data API to verify if a company with the specified ID exists.
     *
     * @param backendBaseUrl The base URL of the backend server hosting the company data API.
     * @param authenticatedOkHttpClient An authenticated OkHttpClient used to make the HTTP request.
     * @param companyId The unique identifier of the company to be checked.
     * @param logger A logger instance used to log information and warnings related to the request.
     * @return True if the company exists (determined by a successful HTTP response), false otherwise.
     */
    fun callCompanyDataApiAndCheckCompanyId(
        backendBaseUrl: String,
        authenticatedOkHttpClient: OkHttpClient,
        companyId: String,
        logger: Logger,
    ): Boolean {
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
                    logger.info("Company with id $companyId not found: Status code ${response.code}")
                    false
                }
            }
        } catch (exception: IOException) {
            logger.warn("Error validating company existence: ${exception.message}")
            false
        }
    }
}
