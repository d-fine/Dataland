package org.dataland.frameworktoolbox.intermediate.datapoints

import org.dataland.frameworktoolbox.specific.datamodel.Annotation
import org.dataland.frameworktoolbox.specific.datamodel.TypeReference
import org.dataland.frameworktoolbox.specific.viewconfig.functional.FrameworkDisplayValueLambda

/**
 * Elements marked with NoDocumentSupport do not require any documentation / proof
 */
data object NoDocumentSupport : DocumentSupport {

    override fun getQaJvmTypeReference(innerType: TypeReference, nullable: Boolean): TypeReference? {
        return null
    }

    override fun getJvmTypeReference(innerType: TypeReference, nullable: Boolean): TypeReference {
        return innerType
    }

    override fun getFrameworkDisplayValueLambda(
        innerLambda: FrameworkDisplayValueLambda,
        fieldLabel: String?,
        dataPointAccessor: String,
    ): FrameworkDisplayValueLambda {
        return innerLambda
    }

    override fun getDataAccessor(dataPointAccessor: String, nullable: Boolean): String {
        return dataPointAccessor
    }

    override fun getFixtureExpression(
        nullableFixtureExpression: String,
        fixtureExpression: String,
        nullable: Boolean,
    ): String {
        return if (nullable) {
            nullableFixtureExpression
        } else {
            fixtureExpression
        }
    }

    override fun getJvmAnnotations(): List<Annotation> {
        return emptyList()
    }
}
