package org.dataland.datalanduserservice.utils

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.infrastructure.ClientException
import org.dataland.datalandbackendutils.exceptions.ConflictApiException
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalanduserservice.exceptions.PortfolioNotFoundApiException
import org.dataland.datalanduserservice.model.PortfolioUpload
import org.dataland.datalanduserservice.service.PortfolioService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service

/**
 * Validator class to validate user input within controller.
 */
@Service
class Validator
    @Autowired
    constructor(
        private val companyDataControllerApi: CompanyDataControllerApi,
        private val portfolioService: PortfolioService,
    ) {
        /**
         * Validates an uploaded portfolio on creation (POST).
         * Checks that provided portfolio name is unique and that companyIds are valid.
         */
        fun validatePortfolioCreation(
            portfolioUpload: PortfolioUpload,
            correlationId: String,
        ) {
            if (portfolioService.existsPortfolioWithNameForUser(portfolioUpload.portfolioName, correlationId)) {
                throw ConflictApiException(
                    message = "Conflicting input detected.",
                    summary =
                        "Conflicting input detected for portfolio with portfolioName $portfolioUpload.portfolioName." +
                            " Please ensure that portfolio names are unique. CorrelationId: $correlationId",
                )
            }
            portfolioUpload.companyIds.forEach { isCompanyIdValid(it, correlationId) }
        }

        /**
         * Validates an uploaded portfolio on replacement (PUT).
         * Checks that portfolio to be replaced exists; checks that replacing portfolio has a unique name; checks that
         * companyIds are valid.
         */
        fun validatePortfolioReplacement(
            portfolioId: String,
            portfolioUpload: PortfolioUpload,
            correlationId: String,
        ) {
            if (!portfolioService.existsPortfolioForUser(portfolioId, correlationId)) {
                throw PortfolioNotFoundApiException(portfolioId)
            }
            val portfolioToBeReplaced = portfolioService.getPortfolioForUser(portfolioId)
            if (portfolioService.existsPortfolioWithNameForUser(portfolioUpload.portfolioName, correlationId) &&
                portfolioToBeReplaced.portfolioName != portfolioUpload.portfolioName
            ) {
                throw ConflictApiException(
                    message = "Conflicting input detected.",
                    summary =
                        "Conflicting input detected for portfolio with portfolioName $portfolioUpload.portfolioName." +
                            " Please ensure that portfolio names are unique. CorrelationId: $correlationId",
                )
            }
            portfolioUpload.companyIds.forEach { isCompanyIdValid(it, correlationId) }
        }

        /**
         * Checks if passed companyId is valid by calling respective HEAD endpoint in backend companyDataController
         * @param companyId
         * @return returns true if companyId is valid
         */
        private fun isCompanyIdValid(
            companyId: String,
            correlationId: String,
        ): Boolean {
            try {
                companyDataControllerApi.isCompanyIdValid(companyId)
                return true
            } catch (exception: ClientException) {
                if (exception.statusCode == HttpStatus.NOT_FOUND.value()) {
                    throw ResourceNotFoundApiException(
                        summary = "Company with CompanyId $companyId not found.",
                        message = "Company with CompanyId $companyId not found. CorrelationId: $correlationId",
                    )
                } else {
                    throw exception
                }
            }
        }
    }
