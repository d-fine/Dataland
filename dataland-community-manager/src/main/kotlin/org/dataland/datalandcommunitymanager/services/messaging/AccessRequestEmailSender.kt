package org.dataland.datalandcommunitymanager.services.messaging

import org.dataland.datalandcommunitymanager.entities.DataRequestEntity
import org.dataland.datalandcommunitymanager.model.companyRoles.CompanyRole
import org.dataland.datalandcommunitymanager.services.CompanyRolesManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service("AccessRequestEmailSender")
class AccessRequestEmailSender(
    @Autowired private val companyRolesManager: CompanyRolesManager,
) {

    fun notifyRequesterAboutGrantedRequest(dataRequestEntity: DataRequestEntity, correlationId: String) {
        // send email to dataRequestEntity.userId
        // that he has been granted data access
        return
    }

    fun notifyCompanyOwnerOrContactsAboutNewRequest(dataRequestEntity: DataRequestEntity, correlationId: String) {
        // get reciever
        // set email

        val companyOwnerList = companyRolesManager.getCompanyRoleAssignmentsByParameters(
            companyRole = CompanyRole.CompanyOwner,
            companyId = dataRequestEntity.datalandCompanyId,
            userId = null,
        )
        // die haben eine user id an die wir schicken koennen
        // an die schicken wir die email
        // ansonsten an die contacts?
        // dataRequestEntity.messageHistory.last().contacts
        // ohne contacts keinen eintrag in die history
        // also contacts sollten irgendwie da sein, aber wenn die kein owner sind
        return
    }
}
