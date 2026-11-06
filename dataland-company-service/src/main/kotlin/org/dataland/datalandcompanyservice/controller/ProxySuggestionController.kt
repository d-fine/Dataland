package org.dataland.datalandcompanyservice.controller

import org.dataland.datalandbackendutils.model.proxyfinder.ProxySuggestion
import org.dataland.datalandcompanyservice.api.ProxySuggestionApi
import org.dataland.datalandcompanyservice.services.ProxyFinderService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController

/**
 * Controller for the proxy suggestion endpoints.
 */
@RestController
class ProxySuggestionController(
    private val proxyFinderService: ProxyFinderService,
) : ProxySuggestionApi {
    override fun findProxySuggestion(companyId: String): ResponseEntity<ProxySuggestion> =
        ResponseEntity.ok(proxyFinderService.findProxy(companyId))
}
