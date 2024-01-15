package org.dataland.datalandbackend.frameworks.esgquestionnaire.custom

import io.swagger.v3.oas.models.OpenAPI
import org.springdoc.core.customizers.OpenApiCustomizer
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.stereotype.Component

/**
 * OpenAPI Customizer for setting the example dataset for the GDV framework
 */
@Component
class OpenApiEsgQuestionnaireExampleCustomizer(
    @Value("classpath:org/dataland/datalandbackend/frameworks/esgQuestionnaire/EsgQuestionnaireExampleDataset.json")
    private val esgQuestionnaireExampleJsonResource: Resource,
) : OpenApiCustomizer {

    private fun readGdvOpenApiExample(): String {
        return esgQuestionnaireExampleJsonResource.getContentAsString(Charsets.UTF_8)
    }
    override fun customise(openApi: OpenAPI) {
        val companyAssociatedEsgquestionnaireDataSchema =
            openApi.components.schemas["CompanyAssociatedDataEsgquestionnaireData"]
        requireNotNull(companyAssociatedEsgquestionnaireDataSchema)
        companyAssociatedEsgquestionnaireDataSchema.example = readGdvOpenApiExample()
    }
}
