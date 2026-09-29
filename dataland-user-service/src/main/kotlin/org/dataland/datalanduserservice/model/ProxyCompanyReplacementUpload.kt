package org.dataland.datalanduserservice.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.GeneralOpenApiDescriptionsAndExamples
import org.dataland.datalandbackendutils.utils.swaggerdocumentation.UserServiceOpenApiDescriptionsAndExamples

/**
 * --- API model ---
 * API model for the user-supplied part of a proxy company replacement (POST/PUT).
 * The userId and timestamp are not user-supplied; they are stamped by the server during upload.
 */
data class ProxyCompanyReplacementUpload(
    @field:JsonProperty(required = true)
    @field:Schema(
        description = UserServiceOpenApiDescriptionsAndExamples.PROXIED_COMPANY_ID_DESCRIPTION,
        example = GeneralOpenApiDescriptionsAndExamples.COMPANY_ID_EXAMPLE,
    )
    val proxiedCompanyId: String,
    @field:JsonProperty(required = true)
    @field:Schema(
        description = UserServiceOpenApiDescriptionsAndExamples.PROXY_COMPANY_ID_DESCRIPTION,
        example = GeneralOpenApiDescriptionsAndExamples.COMPANY_ID_EXAMPLE,
    )
    val proxyCompanyId: String,
)
