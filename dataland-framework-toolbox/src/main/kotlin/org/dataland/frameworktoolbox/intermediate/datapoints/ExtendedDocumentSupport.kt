package org.dataland.frameworktoolbox.intermediate.datapoints

import org.dataland.frameworktoolbox.specific.datamodel.TypeReference

data object ExtendedDocumentSupport : DocumentSupport {
    override fun getJvmTypeReference(innerType: TypeReference, nullable: Boolean): TypeReference {
        return TypeReference(
            "org.dataland.datalandbackend.model.datapoints.ExtendedDataPoint",
            nullable,
            listOf(innerType),
        )
    }
}
