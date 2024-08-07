package org.dataland.frameworktoolbox.intermediate.datapoints

import org.dataland.frameworktoolbox.specific.datamodel.Annotation
import org.dataland.frameworktoolbox.specific.datamodel.TypeReference
import org.dataland.frameworktoolbox.specific.datamodel.annotations.ValidAnnotation
import org.dataland.frameworktoolbox.specific.viewconfig.functional.FrameworkDisplayValueLambda

/**
 * Elements marked with ExtendedDocumentSupport are converted to ExtendedDataPoints
 */
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
        dataPointAccessor: String,
    ): FrameworkDisplayValueLambda {
        return SimpleDocumentSupport.getFrameworkDisplayValueLambda(innerLambda, fieldLabel, dataPointAccessor)
    }

    override fun getDataAccessor(dataPointAccessor: String, nullable: Boolean): String {
        return SimpleDocumentSupport.getDataAccessor(dataPointAccessor, nullable)
    }

    override fun getFixtureExpression(
        nullableFixtureExpression: String,
        fixtureExpression: String,
        nullable: Boolean,
    ): String {
        return if (nullable) {
            "dataGenerator.randomExtendedDataPoint($nullableFixtureExpression)"
        } else {
            "dataGenerator.guaranteedExtendedDataPoint($fixtureExpression)"
        }
    }

    override fun getJvmAnnotations(): List<Annotation> {
        return listOf(ValidAnnotation)
    }
}
