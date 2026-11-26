package org.dataland.datalanduserservice.service

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.infrastructure.ClientException
import org.dataland.datalandbackend.openApiClient.infrastructure.ServerException
import org.dataland.datalanduserservice.utils.isMissingFiscalYearEndOrSector
import org.slf4j.LoggerFactory
import org.springframework.amqp.AmqpException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.io.IOException

/**
 * Checks companies for a missing fiscal year end or sector and publishes an event for each incomplete company.
 */
@Service
class IncompleteCompanyEventService
    @Autowired
    constructor(
        private val companyDataControllerApi: CompanyDataControllerApi,
        private val messageQueuePublisherService: MessageQueuePublisherService,
    ) {
        private val logger = LoggerFactory.getLogger(javaClass)

        /**
         * Publishes an event for every given company that lacks a fiscal year end or a sector.
         * Backend, network and message queue failures for one company are logged and do not affect the others or the
         * caller. Any other exception is a bug and is propagated.
         *
         * @param companyIds the IDs of the companies to check
         * @param correlationId the correlation ID of the request initiating the check
         */
        fun publishEventsForIncompleteCompanies(
            companyIds: Set<String>,
            correlationId: String,
        ) {
            companyIds.forEach { companyId ->
                try {
                    val companyInformation = companyDataControllerApi.getCompanyById(companyId).companyInformation
                    if (companyInformation.isMissingFiscalYearEndOrSector()) {
                        messageQueuePublisherService.publishCompanyInformationIncomplete(companyId, correlationId)
                    }
                } catch (exception: ClientException) {
                    logSkippedCompany(companyId, correlationId, exception)
                } catch (exception: ServerException) {
                    logSkippedCompany(companyId, correlationId, exception)
                } catch (exception: IOException) {
                    logSkippedCompany(companyId, correlationId, exception)
                } catch (exception: AmqpException) {
                    logSkippedCompany(companyId, correlationId, exception)
                }
            }
        }

        private fun logSkippedCompany(
            companyId: String,
            correlationId: String,
            exception: Exception,
        ) {
            logger.error(
                "Could not check or publish incomplete company information for company '$companyId'. " +
                    "Correlation ID: '$correlationId'.",
                exception,
            )
        }
    }
