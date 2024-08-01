package org.dataland.datalandcommunitymanager.services.messaging

import org.dataland.datalandcommunitymanager.entities.MessageEntity
import org.dataland.datalandcommunitymanager.services.CompanyRolesManager
import org.dataland.datalandcommunitymanager.services.KeycloakUserControllerApiService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service("AccessRequestEmailSender")
class AccessRequestEmailSender(
    @Autowired private val companyRolesManager: CompanyRolesManager,
    @Autowired private val keycloakUserControllerApiService: KeycloakUserControllerApiService,
) {

    data class GrantedEmailInformation(
        val datalandCompanyId: String,
        val dataType: String,
        val reportingPeriod: String,
        val userId: String,
    )

    fun notifyRequesterAboutGrantedRequest(emailInformation: GrantedEmailInformation, correlationId: String) {
        // send email to dataRequestEntity.userId
        // that he has been granted data access
        return
        // dataId, fuer den Link muessen wir fetchen fuer den datensatz

        // muessen nachricht an den email service senden
    }

    data class RequestEmailInformation(
        val requesterUserId: String,
        val message: String?,
        val datalandCompanyId: String,
        val dataType: String,
        val reportingPeriods: Set<String>,
        val contacts: Set<String>
    )

    fun notifyCompanyOwnerAboutNewRequest(emailInformation: RequestEmailInformation, correlationId: String) {


        val user = keycloakUserControllerApiService.getUser(emailInformation.requesterUserId)

        val contacts = emailInformation.contacts + setOf(MessageEntity.COMPANY_OWNER_KEYWORD)

        val receiver = emailInformation.contacts.flatMap {
            MessageEntity.realizeContact(it, companyRolesManager, emailInformation.datalandCompanyId)
        }

        return
    }
}
