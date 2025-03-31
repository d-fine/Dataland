package org.dataland.datalandcommunitymanager.services

import org.dataland.datalandbackend.openApiClient.api.CompanyDataControllerApi
import org.dataland.datalandbackend.openApiClient.api.MetaDataControllerApi
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

/**
 * A class for handling the investor-relationships part of the "QA Status Accepted" pipeline.
 */
@Service
class InvestorRelationshipsManager(
    @Autowired private val companyDataControllerApi: CompanyDataControllerApi,
    @Autowired private val metaDataControllerApi: MetaDataControllerApi,
    @Autowired private val investorRelationshipNotificationService: InvestorRelationshipNotificationService,
) {
    private fun getIRContactEmailsForCompany(companyId: String): List<String>? {
        val companyInfo = companyDataControllerApi.getCompanyInfo(companyId)
        return companyInfo.companyContactDetails
    }

    /**
     * Checks whether the company of the dataset associated with the given id has IR
     * contact emails specified and, if so, creates the relevant company-specific
     * notification event for the weekly scheduler.
     */
    fun saveNotificationEventForIREmails(dataId: String) {
        val metaInfo = metaDataControllerApi.getDataMetaInfo(dataId)
        val companyId = metaInfo.companyId
        val irContactEmails = getIRContactEmailsForCompany(companyId)
        if (!irContactEmails.isNullOrEmpty()) {
            investorRelationshipNotificationService.createCompanySpecificNotificationEvent(metaInfo)
        }
    }
}
