package org.dataland.datalandbackend.frameworks.esgdatenkatalog.custom

import io.swagger.v3.oas.models.OpenAPI
import org.springdoc.core.customizers.OpenApiCustomizer
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.stereotype.Component

/**
 * OpenAPI Customizer for setting the example dataset for the Esg Datenkatalog framework
 */
@Component
class OpenApiEsgDatenkatalogExampleCustomizer(
    @Value("classpath:org/dataland/datalandbackend/frameworks/esgdatenkatalog/EsgDatenkatalogExampleDataset.json")
    private val esgDatenkatalogExampleJsonResource: Resource,
) : OpenApiCustomizer {
    private fun readEsgDatenkatalogOpenApiExample(): String = esgDatenkatalogExampleJsonResource.getContentAsString(Charsets.UTF_8)

    override fun customise(openApi: OpenAPI) {
        val companyAssociatedEsgDatenkatalogDataSchema =
            openApi.components.schemas["CompanyAssociatedDataEsgDatenkatalogData"]
        requireNotNull(companyAssociatedEsgDatenkatalogDataSchema)
        companyAssociatedEsgDatenkatalogDataSchema.example = readEsgDatenkatalogOpenApiExample()
    }
}
