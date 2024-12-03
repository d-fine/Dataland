package org.dataland.frameworktoolbox.intermediate.datapoints

import org.dataland.frameworktoolbox.specific.datamodel.Annotation
import org.dataland.frameworktoolbox.specific.datamodel.TypeReference
import org.dataland.frameworktoolbox.specific.viewconfig.functional.FrameworkDisplayValueLambda

/**
 * Elements marked with NoDocumentSupport do not require any documentation / proof
 */
data object NoDocumentSupport : DocumentSupport {
    override fun getNamingPrefix(): String = "plain"

    override fun getQaJvmTypeReference(
        innerType: TypeReference,
        nullable: Boolean,
    ): TypeReference? = null

    override fun getJvmTypeReference(
        innerType: TypeReference,
        nullable: Boolean,
    ): TypeReference = innerType

    override fun getFrameworkDisplayValueLambda(
        innerLambda: FrameworkDisplayValueLambda,
        fieldLabel: String?,
        dataPointAccessor: String,
    ): FrameworkDisplayValueLambda = innerLambda

    override fun getDataAccessor(
        dataPointAccessor: String,
        nullable: Boolean,
    ): String = dataPointAccessor

    override fun getFixtureExpression(
        nullableFixtureExpression: String,
        fixtureExpression: String,
        nullable: Boolean,
    ): String =
        if (nullable) {
            nullableFixtureExpression
        } else {
            fixtureExpression
        }

    override fun getJvmAnnotations(): List<Annotation> = emptyList()
}
