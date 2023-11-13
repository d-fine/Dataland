package org.dataland.frameworktoolbox.intermediate.datapoints

import org.dataland.frameworktoolbox.specific.datamodel.TypeReference
import org.dataland.frameworktoolbox.specific.viewconfig.functional.FrameworkDisplayValueLambda

data object ExtendedDocumentSupport : DocumentSupport {
    override fun getJvmTypeReference(innerType: TypeReference, nullable: Boolean): TypeReference {
        return TypeReference(
            "org.dataland.datalandbackend.model.datapoints.ExtendedDataPoint",
            nullable,
            listOf(innerType),
        )
    }

    override fun getFrameworkDisplayValueLambda(
        innerLambda: FrameworkDisplayValueLambda,
        fieldLabel: String?,
        dataPointAccessor: String
    ): FrameworkDisplayValueLambda {
        return SimpleDocumentSupport.getFrameworkDisplayValueLambda(innerLambda, fieldLabel, dataPointAccessor)
    }

    override fun getDataAccessor(dataPointAccessor: String, nullable: Boolean): String {
        return SimpleDocumentSupport.getDataAccessor(dataPointAccessor, nullable)
    }
}
