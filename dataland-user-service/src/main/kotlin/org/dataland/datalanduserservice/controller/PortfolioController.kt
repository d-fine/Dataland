package org.dataland.datalanduserservice.controller

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalandbackendutils.utils.ValidationUtils
import org.dataland.datalanduserservice.api.PortfolioApi
import org.dataland.datalanduserservice.api.PortfolioSharingApi
import org.dataland.datalanduserservice.model.BasePortfolio
import org.dataland.datalanduserservice.model.BasePortfolioName
import org.dataland.datalanduserservice.model.EnrichedPortfolio
import org.dataland.datalanduserservice.model.PortfolioMonitoringPatch
import org.dataland.datalanduserservice.model.PortfolioSharingPatch
import org.dataland.datalanduserservice.model.PortfolioUpload
import org.dataland.datalanduserservice.model.SupportRequestData
import org.dataland.datalanduserservice.service.MessageQueuePublisherService
import org.dataland.datalanduserservice.service.PortfolioEnrichmentService
import org.dataland.datalanduserservice.service.PortfolioMonitoringService
import org.dataland.datalanduserservice.service.PortfolioService
import org.dataland.datalanduserservice.service.PortfolioSharingService
import org.dataland.datalanduserservice.utils.Validator
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

/**
 * RestController for the Portfolio API
 */
@Suppress("TooManyFunctions")
@RestController
class PortfolioController
    @Autowired
    constructor(
        private val portfolioService: PortfolioService,
        private val validator: Validator,
        private val portfolioEnrichmentService: PortfolioEnrichmentService,
        private val portfolioMonitoringService: PortfolioMonitoringService,
        private val portfolioSharingService: PortfolioSharingService,
        private val messageQueuePublisherService: MessageQueuePublisherService,
        private val companyDataControllerApi: CompanyDataControllerApi,
    ) : PortfolioApi,
        PortfolioSharingApi {
        override fun getAllPortfoliosForCurrentUser(): ResponseEntity<List<BasePortfolio>> =
            ResponseEntity.ok(portfolioService.getAllPortfoliosForUser())

        override fun getPortfolio(portfolioId: String): ResponseEntity<BasePortfolio> =
            ResponseEntity.ok(portfolioService.getPortfolio(portfolioId))

        override fun getPortfoliosForUser(userId: String): ResponseEntity<List<BasePortfolio>> =
            ResponseEntity.ok(portfolioService.getAllPortfoliosForUserById(userId))

        override fun getAllPortfolios(
            chunkSize: Int,
            chunkIndex: Int,
        ): ResponseEntity<List<BasePortfolio>> = ResponseEntity.ok(portfolioService.getAllPortfolios(chunkSize, chunkIndex))

        override fun createPortfolio(portfolioUpload: PortfolioUpload): ResponseEntity<BasePortfolio> {
            val correlationId = UUID.randomUUID().toString()
            validator.validatePortfolioCreation(portfolioUpload, correlationId)

            val validationResults = companyDataControllerApi.postCompanyValidation(portfolioUpload.identifiers.toList())
            val failedIdentifiers = validationResults.filter { it.companyInformation == null }

            if (failedIdentifiers.isNotEmpty()) {
                throw ResourceNotFoundApiException(
                    "Some company identifiers were invalid",
                    "The following company identifiers are invalid: $failedIdentifiers. CorrelationId: $correlationId",
                )
            }

            val validCompanyIds =
                validationResults
                    .mapNotNull { it.companyInformation?.companyId }
                    .toSet()

            val validPortfolioUpload = portfolioUpload.copy(identifiers = validCompanyIds)
            return ResponseEntity(
                portfolioService.createPortfolio(BasePortfolio(validPortfolioUpload), correlationId),
                HttpStatus.CREATED,
            )
        }

        override fun replacePortfolio(
            portfolioId: String,
            portfolioUpload: PortfolioUpload,
        ): ResponseEntity<BasePortfolio> {
            val correlationId = UUID.randomUUID().toString()
            validator.validatePortfolioReplacement(portfolioId, portfolioUpload, correlationId)
            return ResponseEntity.ok(
                portfolioService.replacePortfolio(
                    portfolioId,
                    BasePortfolio(portfolioUpload),
                    correlationId,
                ),
            )
        }

        override fun deletePortfolio(portfolioId: String): ResponseEntity<Unit> =
            ResponseEntity(portfolioService.deletePortfolio(portfolioId), HttpStatus.NO_CONTENT)

        override fun getAllPortfolioNamesForCurrentUser(): ResponseEntity<List<BasePortfolioName>> =
            ResponseEntity.ok(portfolioService.getAllPortfolioNamesForCurrentUser())

        override fun getEnrichedPortfolio(portfolioId: String): ResponseEntity<EnrichedPortfolio> =
            ResponseEntity.ok(portfolioEnrichmentService.getEnrichedPortfolio(portfolioService.getPortfolio(portfolioId)))

        override fun patchMonitoring(
            portfolioId: String,
            portfolioMonitoringPatch: PortfolioMonitoringPatch,
        ): ResponseEntity<BasePortfolio> {
            val correlationId = UUID.randomUUID().toString()
            return ResponseEntity.ok(
                portfolioMonitoringService.patchMonitoring(
                    portfolioId,
                    BasePortfolio(portfolioMonitoringPatch),
                    correlationId,
                ),
            )
        }

        override fun postSupportRequest(supportRequestData: SupportRequestData): ResponseEntity<Unit> {
            val correlationId = UUID.randomUUID().toString()
            messageQueuePublisherService.publishSupportRequest(supportRequestData, correlationId)
            return ResponseEntity.status(HttpStatus.CREATED).build()
        }

        override fun getAllSharedPortfoliosForCurrentUser(): ResponseEntity<List<BasePortfolio>> =
            ResponseEntity.ok(portfolioSharingService.getAllSharedPortfoliosForCurrentUser())

        override fun patchSharing(
            portfolioId: String,
            portfolioSharingPatch: PortfolioSharingPatch,
        ): ResponseEntity<BasePortfolio> {
            val correlationId = UUID.randomUUID().toString()
            return ResponseEntity.ok(
                portfolioSharingService.patchSharing(
                    ValidationUtils.convertToUUID(portfolioId),
                    BasePortfolio(portfolioSharingPatch),
                    correlationId,
                ),
            )
        }

        override fun deleteCurrentUserFromSharing(portfolioId: String) {
            TODO("Not yet implemented")
        }

        override fun getAllSharedPortfolioNamesForCurrentUser(): ResponseEntity<List<BasePortfolioName>> =
            ResponseEntity.ok(portfolioSharingService.getAllSharedPortfolioNamesForCurrentUser())
    }
