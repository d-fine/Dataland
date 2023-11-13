package org.dataland.frameworktoolbox.intermediate.datapoints

import org.dataland.frameworktoolbox.specific.datamodel.TypeReference

data object NoDocumentSupport : DocumentSupport {
    override fun getJvmTypeReference(innerType: TypeReference, nullable: Boolean): TypeReference {
        return innerType
    }
}
