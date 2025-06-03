package org.dataland.datalandbatchmanager.service

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.resilience4j.ratelimiter.RateLimiter
import io.github.resilience4j.ratelimiter.RateLimiter.waitForPermission
import io.github.resilience4j.ratelimiter.RateLimiterRegistry
import io.github.resilience4j.retry.Retry
import io.github.resilience4j.retry.RetryRegistry
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

/**
 * Class for handling the upload of the company information retrieved from GLEIF to the Dataland backend
 */
@Service
class CompanyUploader(
    @Autowired private val companyDataControllerApi: CompanyDataControllerApi,
    @Autowired private val objectMapper: ObjectMapper,
    @Autowired private val rateLimiterRegistry: RateLimiterRegistry,
    @Autowired private val retryRegistry: RetryRegistry,
) {
    companion object {
        const val MAX_RETRIES = 3
        const val UNAUTHORIZED_CODE = 401
    }

    private val logger = LoggerFactory.getLogger(javaClass)

    private val rateLimiter: RateLimiter = rateLimiterRegistry.rateLimiter("apiRateLimiter")
    private val retry: Retry = retryRegistry.retry("apiRetry")

    init {
        retry.eventPublisher.onRetry { event ->
            logger.warn("Retry attempt #${event.numberOfRetryAttempts} failed due to: ${event.lastThrowable?.message}, retrying...")
        }
    }

    @Suppress("ReturnCount")
    private fun checkForDuplicateIdentifierAndGetConflictingCompanyId(exception: ClientException): Pair<String?, Set<String?>?> {
        if (exception.statusCode != HttpStatus.BAD_REQUEST.value()) {
            return Pair(null, null)
        }

        val exceptionResponse = exception.response!! as ClientError<*>
        val exceptionBodyString = exceptionResponse.body.toString()

        val errorResponseBody = objectMapper.readTree(exceptionBodyString)
        val firstError = errorResponseBody["errors"]?.get(0)
        if (firstError?.get("errorType")?.textValue() != "duplicate-company-identifier") {
            return Pair(null, null)
        }

        val conflictingIdentifiers = firstError["metaInformation"]
        if (conflictingIdentifiers == null || !conflictingIdentifiers.isArray || conflictingIdentifiers.size() == 0) {
            return Pair(null, null)
        }

        val conflictingIdentifierTypes: MutableSet<String?> = mutableSetOf()
        val conflictingCompanyIds: MutableSet<String?> = mutableSetOf()
        conflictingIdentifiers.forEach {
            conflictingIdentifierTypes.add(it["identifierType"]?.textValue())
            conflictingCompanyIds.add(it["companyId"]?.textValue())
        }

        if (conflictingCompanyIds.size != 1) {
            logger.error("Found conflicting identifiers for two different companies $conflictingCompanyIds")
            return Pair(null, null)
        }
        return Pair(conflictingCompanyIds.first(), conflictingIdentifierTypes)
    }

    private fun executeWithRetryAndThrottling(task: () -> Unit) {
        val decoratedTask = Retry.decorateRunnable(retry, task)
        try {
            waitForPermission(rateLimiter)
            decoratedTask.run()
            logger.info("Function executed successfully.")
        } catch (exception: ClientException) {
            logger.error("Unexpected client exception occurred. Response was: ${exception.message}.")
        } catch (exception: SocketTimeoutException) {
            logger.error("Unexpected timeout occurred. Response was: ${exception.message}.")
        } catch (exception: ServerException) {
            logger.error("Unexpected server exception. Response was: ${exception.message}.")
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
                logger.info("Uploading company data for ${companyInformation.getNameAndIdentifier()}")
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
            companyDataControllerApi.patchCompanyById(
                companyId,
                companyPatch,
            )
        }
    }

    /**
     * Updates the final / ultimate parents of all companies.
     * @param finalParentMapping the parent-mapping with the format "LEI" -> "LEI"
     */
    fun updateRelationships(finalParentMapping: Map<String, String>) {
        for ((startLei, endLei) in finalParentMapping) {
            val companyId = searchCompanyByLEI(startLei) ?: continue
            logger.info("Updating relationship of company with ID: $companyId and LEI: $startLei")

            executeWithRetryAndThrottling {
                companyDataControllerApi.patchCompanyById(
                    companyId,
                    CompanyInformationPatch(parentCompanyLei = endLei),
                )
            }
        }
    }

    private fun searchCompanyByLEI(lei: String): String? {
        var companyId: String? = null
        var found404 = false

        executeWithRetryAndThrottling {
            logger.info("Searching for company with LEI: $lei")
            try {
                companyId =
                    companyDataControllerApi
                        .getCompanyIdByIdentifier(IdentifierType.Lei, lei)
                        .companyId
            } catch (exception: ClientException) {
                if (exception.statusCode == HttpStatus.NOT_FOUND.value()) {
                    logger.warn("Could not find company with LEI: $lei")
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
        @Suppress("unused")
        for ((lei, newIsins) in leiIsinMapping) {
            val companyId = searchCompanyByLEI(lei) ?: continue
            logger.info("Patching company with ID: $companyId and LEI: $lei")
            updateIsinsOfCompany(newIsins, companyId)
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
            companyDataControllerApi.patchCompanyById(
                companyId,
                companyPatch,
            )
        }
    }
}
