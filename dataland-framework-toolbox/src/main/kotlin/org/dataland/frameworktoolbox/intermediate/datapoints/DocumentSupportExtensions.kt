package org.dataland.frameworktoolbox.intermediate.datapoints

import org.dataland.frameworktoolbox.specific.datamodel.Annotation
import org.dataland.frameworktoolbox.specific.datamodel.TypeReference
import org.dataland.frameworktoolbox.specific.datamodel.elements.DataClassBuilder

/**
 * Add anew property to the dataclass respecting the standard required document-support wrapping.
 */
fun DataClassBuilder.addPropertyWithDocumentSupport(
    documentSupport: DocumentSupport,
    name: String,
    type: TypeReference,
    annotations: List<Annotation> = emptyList(),
) {
    addProperty(
        name,
        documentSupport.getJvmTypeReference(
            type,
            type.nullable,
        ),
        annotations + documentSupport.getJvmAnnotations(),
    )
}
