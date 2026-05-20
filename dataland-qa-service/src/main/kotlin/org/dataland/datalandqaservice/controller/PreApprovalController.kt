package org.dataland.datalandqaservice.org.dataland.datalandqaservice.controller

import org.dataland.datalandqaservice.org.dataland.datalandqaservice.api.PreApprovalApi
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.model.PreApprovalConfig
import org.dataland.datalandqaservice.org.dataland.datalandqaservice.services.PreApprovalService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

/**
 * Controller for the pre-approval configuration.
 */
@RestController
class PreApprovalController(
    @Autowired private val preApprovalService: PreApprovalService,
) : PreApprovalApi {
    override fun getPreApprovalConfig(): ResponseEntity<PreApprovalConfig> = ResponseEntity.ok(preApprovalService.getConfig())

    override fun patchPreApprovalConfig(newConfig: PreApprovalConfig): ResponseEntity<PreApprovalConfig> =
        ResponseEntity.ok(preApprovalService.patchConfig(newConfig))
}
