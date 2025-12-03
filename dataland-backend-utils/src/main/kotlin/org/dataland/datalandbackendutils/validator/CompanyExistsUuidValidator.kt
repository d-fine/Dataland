package org.dataland.datalandbackendutils.validator

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import okhttp3.OkHttpClient
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import java.util.UUID

/**
 * Validator to check that a company exists on Dataland
 */
class CompanyExistsUuidValidator(
    @Value("\${dataland.backend.base-url}") private val backendBaseUrl: String,
    @Qualifier("AuthenticatedOkHttpClient") val authenticatedOkHttpClient: OkHttpClient,
) : ConstraintValidator<CompanyExists, UUID> {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun isValid(
        companyId: UUID?,
        context: ConstraintValidatorContext?,
    ): Boolean {
        if (companyId == null) return false

        return CompanyExistsStringValidator(
            backendBaseUrl = backendBaseUrl,
            authenticatedOkHttpClient = authenticatedOkHttpClient,
        ).callCompanyDataApiAndCheckCompanyId(
            backendBaseUrl = backendBaseUrl,
            authenticatedOkHttpClient = authenticatedOkHttpClient,
            companyId = companyId.toString(),
            logger = logger,
        )
    }
}
