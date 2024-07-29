package org.dataland.frameworktoolbox.specific.qamodel

import org.dataland.frameworktoolbox.intermediate.datapoints.DocumentSupport
import org.dataland.frameworktoolbox.specific.datamodel.Annotation
import org.dataland.frameworktoolbox.specific.datamodel.TypeReference
import org.dataland.frameworktoolbox.specific.datamodel.elements.DataClassBuilder

/**
 * Adds a property to the data class builder with the given document support.
 */
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

/**
 * Given a type reference to the dataland backend, returns the corresponding type reference as it's made available
 * int the OpenApi client.
 */
fun TypeReference.getBackendClientTypeReference(): TypeReference {
    val genericNameExtension = genericTypeParameters?.joinToString { it.name } ?: ""
    val expectedName = name + genericNameExtension

    return TypeReference(
        fullyQualifiedName = "org.dataland.datalandbackend.openApiClient.model.$expectedName",
        nullable = nullable,
    )
}
