package org.dataland.frameworktoolbox.intermediate.datapoints

import org.dataland.frameworktoolbox.specific.datamodel.TypeReference

data object SimpleDocumentSupport : DocumentSupport {
    override fun getJvmTypeReference(innerType: TypeReference, nullable: Boolean): TypeReference {
        return TypeReference(
            "org.dataland.datalandbackend.model.datapoints.BaseDataPoint",
            nullable,
            listOf(innerType),
        )
    }
}
