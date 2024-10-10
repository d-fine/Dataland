package org.dataland.frameworktoolbox.frameworks.lksg.custom
import org.dataland.frameworktoolbox.intermediate.FieldNodeParent
import org.dataland.frameworktoolbox.intermediate.components.requireDocumentSupportIn
import org.dataland.frameworktoolbox.intermediate.datapoints.NoDocumentSupport
import org.dataland.frameworktoolbox.specific.datamodel.TypeReference
import org.dataland.frameworktoolbox.specific.datamodel.elements.DataClassBuilder

/**
 * Represents the Lksg risk positions component
 */
class LksgRiskAssessmentsComponent(
    identifier: String,
    parent: FieldNodeParent,
) : LksgSimpleCustomComponentBase(
        identifier = identifier,
        parent = parent,
        viewFormattingFunctionName = "formatLksgRisksOrViolationsForDisplay",
        uploadComponentName = "RiskAssessmentsFormField",
        guaranteedFixtureExpression =
            "dataGenerator.guaranteedArray(() => " +
                "dataGenerator.generateLksgRiskOrViolationAssessment(), 1, 5)",
        randomFixtureExpression =
            "dataGenerator.randomArray(() => " +
                "dataGenerator.generateLksgRiskOrViolationAssessment(), 1, 5)",
    ) {
    override fun generateDefaultDataModel(dataClassBuilder: DataClassBuilder) {
        requireDocumentSupportIn(setOf(NoDocumentSupport))
        dataClassBuilder.addProperty(
            identifier,
            TypeReference(
                "List", isNullable,
                listOf(
                    TypeReference(
                        "org.dataland.datalandbackend.frameworks.lksg.custom.LksgRiskOrViolationAssessment",
                        true,
                    ),
                ),
            ),
        )
    }
}
