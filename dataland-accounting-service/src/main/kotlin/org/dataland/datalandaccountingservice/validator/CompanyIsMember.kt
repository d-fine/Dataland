package org.dataland.datalandaccountingservice.validator

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import org.dataland.datalandcommunitymanager.openApiClient.api.CompanyRightsControllerApi
import org.dataland.datalandcommunitymanager.openApiClient.model.CompanyRightAssignmentString
import org.slf4j.LoggerFactory
import org.springframework.web.client.RestClientException
import java.io.IOException
import kotlin.reflect.KClass

/**
 * Annotation to validate that a company is a Dataland member
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Constraint(
    validatedBy = [
        CompanyIsMemberValidator::class,
    ],
)
annotation class CompanyIsMember(
    val message: String = "Input validation failed: The company is not a dataland member.",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = [],
)

/**
 * Validator to check that a company is a Dataland member
 */
class CompanyIsMemberValidator(
    private val companyRightsControllerApi: CompanyRightsControllerApi,
) : ConstraintValidator<CompanyIsMember, String> {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun isValid(
        companyId: String?,
        context: ConstraintValidatorContext?,
    ): Boolean {
        if (companyId == null) return false
        return callCompanyRightsAPIAndCheckIfCompanyIsMember(companyId)
    }

    private fun callCompanyRightsAPIAndCheckIfCompanyIsMember(companyId: String): Boolean =
        try {
            val rights = companyRightsControllerApi.getCompanyRights(companyId)
            val isMember = rights.contains(CompanyRightAssignmentString.CompanyRight.Member.toString())
            if (!isMember) {
                logger.info("Company with id $companyId is not a dataland member.")
            }
            isMember
        } catch (e: RestClientException) {
            logger.info("REST error checking if company with id $companyId is a dataland member: ${e.message}")
            false
        } catch (e: IOException) {
            logger.info("I/O error checking if company with id $companyId is a dataland member: ${e.message}")
            false
        }
}
