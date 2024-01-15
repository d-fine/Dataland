package org.dataland.datalandbackend.frameworks.gdv.custom

import io.swagger.v3.oas.models.OpenAPI
import org.springdoc.core.customizers.OpenApiCustomizer
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.stereotype.Component

/**
 * OpenAPI Customizer for setting the example dataset for the GDV framework
 */
@Component
class OpenApiGdvExampleCustomizer(
    @Value("classpath:org/dataland/datalandbackend/frameworks/gdv/GdvExampleDataset.json")
    private val gdvExampleJsonResource: Resource,
) : OpenApiCustomizer {

    private fun readGdvOpenApiExample(): String {
        return gdvExampleJsonResource.getContentAsString(Charsets.UTF_8)
    }
    override fun customise(openApi: OpenAPI) {
        val companyAssociatedGdvDataSchema = openApi.components.schemas["CompanyAssociatedDataGdvData"]
        requireNotNull(companyAssociatedGdvDataSchema)
        companyAssociatedGdvDataSchema.example = readGdvOpenApiExample()
    }
}
