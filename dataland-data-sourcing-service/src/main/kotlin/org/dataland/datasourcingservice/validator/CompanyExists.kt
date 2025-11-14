package org.dataland.datasourcingservice.validator

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.infrastructure.ClientException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
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
    @Autowired
    val companyDataControllerApi: CompanyDataControllerApi,
) : ConstraintValidator<CompanyExists, String> {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun isValid(
        companyId: String?,
        context: ConstraintValidatorContext?,
    ): Boolean {
        if (companyId == null) return true
        return callCompanyDataApiAndCheckCompanyId(companyId, companyDataControllerApi, logger)
    }
}

private fun callCompanyDataApiAndCheckCompanyId(
    companyId: String,
    companyDataControllerApi: CompanyDataControllerApi,
    logger: org.slf4j.Logger,
): Boolean {
    try {
        companyDataControllerApi.isCompanyIdValid(companyId)
    } catch (exception: ClientException) {
        logger.info("The company does not exist on Dataland.")
        logger.info(
            "Message: ${exception.message} " +
                "Status code: ${exception.statusCode} " +
                "Response: ${exception.response}",
        )
        return false
    }
    return true
}
