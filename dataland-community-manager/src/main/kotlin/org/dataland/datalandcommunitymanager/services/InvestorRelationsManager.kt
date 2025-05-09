package org.dataland.datalandcommunitymanager.services

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.api.MetaDataControllerApi
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

/**
 * A class for handling the investor-relationships part of the "QA Status Accepted" pipeline.
 * Notification events are generated, when a dataset is available and the related company ownership is claimable
 */
@Service
class InvestorRelationsManager
    @Autowired
    constructor(
        private val companyDataControllerApi: CompanyDataControllerApi,
        private val metaDataControllerApi: MetaDataControllerApi,
        private val investorRelationsNotificationService: InvestorRelationsNotificationService,
    ) {
        private fun getContactEmailsForCompany(companyId: String): List<String>? {
            val companyInfo = companyDataControllerApi.getCompanyInfo(companyId)
            return companyInfo.companyContactDetails
        }

        /**
         * Checks whether the company of the dataset associated with the given id has
         * contact emails specified and, if so, creates the relevant company-specific
         * notification event for the weekly scheduler.
         */
        fun saveNotificationEventForInvestorRelationsEmails(dataId: String) {
            val metaInfo = metaDataControllerApi.getDataMetaInfo(dataId)
            val companyId = metaInfo.companyId
            val contactEmails = getContactEmailsForCompany(companyId)
            if (!contactEmails.isNullOrEmpty()) {
                investorRelationsNotificationService.createCompanySpecificNotificationEvent(metaInfo)
            }
        }
    }
