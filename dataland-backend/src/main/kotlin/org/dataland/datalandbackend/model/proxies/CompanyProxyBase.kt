package org.dataland.datalandbackend.model.proxies

import com.fasterxml.jackson.annotation.JsonIgnore
import org.dataland.datalandbackendutils.utils.ValidationUtils.convertToUUID

/**
 * Base interface for company proxy representations with generic ID types.
 */
interface CompanyProxyBase {
    val proxiedCompanyId: Any
    val proxyCompanyId: Any
    val framework: String?
    val reportingPeriod: String?

    /**
     * Converts this CompanyProxy with generic ID type to a CompanyProxy with UUIDs.
     */
    @JsonIgnore
    fun convertToCompanyProxyWithUUIDs(): CompanyProxyUUID =
        CompanyProxyUUID(
            proxiedCompanyId = convertToUUID(this.proxiedCompanyId.toString()),
            proxyCompanyId = convertToUUID(this.proxyCompanyId.toString()),
            framework = this.framework,
            reportingPeriod = this.reportingPeriod,
        )
}
