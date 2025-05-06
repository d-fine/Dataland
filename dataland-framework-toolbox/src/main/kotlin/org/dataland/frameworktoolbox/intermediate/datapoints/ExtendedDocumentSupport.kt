package org.dataland.frameworktoolbox.intermediate.datapoints

import org.dataland.frameworktoolbox.specific.datamodel.Annotation
import org.dataland.frameworktoolbox.specific.datamodel.TypeReference
import org.dataland.frameworktoolbox.specific.datamodel.annotations.ValidAnnotation
import org.dataland.frameworktoolbox.specific.qamodel.getBackendClientTypeReference

/**
 * Elements marked with ExtendedDocumentSupport are converted to ExtendedDataPoints
 */
data object ExtendedDocumentSupport : DocumentSupport by SimpleDocumentSupport {
    override fun getNamingPrefix(): String = "extended"

    override fun getJvmTypeReference(
        innerType: TypeReference,
        nullable: Boolean,
    ): TypeReference =
        TypeReference(
            "org.dataland.datalandbackend.model.datapoints.ExtendedDataPoint",
            nullable,
            listOf(innerType),
        )

    override fun getQaJvmTypeReference(
        innerType: TypeReference,
        nullable: Boolean,
    ): TypeReference =
        TypeReference(
            "org.dataland.datalandqaservice.model.reports.QaReportDataPoint",
            true,
            listOf(getJvmTypeReference(innerType, nullable).getBackendClientTypeReference()),
        )

    override fun getFixtureExpression(
        nullableFixtureExpression: String,
        fixtureExpression: String,
        nullable: Boolean,
    ): String =
        if (nullable) {
            "dataGenerator.randomExtendedDataPoint($nullableFixtureExpression)"
        } else {
            "dataGenerator.guaranteedExtendedDataPoint($fixtureExpression)"
        }

    override fun getJvmAnnotations(): List<Annotation> = listOf(ValidAnnotation)
}
