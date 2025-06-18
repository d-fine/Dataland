package org.dataland.datalandbatchmanager.service

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.resilience4j.ratelimiter.RateLimiter
import io.github.resilience4j.ratelimiter.RateLimiter.waitForPermission
import io.github.resilience4j.ratelimiter.RateLimiterConfig
import io.github.resilience4j.ratelimiter.RequestNotPermitted
import io.github.resilience4j.retry.Retry
import io.github.resilience4j.retry.RetryConfig
import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.infrastructure.ClientError
import org.dataland.datalandbackend.openApiClient.infrastructure.ClientException
import org.dataland.datalandbackend.openApiClient.infrastructure.ServerException
import org.dataland.datalandbackend.openApiClient.model.CompanyInformationPatch
import org.dataland.datalandbackend.openApiClient.model.IdentifierType
import org.dataland.datalandbatchmanager.model.ExternalCompanyInformation
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import java.net.SocketTimeoutException
import java.time.Duration

/**
 * Class for handling the upload of the company information retrieved from GLEIF to the Dataland backend
 */
@Service
class CompanyUploader(
    @Autowired private val companyDataControllerApi: CompanyDataControllerApi,
    @Autowired private val objectMapper: ObjectMapper,
) {
    companion object {
        const val MAX_RETRIES = 3
        const val WAIT_DURATION: Long = 1
        const val UNAUTHORIZED_CODE = 401
        const val LIMIT_FOR_PERIOD = 500
        const val LIMIT_REFRESH_DURATION: Long = 1
        const val TIMEOUT_DURATION: Long = 60
    }

    private val logger = LoggerFactory.getLogger(javaClass)

    private val rateLimiter: RateLimiter =
        RateLimiter.of(
            "companyUploaderLimiter",
            RateLimiterConfig
                .custom()
                .limitForPeriod(LIMIT_FOR_PERIOD)
                .limitRefreshPeriod(Duration.ofSeconds(LIMIT_REFRESH_DURATION))
                .timeoutDuration(Duration.ofMinutes(TIMEOUT_DURATION))
                .build(),
        )

    private val retry: Retry =
        Retry
            .of(
                "companyUploaderRetry",
                RetryConfig
                    .custom<Any>()
                    .maxAttempts(MAX_RETRIES)
                    .waitDuration(Duration.ofSeconds(WAIT_DURATION))
                    .retryExceptions(
                        SocketTimeoutException::class.java,
                        ClientException::class.java,
                        ServerException::class.java,
                    ).build(),
            ).apply {
                eventPublisher.onRetry { event ->
                    logger.warn("Retry attempt #${event.numberOfRetryAttempts} failed: ${event.lastThrowable?.message}, retrying...")
                }
            }

    @Suppress("ReturnCount")
    private fun checkForDuplicateIdentifierAndGetConflictingCompanyId(exception: ClientException): Pair<String?, Set<String?>?> {
        if (exception.statusCode != HttpStatus.BAD_REQUEST.value()) return null to null

        val exceptionResponse = exception.response!! as ClientError<*>
        val exceptionBodyString = exceptionResponse.body.toString()

        val errorResponseBody = objectMapper.readTree(exceptionBodyString)
        val firstError = errorResponseBody["errors"]?.get(0)
        if (firstError?.get("errorType")?.textValue() != "duplicate-company-identifier") return null to null

        val conflictingIdentifiers = firstError["metaInformation"]
        if (conflictingIdentifiers == null || !conflictingIdentifiers.isArray || conflictingIdentifiers.size() == 0) return null to null

        val conflictingCompanyIds = conflictingIdentifiers.mapNotNull { it["companyId"]?.textValue() }.toSet()
        val conflictingIdentifierTypes = conflictingIdentifiers.mapNotNull { it["identifierType"]?.textValue() }.toSet()

        return if (conflictingCompanyIds.size != 1) {
            conflictingCompanyIds.first() to conflictingIdentifierTypes
        } else {
            logger.error("Found conflicting identifiers for two different companies $conflictingCompanyIds")
            null to null
        }
    }

    private fun executeWithRetryAndThrottling(task: () -> Unit) {
        val decoratedTask =
            Retry.decorateCheckedRunnable(retry) {
                waitForPermission(rateLimiter)
                task()
            }
        try {
            decoratedTask.run()
            logger.info("Function executed successfully.")
        } catch (exception: ClientException) {
            logger.error("Unexpected client exception occurred. Response was: ${exception.message}.")
            throw exception
        } catch (exception: SocketTimeoutException) {
            logger.error("Unexpected timeout occurred. Response was: ${exception.message}.")
        } catch (exception: ServerException) {
            logger.error("Unexpected server exception. Response was: ${exception.message}.")
        } catch (exception: RequestNotPermitted) {
            logger.error("Rate limit exceeded: ${exception.message}.")
        }
    }

    /**
     * Uploads a single Company to the dataland backend performing at most MAX_RETRIES retries.
     * If the company (identified by the LEI) already exists on dataland, it is patched instead.
     * This function absorbs errors and logs them.
     */
    fun uploadOrPatchSingleCompany(companyInformation: ExternalCompanyInformation) {
        var patchCompanyId: String? = null
        var allConflictingIdentifiers: Set<String?>? = null

        executeWithRetryAndThrottling {
            try {
                logger.info("Uploading company data for ${companyInformation.getNameAndIdentifier()} ")
                companyDataControllerApi.postCompany(companyInformation.toCompanyPost())
            } catch (exception: ClientException) {
                val (conflictingCompanyId, conflictingIdentifiers) =
                    checkForDuplicateIdentifierAndGetConflictingCompanyId(exception)
                if (conflictingCompanyId != null) {
                    patchCompanyId = conflictingCompanyId
                    allConflictingIdentifiers = conflictingIdentifiers
                } else {
                    throw exception
                }
            }
        }

        patchCompanyId?.let {
            logger.info(
                "Company Data for Company ${companyInformation.getNameAndIdentifier()}" +
                    "already present on Dataland. Proceeding to patch company with id $it",
            )
            patchSingleCompany(it, companyInformation, allConflictingIdentifiers)
        }
    }

    /**
     * Updates the information regarding a single company using the dataland-backend API.
     */
    private fun patchSingleCompany(
        companyId: String,
        companyInformation: ExternalCompanyInformation,
        conflictingIdentifiers: Set<String?>?,
    ) {
        val companyPatch = companyInformation.toCompanyPatch(conflictingIdentifiers) ?: return
        executeWithRetryAndThrottling {
            logger.info("Patching single company data for ${companyInformation.getNameAndIdentifier()}")
            companyDataControllerApi.patchCompanyById(companyId, companyPatch)
        }
    }

    /**
     * Updates the final / ultimate parents of all companies.
     * @param finalParentMapping the parent-mapping with the format "LEI" -> "LEI"
     */
    fun updateRelationships(finalParentMapping: Map<String, String>) {
        finalParentMapping.forEach { (startLei, endLei) ->
            val companyId = searchCompanyByLEI(startLei) ?: return@forEach
            executeWithRetryAndThrottling {
                logger.info("Updating relationship of company with ID: $companyId and LEI: $startLei")
                companyDataControllerApi.patchCompanyById(companyId, CompanyInformationPatch(parentCompanyLei = endLei))
            }
        }
    }

    private fun searchCompanyByLEI(lei: String): String? {
        var companyId: String? = null
        var found404 = false

        executeWithRetryAndThrottling {
            logger.info("Searching for company with LEI: $lei")
            try {
                companyId = companyDataControllerApi.getCompanyIdByIdentifier(IdentifierType.Lei, lei).companyId
            } catch (exception: ClientException) {
                if (exception.statusCode == HttpStatus.NOT_FOUND.value()) {
                    logger.error("Could not find company with LEI: $lei")
                    found404 = true
                } else {
                    throw exception
                }
            }
        }
        return if (found404) null else companyId
    }

    /**
     * Updates the ISINs of all companies.
     * @param leiIsinMapping the delta-map with the format "LEI"->"ISIN1,ISIN2,..."
     */
    fun updateIsins(leiIsinMapping: Map<String, Set<String>>) {
        val retryLeiIsinMappingQueue = mutableMapOf<String, Set<String>>()

        leiIsinMapping.forEach { (lei, newIsins) ->
            val companyId = searchCompanyByLEI(lei) ?: return@forEach
            logger.info("Patching company with ID: $companyId and LEI: $lei")
            try {
                updateIsinsOfCompany(newIsins, companyId)
            } catch (exception: ClientException) {
                logger.warn("Initial update failed due to ${exception.message} for company with ID: $companyId and LEI: $lei")
                retryLeiIsinMappingQueue[lei] = newIsins
            }
        }

        retryLeiIsinMappingQueue.forEach { (lei, newIsins) ->
            val companyId = searchCompanyByLEI(lei) ?: return@forEach
            logger.warn("Retrying to patch company with ID: $companyId and LEI: $lei")
            try {
                updateIsinsOfCompany(newIsins, companyId)
            } catch (exception: ClientException) {
                logger.error("Retry failed due to ${exception.message} for company with ID: $companyId and LEI: $lei")
            }
        }
    }

    private fun updateIsinsOfCompany(
        isins: Set<String>,
        companyId: String,
    ) {
        val updatedIdentifiers =
            mapOf(
                IdentifierType.Isin.value to isins.toList(),
            )
        val companyPatch = CompanyInformationPatch(identifiers = updatedIdentifiers)
        executeWithRetryAndThrottling {
            logger.info("Updating ISINs of company with ID: $companyId")
            companyDataControllerApi.patchCompanyById(companyId, companyPatch)
        }
    }
}
