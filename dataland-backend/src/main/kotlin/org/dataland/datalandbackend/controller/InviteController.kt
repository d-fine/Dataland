package org.dataland.datalandbackend.controller

import org.dataland.datalandbackend.api.InviteApi
import org.dataland.datalandbackend.entities.InviteMetaInfoEntity
import org.dataland.datalandbackend.services.InviteManager
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

/**
 * Controller for the file endpoints
 * @param inviteManager the file manager service to handle files
 */

@RestController
class InviteController(
    @Autowired var inviteManager: InviteManager,
) : InviteApi {
    private val logger = LoggerFactory.getLogger(javaClass)

    override fun submitInvite(
        excelFile: MultipartFile,
        isSubmitterNameHidden: Boolean
    ): ResponseEntity<InviteMetaInfoEntity> {
        logger.info(
            "Received a request to submit an invite. " +
                "Hiding the requester is set to $isSubmitterNameHidden."
        )
        return ResponseEntity.ok(inviteManager.submitInvitation(excelFile, isSubmitterNameHidden))
    }
}
