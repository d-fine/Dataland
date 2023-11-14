package org.dataland.frameworktoolbox.intermediate.datapoints

import org.dataland.frameworktoolbox.specific.datamodel.TypeReference
import org.dataland.frameworktoolbox.specific.viewconfig.functional.FrameworkDisplayValueLambda
import org.dataland.frameworktoolbox.template.model.TemplateDocumentSupport

sealed interface DocumentSupport {
    companion object {
        fun fromTemplate(templateDocumentSupport: TemplateDocumentSupport): DocumentSupport {
            return when (templateDocumentSupport) {
                TemplateDocumentSupport.None -> NoDocumentSupport
                TemplateDocumentSupport.Simple -> SimpleDocumentSupport
                TemplateDocumentSupport.Extended -> ExtendedDocumentSupport
            }
        }
    }

    fun getJvmTypeReference(innerType: TypeReference, nullable: Boolean): TypeReference

    fun getFrameworkDisplayValueLambda(
        innerLambda: FrameworkDisplayValueLambda,
        fieldLabel: String?,
        dataPointAccessor: String,
    ): FrameworkDisplayValueLambda

    fun getDataAccessor(dataPointAccessor: String, nullable: Boolean): String

    fun getFixtureExpression(nullableFixtureExpression: String, fixtureExpression: String, nullable: Boolean): String
}
