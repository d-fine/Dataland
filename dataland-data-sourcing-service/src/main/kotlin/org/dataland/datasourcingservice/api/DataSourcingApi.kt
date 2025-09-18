package org.dataland.datasourcingservice.api

import io.swagger.v3.oas.annotations.security.SecurityRequirement
import org.springframework.web.bind.annotation.RequestMapping

/**
 * API interface for handling data-sourcing operations.
 */
@SecurityRequirement(name = "default-bearer-auth")
@SecurityRequirement(name = "default-oauth")
@RequestMapping("/sourcing")
interface DataSourcingApi {
}
