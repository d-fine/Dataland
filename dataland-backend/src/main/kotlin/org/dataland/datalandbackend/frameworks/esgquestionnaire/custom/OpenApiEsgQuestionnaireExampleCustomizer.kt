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
    private val esgQuestionnaireExampleJsonResource: Resource,
) : OpenApiCustomizer {
    private fun readEsgQuestionnaireOpenApiExample(): String = esgQuestionnaireExampleJsonResource.getContentAsString(Charsets.UTF_8)

    override fun customise(openApi: OpenAPI) {
        val companyAssociatedEsgQuestionnaireDataSchema =
            openApi.components.schemas["CompanyAssociatedDataEsgQuestionnaireData"]
        requireNotNull(companyAssociatedEsgQuestionnaireDataSchema)
        companyAssociatedEsgQuestionnaireDataSchema.example = readEsgQuestionnaireOpenApiExample()
    }
}
