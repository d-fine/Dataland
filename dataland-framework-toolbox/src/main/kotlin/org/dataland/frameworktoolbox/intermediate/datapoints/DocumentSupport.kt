package org.dataland.frameworktoolbox.intermediate.datapoints

import org.dataland.frameworktoolbox.specific.datamodel.TypeReference
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
}
