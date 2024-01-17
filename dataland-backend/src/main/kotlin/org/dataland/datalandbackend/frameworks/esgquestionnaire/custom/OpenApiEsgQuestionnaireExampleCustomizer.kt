package org.dataland.datalandbackend.frameworks.esgquestionnaire.custom

import io.swagger.v3.oas.models.OpenAPI
import org.springdoc.core.customizers.OpenApiCustomizer
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.stereotype.Component

/**
 * OpenAPI Customizer for setting the example dataset for the Esg Questionnaire framework
 */
@Component
class OpenApiEsgQuestionnaireExampleCustomizer(
    @Value("classpath:org/dataland/datalandbackend/frameworks/esgquestionnaire/EsgQuestionnaireExampleDataset.json")
    private val gdvExampleJsonResource: Resource, // TODO renamings
) : OpenApiCustomizer {

    private fun readGdvOpenApiExample(): String {
        return gdvExampleJsonResource.getContentAsString(Charsets.UTF_8)
    }
    override fun customise(openApi: OpenAPI) {
        val companyAssociatedGdvDataSchema = openApi.components.schemas["CompanyAssociatedDataEsgQuestionnaireData"]
        requireNotNull(companyAssociatedGdvDataSchema)
        companyAssociatedGdvDataSchema.example = readGdvOpenApiExample()
    }
}
