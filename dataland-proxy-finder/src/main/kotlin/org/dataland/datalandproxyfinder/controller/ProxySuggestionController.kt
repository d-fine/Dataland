package org.dataland.datalandproxyfinder.controller

import org.dataland.datalandbackendutils.model.proxyfinder.ProxySuggestion
import org.dataland.datalandproxyfinder.api.ProxySuggestionApi
import org.dataland.datalandproxyfinder.services.ProxyFinderService
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
