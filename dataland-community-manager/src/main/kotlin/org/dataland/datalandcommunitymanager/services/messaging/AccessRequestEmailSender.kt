package org.dataland.datalandcommunitymanager.services.messaging

import org.dataland.datalandcommunitymanager.model.companyRoles.CompanyRole
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
    )

    fun notifyCompanyOwnerAboutNewRequest(emailInformation: RequestEmailInformation, correlationId: String) {
        val user = keycloakUserControllerApiService.getUser(emailInformation.requesterUserId)

        // was ist mit dem company name?

        // get reciever
        // set email

        // die logik muss woanders rein, da wir das ja generischer umsetzen
        val companyOwnerList = companyRolesManager.getCompanyRoleAssignmentsByParameters(
            companyRole = CompanyRole.CompanyOwner,
            companyId = emailInformation.datalandCompanyId,
            userId = null,
        )

        // send email to companyOwnerList[0].userId
        // haben evtl. namen und nachnamen des users
        // haben auf jeden fall die email Adresse
        // Message irgendwie bekommen

        // die haben eine user id an die wir schicken koennen
        // an die schicken wir die email
        // ansonsten an die contacts?
        // dataRequestEntity.messageHistory.last().contacts
        // ohne contacts keinen eintrag in die history
        // also contacts sollten irgendwie da sein, aber wenn die kein owner sind
        return
    }
}
