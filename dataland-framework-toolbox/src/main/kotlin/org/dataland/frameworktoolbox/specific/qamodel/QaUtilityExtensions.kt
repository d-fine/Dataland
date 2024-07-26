package org.dataland.frameworktoolbox.specific.qamodel

import org.dataland.frameworktoolbox.intermediate.datapoints.DocumentSupport
import org.dataland.frameworktoolbox.specific.datamodel.Annotation
import org.dataland.frameworktoolbox.specific.datamodel.TypeReference
import org.dataland.frameworktoolbox.specific.datamodel.elements.DataClassBuilder

fun DataClassBuilder.addQaPropertyWithDocumentSupport(
    documentSupport: DocumentSupport,
    name: String,
    type: TypeReference,
    annotations: List<Annotation> = emptyList(),
) {
    val qaJvmTypeReference = documentSupport.getQaJvmTypeReference(
        type,
        type.nullable,
    )

    if (qaJvmTypeReference != null) {
        addProperty(
            name,
            qaJvmTypeReference,
            annotations + documentSupport.getJvmAnnotations(),
        )
    }
}

fun TypeReference.getBackendClientTypeReference(): TypeReference {
    val genericNameExtension = genericTypeParameters?.joinToString { it.name } ?: ""
    val expectedName = name + genericNameExtension

    return TypeReference(
        fullyQualifiedName = "org.dataland.datalandbackend.openApiClient.model.$expectedName",
        nullable = nullable,
    )
}
