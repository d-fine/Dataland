package org.dataland.frameworktoolbox.intermediate.datapoints

import org.dataland.frameworktoolbox.specific.datamodel.Annotation
import org.dataland.frameworktoolbox.specific.datamodel.TypeReference
import org.dataland.frameworktoolbox.specific.viewconfig.functional.FrameworkDisplayValueLambda
import org.dataland.frameworktoolbox.template.model.TemplateDocumentSupport

/**
 * A DocumentSupport implementation specified to which degrees evidence is desired / required for this datapoint
 * This has a large impact on the generated datamodel.
 */
sealed interface DocumentSupport {
    companion object {
        /**
         * Obtain a DocumentSupport implementation from a TemplateDocumentSupport instance from the template CSV
         */
        fun fromTemplate(templateDocumentSupport: TemplateDocumentSupport): DocumentSupport {
            return when (templateDocumentSupport) {
                TemplateDocumentSupport.None -> NoDocumentSupport
                TemplateDocumentSupport.Simple -> SimpleDocumentSupport
                TemplateDocumentSupport.Extended -> ExtendedDocumentSupport
            }
        }
    }

    /**
     * Calculate the JVM type-reference for a datapoint of type innerType given the current DocumentSupport
     * requirements
     * @param innerType the type of the datapoint
     * @param nullable true iff the datapoint should be nullable
     */
    fun getJvmTypeReference(innerType: TypeReference, nullable: Boolean): TypeReference

    /**
     * Calculate the JVM type-reference for a QA-DataPoint of type innerType given the current DocumentSupport
     * requirements
     * @param innerType the type of the datapoint
     * @param nullable true iff the datapoint should be nullable
     */
    fun getQaJvmTypeReference(innerType: TypeReference, nullable: Boolean): TypeReference?

    /**
     * Calculate a list of annotations that must be applied to JVM-types in the backend using this
     * document-support class.
     */
    fun getJvmAnnotations(): List<Annotation>

    /**
     * Calculate a Framework Display Lambda for a datapoint with original lambda innerLambda given the current
     * DocumentSupport requirements
     * @param innerLambda a lambda for displaying the underlying datapoint
     * @param fieldLabel a human-readable label for the field
     * @param dataPointAccessor a ts-accessor to the containing datapoint
     */
    fun getFrameworkDisplayValueLambda(
        innerLambda: FrameworkDisplayValueLambda,
        fieldLabel: String?,
        dataPointAccessor: String,
    ): FrameworkDisplayValueLambda

    /**
     * Calculate a TS-Accessor for getting the value of a datapoint with the current DocumentSupport requirements
     * @param dataPointAccessor a TS-Accessor to the containing datapoint
     * @param nullable true iff the datapoint is nullable
     */
    fun getDataAccessor(dataPointAccessor: String, nullable: Boolean): String

    /**
     * Calculate a Fixture-Expression for generating the value of a datapoint with the current DocumentSupport
     * requirements
     * @param nullableFixtureExpression a fixture expression that generates a possibly null value for the data-element
     * @param fixtureExpression a fixture expression that generates a non-nullable value for the data-element
     * @param nullable true iff the datapoint is nullable
     */
    fun getFixtureExpression(nullableFixtureExpression: String, fixtureExpression: String, nullable: Boolean): String
}
