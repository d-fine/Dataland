package org.dataland.datalanduserservice.service

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.infrastructure.ClientException
import org.dataland.datalandbackend.openApiClient.infrastructure.ServerException
import org.dataland.datalandbackendutils.exceptions.ResourceNotFoundApiException
import org.dataland.datalanduserservice.entity.IncompleteCompanyInformationEntity
import org.dataland.datalanduserservice.repository.IncompleteCompanyInformationRepository
import org.dataland.datalanduserservice.utils.isMissingFiscalYearEndOrSector
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.io.IOException

/**
 * Manages the persistent human log of companies whose fiscal year end or sector could not be completed by the AI.
 */
@Service
class IncompleteCompanyInformationService
    @Autowired
    constructor(
        private val incompleteCompanyInformationRepository: IncompleteCompanyInformationRepository,
        private val companyDataControllerApi: CompanyDataControllerApi,
    ) {
        private val logger = LoggerFactory.getLogger(javaClass)

        /**
         * Adds a company to the human log. Does nothing if the company is already logged.
         *
         * @param companyId the ID of the company
         * @param correlationId the correlation ID of the request
         * @throws ResourceNotFoundApiException if the backend does not know the company
         */
        fun registerIncompleteCompany(
            companyId: String,
            correlationId: String,
        ) {
            try {
                companyDataControllerApi.getCompanyById(companyId)
            } catch (exception: ClientException) {
                if (exception.statusCode == HttpStatus.NOT_FOUND.value()) {
                    throw ResourceNotFoundApiException(
                        summary = "Company with CompanyId $companyId not found.",
                        message = "Company with CompanyId $companyId not found. CorrelationId: $correlationId",
                    )
                }
                throw exception
            }
            if (incompleteCompanyInformationRepository.existsById(companyId)) return
            try {
                incompleteCompanyInformationRepository.saveAndFlush(IncompleteCompanyInformationEntity(companyId))
            } catch (exception: DataIntegrityViolationException) {
                logger.debug(
                    "Company '$companyId' was logged concurrently. Correlation ID: '$correlationId'.",
                    exception,
                )
            }
        }

        /**
         * Returns the IDs of all logged companies that are still incomplete. Entries of companies that are now complete
         * or no longer exist are removed from the log. Entries are kept if the backend cannot be reached reliably.
         */
        @Transactional
        fun getIncompleteCompanyIds(): Set<String> {
            val remainingCompanyIds = mutableSetOf<String>()
            incompleteCompanyInformationRepository.findAll().forEach { entry ->
                val companyId = entry.companyId
                try {
                    if (companyDataControllerApi
                            .getCompanyById(companyId)
                            .companyInformation
                            .isMissingFiscalYearEndOrSector()
                    ) {
                        remainingCompanyIds.add(companyId)
                    } else {
                        incompleteCompanyInformationRepository.deleteById(companyId)
                    }
                } catch (exception: ClientException) {
                    if (exception.statusCode == HttpStatus.NOT_FOUND.value()) {
                        incompleteCompanyInformationRepository.deleteById(companyId)
                    } else {
                        keepEntry(companyId, remainingCompanyIds, exception)
                    }
                } catch (exception: ServerException) {
                    keepEntry(companyId, remainingCompanyIds, exception)
                } catch (exception: IOException) {
                    keepEntry(companyId, remainingCompanyIds, exception)
                }
            }
            return remainingCompanyIds
        }

        private fun keepEntry(
            companyId: String,
            remainingCompanyIds: MutableSet<String>,
            exception: Exception,
        ) {
            logger.error("Could not check company '$companyId'. Keeping its entry in the human log.", exception)
            remainingCompanyIds.add(companyId)
        }
    }
