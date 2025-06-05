package org.dataland.datalanduserservice.controller

import org.dataland.datalanduserservice.api.PortfolioApi
import org.dataland.datalanduserservice.model.BasePortfolio
import org.dataland.datalanduserservice.model.BasePortfolioName
import org.dataland.datalanduserservice.model.EnrichedPortfolio
import org.dataland.datalanduserservice.model.PortfolioMonitoringPatch
import org.dataland.datalanduserservice.model.PortfolioUpload
import org.dataland.datalanduserservice.service.PortfolioEnrichmentService
import org.dataland.datalanduserservice.service.PortfolioService
import org.dataland.datalanduserservice.utils.Validator
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

/**
 * RestController for the Portfolio API
 */
@RestController
class PortfolioController
    @Autowired
    constructor(
        private val portfolioService: PortfolioService,
        private val validator: Validator,
        private val portfolioEnrichmentService: PortfolioEnrichmentService,
    ) : PortfolioApi {
        override fun getAllPortfoliosForCurrentUser(): ResponseEntity<List<BasePortfolio>> =
            ResponseEntity.ok(portfolioService.getAllPortfoliosForUser())

        override fun getPortfolio(portfolioId: String): ResponseEntity<BasePortfolio> =
            ResponseEntity.ok(portfolioService.getPortfolioForUser(portfolioId))

        override fun createPortfolio(portfolioUpload: PortfolioUpload): ResponseEntity<BasePortfolio> {
            val correlationId = UUID.randomUUID().toString()
            validator.validatePortfolioCreation(portfolioUpload, correlationId)
            return ResponseEntity(portfolioService.createPortfolio(BasePortfolio(portfolioUpload), correlationId), HttpStatus.CREATED)
        }

        override fun replacePortfolio(
            portfolioId: String,
            portfolioUpload: PortfolioUpload,
        ): ResponseEntity<BasePortfolio> {
            val correlationId = UUID.randomUUID().toString()
            validator.validatePortfolioReplacement(portfolioId, portfolioUpload, correlationId)
            return ResponseEntity.ok(portfolioService.replacePortfolio(portfolioId, BasePortfolio(portfolioUpload), correlationId))
        }

        override fun replacePortfolioTEST(
            portfolioId: String,
            portfolioUpload: PortfolioUpload,
        ): ResponseEntity<BasePortfolio> {
            val correlationId = UUID.randomUUID().toString()
            validator.validatePortfolioReplacement(portfolioId, portfolioUpload, correlationId)
            return ResponseEntity.ok(
                portfolioService.replacePortfolioTEST(
                    portfolioId,
                    portfolioUpload,
                    correlationId,
                ),
            )
        }

        override fun deletePortfolio(portfolioId: String): ResponseEntity<Unit> =
            ResponseEntity(portfolioService.deletePortfolio(portfolioId), HttpStatus.NO_CONTENT)

        override fun getAllPortfolioNamesForCurrentUser(): ResponseEntity<List<BasePortfolioName>> =
            ResponseEntity.ok(portfolioService.getAllPortfolioNamesForCurrentUser())

        override fun getEnrichedPortfolio(portfolioId: String): ResponseEntity<EnrichedPortfolio> =
            ResponseEntity.ok(portfolioEnrichmentService.getEnrichedPortfolio(portfolioId))

        override fun patchMonitoring(
            portfolioId: String,
            monitoringUpdate: PortfolioMonitoringPatch,
        ): ResponseEntity<Unit> {
            portfolioService.patchMonitoring(portfolioId, monitoringUpdate)
            return ResponseEntity.ok().build()
        }
    }
